package com.example.tools;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.facebook.stetho.common.Util;
import com.facebook.stetho.inspector.protocol.module.Database.ExecuteSQLResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.example.tools.RealmQueryUtil.EQ;
import static com.example.tools.RealmQueryUtil.GT;
import static com.example.tools.RealmQueryUtil.GTE;
import static com.example.tools.RealmQueryUtil.ILIKE;
import static com.example.tools.RealmQueryUtil.IS_NULL;
import static com.example.tools.RealmQueryUtil.LIKE;
import static com.example.tools.RealmQueryUtil.LIMIT;
import static com.example.tools.RealmQueryUtil.LT;
import static com.example.tools.RealmQueryUtil.LTE;
import static com.example.tools.RealmQueryUtil.NOT_NULL;
import static com.example.tools.RealmQueryUtil.SDF;
import static com.example.tools.RealmQueryUtil.SELECT_PATTERN;
import static com.example.tools.RealmQueryUtil.SORT;
import static com.example.tools.RealmQueryUtil.applyEq;
import static com.example.tools.RealmQueryUtil.applyGT;
import static com.example.tools.RealmQueryUtil.applyGTE;
import static com.example.tools.RealmQueryUtil.applyILike;
import static com.example.tools.RealmQueryUtil.applyIsNull;
import static com.example.tools.RealmQueryUtil.applyLT;
import static com.example.tools.RealmQueryUtil.applyLTE;
import static com.example.tools.RealmQueryUtil.applyLike;
import static com.example.tools.RealmQueryUtil.applyNotNull;
import static com.example.tools.RealmQueryUtil.getFirstWord;

/**
 * Created by THINK on 2016/10/11.
 */

public class RealmExecuteResultHandler {

    public static final String JSON_FORMAT_ERR = "Json format is invalid";
    final Context context;
    final RealmConfiguration realmConfiguration;
    private final Map<String, Class<? extends RealmModel>> modelMap;
    private final Map<String, RealmModelHandler> modelHandlerMap;

    public RealmExecuteResultHandler(Context context, RealmConfiguration realmConfiguration) {
        this.context = context;
        this.realmConfiguration = realmConfiguration;
        Set<Class<? extends RealmModel>> modelClassSet = realmConfiguration.getRealmObjectClasses();
        Map<String, Class<? extends RealmModel>> mModelMap = new HashMap<>();
        Map<String, RealmModelHandler> mModelHandlerMap = new HashMap<>();

        this.modelMap = Collections.unmodifiableMap(mModelMap);
        this.modelHandlerMap = Collections.unmodifiableMap(mModelHandlerMap);

        for (Class<? extends RealmModel> modelClass : modelClassSet) {
            String modelName = modelClass.getSimpleName().toLowerCase();
            mModelMap.put(modelName, modelClass);
            mModelHandlerMap.put(modelName, new RealmModelHandler(modelClass));
        }
    }

    public static String getAndCheckTableName(RealmQueryString seq) {
        String tableName = seq.nextToken();
        if (tableName == null || tableName.isEmpty()) {
            Errors.requireTableName();
        }
        return tableName;
    }

    private static void copyField(JSONObject param, RealmModel model, RealmModelHandler realmModelHandler) throws Exception {

        Iterator<String> keys = param.keys();
        while (keys.hasNext()) {
            String fieldName = keys.next();
            Object fieldValue = param.get(fieldName);
            Method method = realmModelHandler.propertySetter.get(fieldName.toLowerCase());
            Field field = realmModelHandler.allFields.get(fieldName);
            if (method != null) {
                Convertor convertor = Convertor.getConvertor(fieldValue.getClass(), method.getParameterTypes()[0]);
                if (convertor != null) {
                    fieldValue = convertor.convert(fieldValue);
                }
                method.invoke(model, fieldValue);
            } else if (field != null && realmModelHandler.isPropertyField(field)) {
                Convertor convertor = Convertor.getConvertor(fieldValue.getClass(), field.getType());
                if (convertor != null) {
                    fieldValue = convertor.convert(fieldValue);
                }
                field.set(model, fieldValue);
            } else {
                Errors.fieldNotFound(fieldName);
            }

        }
    }

    private boolean isStringArrayParam(String clauseType) {
        switch (clauseType) {
            case IS_NULL:
            case NOT_NULL:
                return true;
            default:
                return false;
        }
    }

    private boolean isJsonParam(String clauseType) {
        switch (clauseType) {
            case EQ:
            case LT:
            case LTE:
            case GT:
            case GTE:
            case LIKE:
            case ILIKE:
                return true;
            default:
                return false;
        }
    }

    private void applyParams(
            String clauseType,
            Object params,
            RealmModelHandler realmModelHandler,
            RealmQuery<? extends RealmModel> where

    ) throws JSONException {

        if (params instanceof JSONObject
                || params instanceof JSONArray) {
            if (isStringArrayParam(clauseType)) {
                JSONArray jsonArray = (JSONArray) params;
                applyStringArrayParams(clauseType, jsonArray, realmModelHandler, where);
            } else if (isJsonParam(clauseType)) {
                JSONObject jsonObject = (JSONObject) params;
                applyJsonParams(clauseType, jsonObject, realmModelHandler, where);
            } else {
                Errors.unsupportedAction(clauseType);
            }
        } else {
            List<String> propertyNames = realmModelHandler.getPropertyName();
            boolean contains = propertyNames.contains(clauseType);
            if (contains) {
                Class propertyType = realmModelHandler.getPropertyType(clauseType);
                applyEq(where, clauseType, propertyType, params, realmModelHandler);
            } else {
                Errors.fieldNotFound(clauseType);
            }
        }

    }

    private void applyStringArrayParams(
            String clauseType,
            JSONArray jsonArray,
            RealmModelHandler realmModelHandler,
            RealmQuery<? extends RealmModel> where

    ) throws JSONException {
        int length = jsonArray.length();
        switch (clauseType) {

            case IS_NULL:
                for (int i = 0; i < length; i++) {
                    applyIsNull(where, jsonArray.getString(i), realmModelHandler);
                }
                break;
            case NOT_NULL:
                for (int i = 0; i < length; i++) {
                    applyNotNull(where, jsonArray.getString(i), realmModelHandler);
                }
                break;
            default:
                throw new SQLiteException("unexpected Array Param with type:" + clauseType);
        }
    }

    private void applyJsonParams(
            String clauseType,
            JSONObject jsonParam,
            RealmModelHandler realmModelHandler,
            RealmQuery<? extends RealmModel> where

    ) throws JSONException {

        Iterator<String> paramNames = jsonParam.keys();
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            Class propertyType = realmModelHandler.getPropertyType(paramName);
            if (propertyType == null) {
                Errors.fieldNotFound(paramName);
            } else {
                Object p = jsonParam.get(paramName);
                Object paramValue = p;
                Convertor convertor = Convertor.getConvertor(p.getClass(), propertyType);
                if (convertor != null) {
                    try {
                        paramValue = convertor.convert(p);
                    } catch (Exception e) {
                        Errors.convertError(p, paramName, propertyType, e);
                    }
                }
                switch (clauseType) {
                    case EQ:
                        applyEq(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    case LT:
                        applyLT(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    case LTE:
                        applyLTE(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    case GT:
                        applyGT(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    case GTE:
                        applyGTE(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    case LIKE:
                        applyLike(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    case ILIKE:
                        applyILike(where, paramName, propertyType, paramValue, realmModelHandler);
                        break;
                    default:
                        Errors.unSupportedClauseType(clauseType);
                }
            }
        }
    }

    private void extractLimitAndOrder(JSONObject jsonObject, LimitAndOrder limitAndOrder) throws JSONException {
        if (jsonObject.has(LIMIT)) {
            JSONArray jsonArray = jsonObject.getJSONArray(LIMIT);
            jsonObject.remove(LIMIT);
            int[] a = new int[jsonArray.length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = jsonArray.getInt(i);
            }
            limitAndOrder.setLimit(a);

        }
        List<Order> order = limitAndOrder.getOrder();
        if (jsonObject.has(SORT)) {
            JSONArray jsonArray = jsonObject.getJSONArray(SORT);
            jsonObject.remove(SORT);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                order.add(new Order(jsonArray.getString(i)));
            }
        }
    }

    private RealmResults<? extends RealmModel> sort(
            RealmResults<? extends RealmModel> results,
            LimitAndOrder limitAndOrder) {
        List<Order> order = limitAndOrder.getOrder();
        int size = order.size();
        String[] o = new String[size];
        Sort[] s = new Sort[size];
        for (int i = 0; i < order.size(); i++) {
            Order order1 = order.get(i);
            o[i] = order1.fieldName;
            s[i] = order1.asc ? Sort.ASCENDING : Sort.DESCENDING;
        }
        return size > 0 ? results.sort(o, s) : results;
    }

    private LimitAndOrder prepareQueryAndExtractLimitAndOrder(
            RealmQuery<? extends RealmModel> where,
            RealmModelHandler realmModelHandler,
            JSONArray jsonArray
    ) throws JSONException {
        Util.throwIfNull(jsonArray);
        LimitAndOrder limitAndOrder = new LimitAndOrder();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            extractLimitAndOrder(jsonObject, limitAndOrder);
            if (jsonObject.length() <= 0) {
                continue;
            }
            if (i > 0) {
                where.or();
            }
            Iterator<String> keys = jsonObject.keys();
            where.beginGroup();
            while (keys.hasNext()) {
                String nextKey = keys.next();
                Object value = jsonObject.get(nextKey);
                applyParams(nextKey, value, realmModelHandler, where);
            }
            where.endGroup();
        }
        return limitAndOrder;
    }


    ExecuteSQLResponse handleSelect(String query) {
        Matcher matcher = SELECT_PATTERN.matcher(query);
        String tableName;
        if (matcher.matches()) {
            tableName = matcher.group(1);
            return handleList(tableName);
        } else {
            return handleList(query.trim().substring(getFirstWord(query).length()));
        }

    }

    private List<? extends RealmModel> doQuery(
            Realm realm,
            String tableName,
            JSONArray jsonArray) throws JSONException {
        Class<? extends RealmModel> modelClass = getModelClass(tableName);
        RealmModelHandler realmModelHandler = modelHandlerMap.get(tableName);
        RealmQuery<? extends RealmModel> where = realm.where(modelClass);
        LimitAndOrder limitAndOrder = prepareQueryAndExtractLimitAndOrder(where, realmModelHandler, jsonArray);
        RealmResults<? extends RealmModel> results = where.findAll();
        results = sort(results, limitAndOrder);
        List<? extends RealmModel> realmModels;
        int resultSize = results.size();
        int[] limit = limitAndOrder.getLimit();
        boolean reverse = false;
        switch (limit.length) {
            case 0:
                realmModels = results.subList(0, resultSize);
                break;
            case 1:
                realmModels = results.subList((resultSize + limit[0]) % resultSize, resultSize);
                break;
            default:
                int a = (resultSize + limit[0]) % resultSize;
                int b = (resultSize + limit[1]) % resultSize;
                reverse = a > b;
                realmModels = (a < b ? results.subList(a, b) : results.subList(b, a));
        }
        if (reverse) {
            List<RealmModel> reverseResult = new ArrayList<>();
            for (int i = 0; i < realmModels.size(); i++) {
                reverseResult.add(0, realmModels.get(i));
            }
            return reverseResult;
        } else {
            return realmModels;
        }
    }

    ExecuteSQLResponse handleList(String query) {

        RealmQueryString sequence = new RealmQueryString(query);
        String tableName = getAndCheckTableName(sequence);
        JSONArray jsonArray = sequence.getTailAsJSONArray();
        RealmModelHandler realmModelHandler = modelHandlerMap.get(tableName);
        Realm realm = Realm.getInstance(realmConfiguration);
        ExecuteSQLResponse response = new ExecuteSQLResponse();

        try {
            List<? extends RealmModel> realmModels
                    = doQuery(realm, tableName, jsonArray);

            realmModels = realm.copyFromRealm(realmModels, 1);
            response.columnNames = realmModelHandler.getPropertyName();
            response.values = realmModelHandler.getFlattenRows(realmModels);
        } catch (JSONException e) {
            Errors.rethrow(e);
        } finally {
            realm.close();
        }
        return response;
    }

    ExecuteSQLResponse handleInsert(String query) {
        RealmQueryString sequence = new RealmQueryString(query);
        final String tableName = getAndCheckTableName(sequence);
        final JSONArray jsonArray = sequence.getTailAsJSONArray();
        final RealmModelHandler realmModelHandler = modelHandlerMap.get(tableName);
        Realm realm = Realm.getInstance(realmConfiguration);
        final ExecuteSQLResponse response = new ExecuteSQLResponse();
        try {
            final int length = jsonArray.length();
            if (length == 0) {
                Errors.requiredParams();
            }
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (int i = 0; i < length; i++) {
                        try {
                            RealmModel newCreated = modelMap.get(tableName).newInstance();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            copyField(jsonObject, newCreated, realmModelHandler);
                            realm.copyToRealm(newCreated);
                        } catch (Exception e) {
                            Errors.rethrow(e);
                        }
                        response.columnNames = Collections.singletonList("created");
                        response.values = Collections.singletonList(length + " records");
                    }
                }
            });
        } catch (Exception e) {
            Errors.rethrow(e);
        } finally {
            realm.close();
        }
        return response;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    ExecuteSQLResponse handleUpdate(String query) {
        RealmQueryString sequence = new RealmQueryString(query);
        String tableName = getAndCheckTableName(sequence);
        JSONArray jsonArray = sequence.getTailAsJSONArray();

        final RealmModelHandler realmModelHandler = modelHandlerMap.get(tableName);
        Realm realm = Realm.getInstance(realmConfiguration);
        final ExecuteSQLResponse response = new ExecuteSQLResponse();

        try {
            if (jsonArray.length() < 2) {
                Errors.unsupportedAction("update all");
            }
            final JSONObject o = jsonArray.getJSONObject(0);
            jsonArray.remove(0);
            final List<? extends RealmModel> realmModels
                    = doQuery(realm, tableName, jsonArray);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        for (RealmModel realmModel : realmModels) {
                            copyField(o, realmModel, realmModelHandler);
                        }
                    } catch (Exception e) {
                        Errors.rethrow(e);
                    }
                    /*try {
                        List<? extends RealmModel> detached = realm.copyFromRealm(realmModels);
                        for (RealmModel detachedModel : detached) {
                            copyField(o, detachedModel, realmModelHandler);
                        }
                        realm.copyToRealmOrUpdate(detached);
                    } catch (Exception e) {
                        Errors.rethrow(e);
                    }*/
                }
            });

            response.columnNames = Collections.singletonList("updated");
            response.values = Collections.singletonList(realmModels.size() + " records");

        } catch (JSONException e) {
            Errors.rethrow(e);
        } finally {
            realm.close();
        }
        return response;
    }

    ExecuteSQLResponse handleDelete(String query) {

        RealmQueryString sequence = new RealmQueryString(query);
        final String tableName = getAndCheckTableName(sequence);
        JSONArray jsonArray = sequence.getTailAsJSONArray();
        if (jsonArray.length() < 1) {
            Errors.unsupportedAction("delete all");
        }
        Realm realm = Realm.getInstance(realmConfiguration);
        final ExecuteSQLResponse response = new ExecuteSQLResponse();

        try {
            final List<? extends RealmModel> realmModels = doQuery(realm, tableName, jsonArray);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (RealmModel realmModel : realmModels) {
                        RealmObject.deleteFromRealm(realmModel);
                    }
                    response.columnNames = Collections.singletonList("Deleted");
                    response.values = Collections.singletonList(realmModels.size() + " records");

                }
            });
        } catch (JSONException e) {
            Errors.rethrow(e);
        } finally {
            realm.close();
        }
        return response;
    }

    public Class<? extends RealmModel> getModelClass(String tableName) {
        Class<? extends RealmModel> modelClass = modelMap.get(tableName);
        Util.throwIfNull(modelClass);
        return modelClass;
    }

    public void test() {
        Realm realm;

    }

    private class Order {
        public final String fieldName;
        public final boolean asc;

        public Order(String param) {
            String trim = param.trim();
            if (trim.startsWith("-")) {
                this.asc = false;
                this.fieldName = trim.substring(1);
            } else {
                this.asc = true;
                this.fieldName = trim;
            }
        }

    }

    public class LimitAndOrder {
        int[] limit = new int[0];
        List<Order> order = new ArrayList<>();

        public int[] getLimit() {
            return limit;
        }

        public void setLimit(int[] limit) {
            this.limit = limit;
        }

        public List<Order> getOrder() {
            return order;
        }
    }

    public class RealmModelHandler {

        private final List<Method> propertyGetter;
        private final Map<String, Method> propertySetter;
        private final List<String> propertyName;
        private final Map<String, Class> propertyTypes;
        private final List<Field> propertyFields;
        private final Map<String, Field> allFields;

        public RealmModelHandler(Class<? extends RealmModel> clazz) {
            List<Method> mPropertyGetter = new ArrayList<>();
            Map<String, Method> mPropertySetter = new HashMap<>();
            List<String> mPropertyName = new ArrayList<>();
            Map<String, Class> mPropertyTypes = new HashMap<>();
            List<Field> mPropertyFields = new ArrayList<>();
            Map<String, Field> mAllFields = new HashMap<>();
            propertyGetter = Collections.unmodifiableList(mPropertyGetter);
            propertySetter = Collections.unmodifiableMap(mPropertySetter);
            propertyName = Collections.unmodifiableList(mPropertyName);
            propertyTypes = Collections.unmodifiableMap(mPropertyTypes);
            propertyFields = Collections.unmodifiableList(mPropertyFields);
            allFields = Collections.unmodifiableMap(mAllFields);
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                String simpleName = method.getName().substring(3).toLowerCase();
                if (isPropertyGetter(method)) {
                    mPropertyGetter.add(method);
                    mPropertyName.add(simpleName);
                    mPropertyTypes.put(simpleName, method.getReturnType());
                } else if (isPropertySetter(method)) {
                    mPropertySetter.put(simpleName, method);
                }
            }
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                String name = declaredField.getName().toLowerCase();
                mAllFields.put(name, declaredField);
                if (isPropertyField(declaredField) && (!propertyName.contains(name))) {
                    mPropertyFields.add(declaredField);
                    mPropertyName.add(name);
                }
            }

        }

        private String formatField(Object o) {
            if (o == null) {
                return null;
            } else if (o instanceof Date) {
                return SDF.format(o);
            } else {
                return o.toString();
            }
        }

        public String getRealFieldName(String nameLowerCase) {
            Field field = allFields.get(nameLowerCase);
            return field == null ? nameLowerCase : field.getName();
        }

        public List<String> getFlattenRows(List<? extends RealmModel> unmanagedValues) {
            List<String> result = new ArrayList<>();
            Object[] args = new Object[0];
            for (RealmModel unmanagedValue : unmanagedValues) {
                for (Method method : propertyGetter) {
                    try {
                        Object invoke = method.invoke(unmanagedValue, args);
                        result.add(formatField(invoke));
                    } catch (Exception e) {
                        Errors.rethrow(e);
                    }
                }
                for (Field propertyField : propertyFields) {
                    try {
                        Object o = propertyField.get(unmanagedValue);
                        result.add(formatField(o));
                    } catch (IllegalAccessException e) {
                        Errors.rethrow(e);
                    }
                }

            }
            return result;
        }

        public Class getPropertyType(String propertyName) {
            if (propertyName.contains(".")) {
                int i = propertyName.indexOf(".");
                String path = propertyName.substring(0, i);
                String simpleName = getPropertyType(path).getSimpleName().toLowerCase();
                RealmModelHandler realmModelHandler = modelHandlerMap.get(simpleName);
                return realmModelHandler.getPropertyType(propertyName.substring(i + 1));
            }
            return propertyTypes.get(propertyName);
        }

        public List<String> getPropertyName() {

            return propertyName;
        }

        private boolean isPropertyGetter(Method method) {
            String name = method.getName();
            int modifiers = method.getModifiers();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();

            boolean result;
            result = Modifier.isPublic(modifiers)
                    && (!Modifier.isStatic(modifiers))
                    && (!Modifier.isAbstract(modifiers))
                    && (name.startsWith("get"))
                    && (name.length() > 3)
                    && (!"void".equals(returnType.getName()))
                    && (parameterTypes == null || parameterTypes.length == 0);
            return result;
        }

        private boolean isPropertyField(Field field) {

            int modifiers = field.getModifiers();

            boolean result;
            result = Modifier.isPublic(modifiers)
                    && (!Modifier.isStatic(modifiers))
                    && (!Modifier.isFinal(modifiers));
            return result;
        }

        private boolean isPropertySetter(Method method) {
            String name = method.getName();
            int modifiers = method.getModifiers();
            Class<?>[] parameterTypes = method.getParameterTypes();

            boolean result;
            result = Modifier.isPublic(modifiers)
                    && (!Modifier.isStatic(modifiers))
                    && (!Modifier.isAbstract(modifiers))
                    && (name.startsWith("set"))
                    && (name.length() > 3)
                    && (parameterTypes == null || parameterTypes.length == 1);
            return result;
        }
    }
}

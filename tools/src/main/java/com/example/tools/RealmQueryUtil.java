package com.example.tools;

import android.database.sqlite.SQLiteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Case;
import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Created by THINK on 2016/10/11.
 */

public class RealmQueryUtil {
    public static final Pattern SELECT_PATTERN
            = Pattern.compile("(?i)^\\s*select\\s+[^\\r\\n]*from\\s*\"\\s*([^\"]+?)\\s*\"\\s*$");
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final String EQ = "eq";
    public static final String LT = "lt";
    public static final String LTE = "lte";
    public static final String GT = "gt";
    public static final String GTE = "gte";
    public static final String LIKE = "lk";
    public static final String ILIKE = "ilk";
    public static final String IS_NULL = "isnull";
    public static final String NOT_NULL = "notnull";
    public static final String LIMIT = "limit";
    public static final String SORT = "sort";

    public static String getFirstWord(String s) {
        s = s.trim();
        int firstSpace = s.indexOf(' ');
        int firstB = s.indexOf('{');
        int token = -1;
        if (firstSpace > 0) {
            token = firstSpace;
            if (firstB > 0) {
                token = Math.min(firstB, firstSpace);
            }
        } else if (firstB > 0) {
            token = firstB;
        }

        return token >= 0 ? s.substring(0, token) : s;
    }


    public static void applyNotNull(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        where.isNotNull(realmModelHandler.getRealFieldName(fieldName));
    }

    public static void applyIsNull(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        where.isNull(realmModelHandler.getRealFieldName(fieldName));
    }

    public static void applyLike(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object value,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        if (fieldType.getName().equals("java.lang.String")) {
            where.contains(realmModelHandler.getRealFieldName(fieldName), value.toString(), Case.SENSITIVE);
        } else {
            Errors.doLikeQueryOnNonStringField();
        }
    }

    public static void applyILike(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object value,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        if (fieldType.getName().equals("java.lang.String")) {
            where.contains(realmModelHandler.getRealFieldName(fieldName), value.toString(), Case.INSENSITIVE);
        } else {
            Errors.doLikeQueryOnNonStringField();
        }
    }

    public static void applyEq(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object value,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        try {
            fieldName = realmModelHandler.getRealFieldName(fieldName);
            if (value == null) {
                where.isNull(fieldName);
            } else if (fieldType.getName().equals("java.lang.String")) {
                where.contains(fieldName, value.toString(), Case.INSENSITIVE);
            } else {
                Method equalTo = where.getClass().getMethod("equalTo", String.class, getWapperType(fieldType));
                equalTo.invoke(where, fieldName, value);
            }
        } catch (Exception e) {
            Errors.failedApplyActionOnField("eq(equalTo)", fieldName, e);
        }
    }

    public static void applyRange(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object lower,
            Object upper,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        if (lower == null) {
            if (upper != null) {
                applyLTE(where, fieldName, fieldType, upper,realmModelHandler);
            }
        } else if (upper == null) {
            applyGTE(where, fieldName, fieldType, lower,realmModelHandler);
        } else {
            applyBetween(where, fieldName, fieldType, lower, upper,realmModelHandler);
        }
    }


    public static void applyBetween(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object lower,
            Object upper,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        fieldName = realmModelHandler.getRealFieldName(fieldName);
        try {
            Class unWapperType = getUnWapperType(fieldType);
            Method between = where.getClass().getMethod("between", String.class, unWapperType, unWapperType);
            between.invoke(where, fieldName, lower, upper);
        } catch (Exception e) {
            Errors.failedApplyActionOnField("bt(between)", fieldName, e);
        }
    }

    public static void applyGT(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object lower,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        fieldName = realmModelHandler.getRealFieldName(fieldName);
        try {

            Class unWapperType = getUnWapperType(fieldType);
            Method between = where.getClass().getMethod("greaterThan", String.class, unWapperType);
            between.invoke(where, fieldName, lower);
        } catch (Exception e) {
            Errors.failedApplyActionOnField("gt(greaterThan)", fieldName, e);
        }
    }

    public static void applyGTE(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object lower,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        fieldName=realmModelHandler.getRealFieldName(fieldName);
        try {

            Class unWapperType = getUnWapperType(fieldType);
            Method between = where.getClass().getMethod("greaterThanOrEqualTo", String.class, unWapperType);
            between.invoke(where, fieldName, lower);
        } catch (Exception e) {
            Errors.failedApplyActionOnField("gte(greaterThanOrEqualTo)", fieldName, e);
        }
    }

    public static void applyLT(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object upper,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        fieldName=realmModelHandler.getRealFieldName(fieldName);
        try {

            Class unWapperType = getUnWapperType(fieldType);
            Method between = where.getClass().getMethod("lessThan", String.class, unWapperType);
            between.invoke(where, fieldName, upper);
        } catch (Exception e) {
            Errors.failedApplyActionOnField("lt(lessThan)", fieldName, e);
        }
    }

    public static void applyLTE(
            RealmQuery<? extends RealmModel> where,
            String fieldName,
            Class fieldType,
            Object upper,
            RealmExecuteResultHandler.RealmModelHandler realmModelHandler
    ) {
        try {
            realmModelHandler.getRealFieldName(fieldName);
            Class unWapperType = getUnWapperType(fieldType);
            Method between = where.getClass().getMethod("lessThanOrEqualTo", String.class, unWapperType);
            between.invoke(where, fieldName, upper);
        } catch (Exception e) {
            Errors.failedApplyActionOnField("lte(lessThanOrEqualTo)", fieldName, e);
        }
    }

    public static Class getWapperType(Class clazz) {
        String name = clazz.getName();
        switch (name) {
            case "byte":
                return Byte.class;
            case "char":
                return Character.class;
            case "int":
                return Integer.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            case "boolean":
                return Boolean.class;
            default:
                return clazz;
        }
    }

    public static Class getUnWapperType(Class clazz) {
        String name = clazz.getName();
        switch (name) {
            case "java.lang.Byte":
                return byte.class;
            case "java.lang.Character":
                return char.class;
            case "java.lang.Integer":
                return int.class;
            case "java.lang.Long":
                return long.class;
            case "java.lang.Float":
                return float.class;
            case "java.lang.Double":
                return double.class;
            case "java.lang.Boolean":
                return boolean.class;
            default:
                return clazz;
        }
    }


}

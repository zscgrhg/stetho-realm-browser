package com.example.tools;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.facebook.stetho.inspector.protocol.module.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.RealmConfiguration;
import io.realm.RealmModel;

import static com.example.tools.RealmQueryUtil.getFirstWord;

/**
 * Created by THINK on 2016/10/11.
 */

public class RealmDatabaseDriver extends Database.DatabaseDriver {


    private final Map<String, RealmExecuteResultHandler> handlerMap;
    private final List<String> databaseNames;
    private final Map<String, List<String>> tableNamesMap;


    public RealmDatabaseDriver(Context context, RealmConfiguration... configurations) {
        super(context);

        Map<String, RealmExecuteResultHandler> mHandlerMap = new HashMap<>();
        List<String> mDatabaseNames = new ArrayList<>();
        Map<String, List<String>> mTableNamesMap = new HashMap<>();


        this.handlerMap = Collections.unmodifiableMap(mHandlerMap);
        this.databaseNames = Collections.unmodifiableList(mDatabaseNames);
        this.tableNamesMap = Collections.unmodifiableMap(mTableNamesMap);

        for (RealmConfiguration realmConfiguration : configurations) {
            String realmFileName = realmConfiguration.getRealmFileName();
            mHandlerMap.put(realmFileName, new RealmExecuteResultHandler(context, realmConfiguration));
            mDatabaseNames.add(realmFileName);
            ArrayList<String> tableNames = new ArrayList<>();
            mTableNamesMap.put(realmFileName, Collections.unmodifiableList(tableNames));
            Set<Class<? extends RealmModel>> modelClassSet = realmConfiguration.getRealmObjectClasses();
            for (Class<? extends RealmModel> modelClass : modelClassSet) {
                tableNames.add(modelClass.getSimpleName().toLowerCase());
            }
        }
    }


    @Override
    public List<String> getDatabaseNames() {
        ArrayList<String> names = new ArrayList<>();
        names.addAll(databaseNames);
        return names;
    }

    @Override
    public List<String> getTableNames(String databaseId) {

        ArrayList<String> names = new ArrayList<>();
        names.addAll(tableNamesMap.get(databaseId));
        return names;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Database.ExecuteSQLResponse executeSQL(
            String databaseName,
            String query,
            ExecuteResultHandler<Database.ExecuteSQLResponse> handler) throws SQLiteException {

        try {
            RealmExecuteResultHandler realmHandler = handlerMap.get(databaseName);
            String action = getFirstWord(query).toUpperCase();
            String querySubstr = query.trim().substring(action.length());
            switch (action) {
                case "UPDATE":
                case "SET":
                case "LET":
                    return realmHandler.handleUpdate(querySubstr);
                case "DELETE":
                case "RM":
                case "DEL":
                    return realmHandler.handleDelete(querySubstr);
                case "INSERT":
                case "NEW":
                case "MK":
                case "ADD":
                case "MAKE":
                    return realmHandler.handleInsert(querySubstr);
                case "SELECT":
                    return realmHandler.handleSelect(query);
                case "LS":
                case "LIST":
                case "GET":
                case "SHOW":
                    return realmHandler.handleList(querySubstr);
                default:
                    throw new UnsupportedOperationException("Not yet implemented:" + action);
            }
        } catch (Exception e) {
            Database.ExecuteSQLResponse response = new Database.ExecuteSQLResponse();
            Database.Error error = new Database.Error();
            error.message = e.getMessage();
            error.code = 0;
            response.sqlError = error;
            return response;
        }
    }
}

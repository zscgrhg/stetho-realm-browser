package com.example.tools;

import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.security.spec.PSSParameterSpec;

/**
 * Created by THINK on 2016/10/12.
 */

public class Errors {

    public static class SyntaxException extends SQLException {
        public SyntaxException() {
        }

        public SyntaxException(String error) {
            super(error);
        }

    }

    public static void requireTableName() {
        throw new SyntaxException("table name is required");
    }

    public static void requiredParams() {
        throw new SyntaxException("Parameters are required");
    }

    public static void fieldNotFound(String fieldName) {
        throw new SyntaxException("field not found: " + fieldName);
    }

    public static void unsupportedAction(String action) {
        throw new UnsupportedOperationException("Unsupported Operation:" + action);
    }

    public static void unSupportedClauseType(String clauseType) {
        throw new SQLException("unSupportedClauseType:" + clauseType);
    }

    public static void rethrow(Exception e) {
        throw new RuntimeException(e);
    }

    public static void doLikeQueryOnNonStringField() {
        throw new SQLException("Like query on non-String field");
    }

    public static void failedApplyActionOnField(String action, String fieldName, Exception e) {
        throw new SQLException("apply " + action + " failed on Field:" + fieldName + " , detail: " + e.getMessage());
    }

    public static void convertError(Object value, String fieldName, Class fieldType, Exception e) {
        throw new SQLException("value Can not be converted: " + value + ",field:  " + fieldName +
                "expected Type:" + fieldType +
                " , detail: " + e.getMessage());
    }
}

package com.example.think.stetho_realm_browser;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.tools.RealmDatabaseDriver;
import com.facebook.stetho.inspector.protocol.module.Database;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.think.stetho_realm_browser", appContext.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Test
    public void testRealmDatabaseDriver() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Realm.init(appContext);
        RealmConfiguration SCHEMA_1 = RealmSchemas.SCHEMA_1;
        RealmDatabaseDriver databaseDriver = new RealmDatabaseDriver(appContext, SCHEMA_1);
        List<String> databaseNames = databaseDriver.getDatabaseNames();
        assertTrue(databaseNames.size() > 0);
        List<String> tableNames = databaseDriver.getTableNames(databaseNames.get(0));

        assertTrue(tableNames.contains("user"));
        Database.ExecuteSQLResponse response =
                databaseDriver.executeSQL(databaseNames.get(0), "ls user", null);
        assertTrue(response.columnNames.size() > 0);
        ;
    }
}

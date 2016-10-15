package com.example.tools;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.facebook.stetho.inspector.protocol.module.Database;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ExampleInstrumentedTest {
    public static final String DB_NAME = "schema1.realm";
    final Context appContext = InstrumentationRegistry.getTargetContext();
    Realm realm;
    RealmConfiguration SCHEMA_1;
    RealmDatabaseDriver databaseDriver;

    @BeforeClass
    public static void prepare() {
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    @AfterClass
    public static void close() {

    }

    @Before
    public void initDb() {

        SCHEMA_1 = new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(SCHEMA_1);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < 10; i++) {
                    User user = realm.createObject(User.class);
                    user.setName("user" + i);
                    user.setAge(i);
                }
            }
        });
        databaseDriver = new RealmDatabaseDriver(appContext, SCHEMA_1);
    }

    @After
    public void cleanDb() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.close();
    }

    @Test
    public void useAppContext() throws Exception {

        assertEquals("com.example.tools.test", appContext.getPackageName());
    }


    @Test
    public void testRealmDatabaseDriver() throws Exception {


        List<String> databaseNames = databaseDriver.getDatabaseNames();
        assertTrue(databaseNames.get(0).equals(DB_NAME));
        List<String> tableNames = databaseDriver.getTableNames(DB_NAME);
        assertTrue(tableNames.contains("user"));

    }


    @Test
    public void testLs() {

        Database.ExecuteSQLResponse response =
                databaseDriver.executeSQL(DB_NAME, "ls user", null);
        List<String> columnNames = response.columnNames;
        assertTrue(columnNames.contains("name"));
        assertTrue(columnNames.contains("age"));
        List<String> values = response.values;
        for (int i = 0; i < 10; i++) {
            assertTrue(values.size() == columnNames.size() * 10);
        }
    }


    @Test
    public void testUpdate() {
        databaseDriver.executeSQL(DB_NAME, "set user{name:zscgrhg},{name:user1}", null);
        User user1 = realm.where(User.class).equalTo("age", 1).findFirst();
        assertTrue(user1.getName().equals("zscgrhg"));
    }

    @Test
    public void testInsert() {
        Database.ExecuteSQLResponse response
                = databaseDriver.executeSQL(DB_NAME, "mk user{name:mk1,age:33},{name:mk2,age:34},{name:mk3,age:35}", null);
        assertTrue(response.values.get(0).toLowerCase().equals("3 records"));
        for (int i = 1; i < 4; i++) {
            User user1 = realm.where(User.class).equalTo("name", "mk" + i).findFirst();
            assertTrue(user1.getAge() == (32 + i));
        }
    }
}

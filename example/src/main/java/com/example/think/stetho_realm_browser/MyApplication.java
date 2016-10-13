package com.example.think.stetho_realm_browser;

import android.app.Application;

import com.example.tools.RealmSupport;
import com.facebook.stetho.Stetho;

import io.realm.Realm;

import static com.example.think.stetho_realm_browser.RealmSchemas.SCHEMA_1;
import static com.example.think.stetho_realm_browser.RealmSchemas.SCHEMA_2;

/**
 * Created by THINK on 2016/10/11.
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                RealmSupport.addRealmsToStethoDefaults(this, SCHEMA_1, SCHEMA_2))
                        .build());
    }
}

package com.example.think.stetho_realm_browser;

import android.app.Application;

import com.example.tools.RealmSupport;
import com.facebook.stetho.Stetho;

import io.realm.Realm;

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
                                RealmSupport.addRealmsToStethoDefaults(this,RealmSchemas.SCHEMA_1,RealmSchemas.SCHEMA_2))
                        .build());
    }
}

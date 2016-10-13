package com.example.tools;

import android.content.Context;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

import io.realm.RealmConfiguration;

/**
 * Created by THINK on 2016/10/11.
 */

public class RealmSupport {
    public static final InspectorModulesProvider addRealmsToStethoDefaults(final Context context, final RealmConfiguration... realmConfigurations){
        InspectorModulesProvider imp = new InspectorModulesProvider() {
            @Override
            public Iterable<ChromeDevtoolsDomain> get() {
                Stetho.DefaultInspectorModulesBuilder builder
                        = new Stetho.DefaultInspectorModulesBuilder(context);
                RealmDatabaseDriver databaseDriver=new RealmDatabaseDriver(context,realmConfigurations);
                builder.provideDatabaseDriver(databaseDriver);
                return builder.finish();
            }
        };
        return imp;
    }
}

#A Stetho Expansion Module To Browse Realm-Android Databases In Chrome DevTools's Inspector Window 


###Simple to Use

###### dependencies

```
repositories {
        maven{
            url "https://github.com/zscgrhg/stetho-realm-browser/raw/master/maven-repo/"
        }
}

dependencies {
    
    compile 'com.facebook.stetho:stetho:1.4.1'
    compile 'com.zscgrhg.devtools:tools:1.0'
    
}
```
######Android Application


```
public class RealmSchemas {
    public static final RealmConfiguration SCHEMA_1=new RealmConfiguration.Builder()
            .name("schema1.realm")
            .schemaVersion(2)
            .deleteRealmIfMigrationNeeded()
            .build();
    public static final RealmConfiguration SCHEMA_2=new RealmConfiguration.Builder()
            .name("schema2.realm")
            .schemaVersion(3)
            .deleteRealmIfMigrationNeeded()
            .build();
    public static final RealmConfiguration SCHEMA_3=new RealmConfiguration.Builder()
            .name("schema3.realm")
            .schemaVersion(4)
            .deleteRealmIfMigrationNeeded()
            .build();
}
```

```
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                RealmSupport.addRealmsToStethoDefaults(this,SCHEMA_1,SCHEMA_2))
                        .build());
    }
}

```

######AndroidManifest

```
<application
        android:name=".MyApplication"
        ...
</application>
```

######That's All Required!

###Stetho in debug build only

[http://littlerobots.nl/blog/stetho-for-android-debug-builds-only/](http://littlerobots.nl/blog/stetho-for-android-debug-builds-only/)


###Let's try it,Run The project(":example") In Android Studio


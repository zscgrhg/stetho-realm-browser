#A Stetho Expansion Module To Browse Realm-Android Databases Through Chrome DevTools 


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
    compile 'com.zscgrhg.devtools:tools:0.0.2'
    
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
 
Open Chrome to [chrome://inspect/#devices](chrome://inspect/#devices)

Click the Hyperlink inspect

![screenshot1](https://github.com/zscgrhg/stetho-realm-browser/blob/master/inspect.bmp)

######databases
![screenshot2](https://github.com/zscgrhg/stetho-realm-browser/blob/master/realm1.bmp)
######console
![screenshot3](https://github.com/zscgrhg/stetho-realm-browser/blob/master/realm2.bmp)

###Console Query Guild

######Format: [command] [table name] [Well-formed json args]

1. \>**ls user**

    results in:
   
    ```
    realm.where(User.class).findAll()
    ```
    
    \>**ls user{age:18}**
    
    results in:
    
    ```
    realm.where(User.class).equalTo("age",18).findAll()
    ```
    the followed json **{age:18},{name:obama}** Explained as Query Condition:
    
    \>**ls user{age:18},{name:obama}**
    
    ```
    realm.where(User.class).equalTo("age",18).or().equalTo("name","obama").findAll()
    ```
    
    **variants: select / get / list / show / ls**
    
2. \>**mk user{name:abc,age:18}**

    results in:
    
    ```
    realm.beginTransaction();
    User user=realm.createObject(User.class);
    user.setName("abc");
    user.setAge(18);
    realm.commitTransaction();
    ```
    
    **variants: insert / new / add / make / mk**
    
3. \>**rm user{}**

    results in:
    
    ```
    RealmResults<User> results = realm.where(User.class).findAll();
    results.deleteAllFromRealm()
    ```
    
    \>**rm user{age:10}**
    
    results in:
    
    ```
    RealmResults<User> results = realm.where(User.class)
    .equalTo("age",10)
    .findAll();
    results.deleteAllFromRealm()
    ```
    
    **variants: delete / del / rm**
    
4. \>**set user{age:18},{name:obama}**
    
    results in:(the first json is the values to update,then the query condition)
    
    ```
    RealmResults<User> results=realm.where(User.class).equalTo("name","obama").findAll()
    for(User user:results){
        user.setAge(18)
    }    
    ```
    
    **variants: let / set / update**
    
5. **conditions : eq / gt / gte / lt / lte / lk / ilk / isnull / notnull / limit / sort**
    
    \>**ls user{gt:{age:52},lk:{name:obama}},{lt:{age:24},ilk:{name:jax}}**
    
    results in:
    
    ```
    realm.where(User.class)
                            .greaterThan("age", 52)
                            .contains("name", "obama",Case.SENSITIVE)
                            .or()
                            .beginGroup()
                            .lessThan("age", 24)
                            .contains("name", "jax", Case.INSENSITIVE)
                            .endGroup()
                            .findAll();
    ```
    
    \>**ls user{gt:{age:52},lk:{name:obama}},{lt:{age:24},ilk:{name:jax},{limit:[3,5]}}**
    
    results in:
    
    ```
     realm.where(User.class)
                            .greaterThan("age", 52)
                            .contains("name", "obama",Case.SENSITIVE)
                            .or()
                            .beginGroup()
                            .lessThan("age", 24)
                            .contains("name", "jax", Case.INSENSITIVE)
                            .endGroup()
                            .findAll()
                            .subList(3,5);
    ```

    
    \>**ls user{gt:{age:52},lk:{name:obama}},{lt:{age:24},ilk:{name:jax},{limit:[3,5],sort:[name,-age]}}**
    
    results in:
    
    ```
    realm.where(User.class)
                            .greaterThan("age", 52)
                            .contains("name", "obama",Case.SENSITIVE)
                            .or()
                            .beginGroup()
                            .lessThan("age", 24)
                            .contains("name", "jax", Case.INSENSITIVE)
                            .endGroup()
                            .findAll()
                            .sort(new String[]{"name","age"},new Sort[]{Sort.ASCENDING,Sort.DESCENDING})
                            .subList(3,5);
    ```
    
    the leading '*-*' means *Sort.DESCENDING* ,default is *Sort.ASCENDING*
    



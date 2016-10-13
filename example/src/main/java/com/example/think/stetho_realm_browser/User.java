package com.example.think.stetho_realm_browser;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by THINK on 2016/10/7.
 */

public class User extends RealmObject {

    private String name;
    private int age;
    @PrimaryKey
    private String uuid;
    public String pa;
    public String pB;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {


        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


}

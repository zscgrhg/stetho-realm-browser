package com.example.tools;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by THINK on 2016/10/14.
 */
@RealmClass
public class User extends RealmObject {
    private String name;
    private int age;

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

package com.example.think.stetho_realm_browser;

import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by THINK on 2016/10/13.
 */
@RealmClass
public class Book implements RealmModel {
    public String oops;
    @PrimaryKey
    public String id;
    private String title;
    private Date publishDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
}

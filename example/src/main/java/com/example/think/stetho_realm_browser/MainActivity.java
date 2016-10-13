package com.example.think.stetho_realm_browser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Date;
import java.util.Random;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    public static final Random random = new Random();
    private Realm realm1;
    private Realm realm2;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm1.close();
        realm2.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realm1 = Realm.getInstance(RealmSchemas.SCHEMA_1);
        realm2 = Realm.getInstance(RealmSchemas.SCHEMA_2);
    }

    public void onClick(View view) {

        realm1.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                User user = new User();
                user.setName(Names.FIRST_NAME[random.nextInt(Names.FIRST_NAME.length)]
                        + "    " + Names.LAST_NAME[random.nextInt(Names.LAST_NAME.length)]);
                user.setAge(random.nextInt(100));
                user.setUuid(Integer.toString(random.nextInt(10000)));
                realm.copyToRealm(user);

                Book book = new Book();
                book.setPublishDate(new Date());
                book.setTitle(Names.LAST_NAME[random.nextInt(Names.LAST_NAME.length)]);
                realm.copyToRealm(book);
            }
        });

        Log.d("USER1", Long.toString(realm1.where(User.class).count()));
        realm2.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                User user = new User();
                user.setName(Names.FIRST_NAME[random.nextInt(Names.FIRST_NAME.length)]
                        + "    " + Names.LAST_NAME[random.nextInt(Names.LAST_NAME.length)]);
                user.setAge(random.nextInt(100));
                user.setUuid(Integer.toString(random.nextInt(10000)));
                realm.copyToRealm(user);
                Book book = new Book();
                book.setPublishDate(new Date());
                book.setTitle(Names.LAST_NAME[random.nextInt(Names.LAST_NAME.length)]);
                realm.copyToRealm(book);
            }
        });

        Log.d("USER2", Long.toString(realm2.where(User.class).count()));
    }
}

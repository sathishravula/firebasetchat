package com.personal.firebase;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class CustomApplication extends Application {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
//        Realm.init(this); //initialize other plugins

    }
}
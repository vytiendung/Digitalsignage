package com.five9.admin.digitalsignage;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static MyApplication getIntance(){
        return instance;
    }
}

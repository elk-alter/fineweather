package com.example.fineweather.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

//用于获取context
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext() {
        return context;
    }
}

package com.example.crowdtest;

import android.app.Application;

import timber.log.Timber;

public class AppController extends Application {
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        Timber.plant(new FileLoggingTree());
    }

    public static AppController getInstance() {
        return mInstance;
    }
}

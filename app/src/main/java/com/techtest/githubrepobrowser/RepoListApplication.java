package com.techtest.githubrepobrowser;

import android.app.Application;

import io.realm.Realm;

public class RepoListApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
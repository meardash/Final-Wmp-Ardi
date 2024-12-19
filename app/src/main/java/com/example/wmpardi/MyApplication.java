package com.example.wmpardi;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase when the app starts
        FirebaseApp.initializeApp(this);
    }
}

package com.example.medibook;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MedibookApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}

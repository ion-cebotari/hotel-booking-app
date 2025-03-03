package com.example.hotel_v2;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class HotelApplication extends Application {
    private static final String TAG = "HotelApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
        }
    }
} 
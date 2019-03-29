package com.example.mitm.features

import android.app.Application
import com.facebook.stetho.Stetho

public class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}
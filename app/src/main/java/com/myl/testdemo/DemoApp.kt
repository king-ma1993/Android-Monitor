package com.myl.testdemo

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class DemoApp :Application(){
    override fun onCreate() {
        super.onCreate()
        // 开启暗黑模式
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
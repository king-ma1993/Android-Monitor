package com.myl.testdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.myl.testdemo.databinding.TestActivityBinding

class TestActivity : AppCompatActivity(){

    private lateinit var binding: TestActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myl","TestActivity onCreate:${Log.getStackTraceString(Throwable()) }")
        binding = TestActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPause() {
        super.onPause()
        Log.d("myl","TestActivity onPause:${Log.getStackTraceString(Throwable()) }")
    }

    override fun onResume() {
        super.onResume()
        Log.d("myl","TestActivity onResume:${Log.getStackTraceString(Throwable()) }")
    }
}
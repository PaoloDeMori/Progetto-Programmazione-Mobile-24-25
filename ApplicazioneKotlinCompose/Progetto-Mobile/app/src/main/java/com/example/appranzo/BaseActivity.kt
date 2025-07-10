package com.example.appranzo

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity

open class BaseActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

package com.instadown

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InstaDownApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

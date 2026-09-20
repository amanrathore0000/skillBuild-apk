package com.skillbuilder.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SkillBuilderApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

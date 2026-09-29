package com.skillbuilder.app

import android.app.Application
import com.skillbuilder.app.data.local.SearchHistoryManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SkillBuilderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SearchHistoryManager.initialize(this)
    }
}

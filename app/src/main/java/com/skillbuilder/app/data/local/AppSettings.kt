package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    ENGLISH,
    HINDI
}

object AppSettings {
    private const val PREFS_NAME = "skillbuilder_settings"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_LANGUAGE = "language"

    private lateinit var prefs: SharedPreferences

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        _themeMode.value = runCatching { AppThemeMode.valueOf(savedTheme ?: AppThemeMode.SYSTEM.name) }
            .getOrDefault(AppThemeMode.SYSTEM)

        val savedLang = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)
        _language.value = runCatching { AppLanguage.valueOf(savedLang ?: AppLanguage.ENGLISH.name) }
            .getOrDefault(AppLanguage.ENGLISH)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
        }
    }
}

@Composable
fun tr(en: String, hi: String): String {
    val lang by AppSettings.language.collectAsState()
    return if (lang == AppLanguage.HINDI) hi else en
}

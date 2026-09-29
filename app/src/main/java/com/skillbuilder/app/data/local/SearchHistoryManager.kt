package com.skillbuilder.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

object SearchHistoryManager {
    private const val PREFS_NAME = "skillbuilder_search_history"
    private const val KEY_SEARCH_HISTORY = "search_history_items"
    private const val KEY_CLEARED_FLAG = "has_user_cleared_history"

    private var prefs: SharedPreferences? = null

    private val initialSampleSearches = listOf(
        "Android Jetpack Compose",
        "Business English Workplace",
        "Data Analytics",
        "Acoustic Guitar Basics",
        "Stanford Writing"
    )

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    @Synchronized
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        val sp = prefs ?: return
        val hasCleared = sp.getBoolean(KEY_CLEARED_FLAG, false)
        val savedJson = sp.getString(KEY_SEARCH_HISTORY, null)

        if (savedJson != null) {
            val list = mutableListOf<String>()
            try {
                val jsonArray = JSONArray(savedJson)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optString(i)
                    if (item.isNotBlank()) {
                        list.add(item)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _searchHistory.value = list
        } else if (!hasCleared) {
            // First time ever, not yet cleared: populate with initial searches
            _searchHistory.value = initialSampleSearches
            persistList(initialSampleSearches)
        } else {
            // User had previously clicked "Clear All" -> keep it empty, do not show again
            _searchHistory.value = emptyList()
        }
    }

    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = _searchHistory.value.toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)

        val updated = current.take(15)
        _searchHistory.value = updated
        persistList(updated)
    }

    fun removeSearchQuery(query: String) {
        val current = _searchHistory.value.toMutableList()
        val removed = current.removeAll { it.equals(query, ignoreCase = true) }
        if (removed) {
            _searchHistory.value = current
            persistList(current)
            if (current.isEmpty()) {
                prefs?.edit()?.putBoolean(KEY_CLEARED_FLAG, true)?.apply()
            }
        }
    }

    fun clearAll() {
        _searchHistory.value = emptyList()
        prefs?.edit()
            ?.putString(KEY_SEARCH_HISTORY, "[]")
            ?.putBoolean(KEY_CLEARED_FLAG, true)
            ?.apply()
    }

    private fun persistList(list: List<String>) {
        val sp = prefs ?: return
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        sp.edit().putString(KEY_SEARCH_HISTORY, jsonArray.toString()).apply()
    }
}

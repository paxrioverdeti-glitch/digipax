package com.example.pxrioverde.util

import android.content.Context
import android.content.SharedPreferences

actual object LocalStorage {
    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (::prefs.isInitialized) prefs.getBoolean(key, defaultValue) else defaultValue
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return if (::prefs.isInitialized) prefs.getString(key, defaultValue) ?: defaultValue else defaultValue
    }

    actual fun clear() {
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }
}

package com.example.pxrioverde.util

import java.util.prefs.Preferences

actual object LocalStorage {
    private val prefs = Preferences.userNodeForPackage(LocalStorage::class.java)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.get(key, defaultValue)
    }

    actual fun clear() {
        prefs.clear()
    }
}

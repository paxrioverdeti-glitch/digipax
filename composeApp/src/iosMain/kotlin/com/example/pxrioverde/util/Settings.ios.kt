package com.example.pxrioverde.util

import platform.Foundation.NSUserDefaults

actual object LocalStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, key)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (userDefaults.objectForKey(key) != null) {
            userDefaults.boolForKey(key)
        } else {
            defaultValue
        }
    }

    actual fun putString(key: String, value: String) {
        userDefaults.setObject(value, key)
    }

    actual fun getString(key: String, defaultValue: String): String {
        return userDefaults.stringForKey(key) ?: defaultValue
    }

    actual fun clear() {
        val dictionary = userDefaults.dictionaryRepresentation()
        dictionary.keys.forEach { key ->
            userDefaults.removeObjectForKey(key as String)
        }
        userDefaults.synchronize()
    }
}

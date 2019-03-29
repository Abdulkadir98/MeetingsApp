package com.example.mitm.features.utils

import android.content.Context
import kotlin.reflect.KProperty


object DelegateExt {

    val ACCESS_TOKEN = "accessToken"
    val DEFAULT_TOKEN = ""

    fun stringPreference(context: Context, name: String, default: String) =
            StringPreference(context, name, default)

    fun intPreference(context: Context, id: String, default: Int) = IntPreference(context, id, default)

    fun booleanPreference(context: Context, name: String, default: Boolean) = BooleanPreference(context, name, default)

    class BooleanPreference(val context: Context, val name: String, val default: Boolean) {
        val prefs by lazy {
            context.getSharedPreferences("default", Context.MODE_PRIVATE)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return prefs.getBoolean(name, default)!!
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            prefs.edit().putBoolean(name, value).apply()
        }
    }
}

    class StringPreference(val context: Context, val name: String, val default: String) {

        val prefs by lazy {
            context.getSharedPreferences("default", Context.MODE_PRIVATE)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return prefs.getString(name, default)!!
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            prefs.edit().putString(name, value).apply()
        }

    }

class IntPreference(val context: Context, val id: String, val default: Int) {

    val prefs by lazy {
        context.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return prefs.getInt(id, default)!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        prefs.edit().putInt(id, value).apply()
    }
}

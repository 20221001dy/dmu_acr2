package com.dmu.dmu_app.util

import android.content.Context

object AuthManager {

    fun saveUserInfo(context: Context, userCode: Int, userName: String) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("user_code", userCode)
            .putString("user_name", userName)
            .apply()
    }

    fun getUserCode(context: Context): Int {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getInt("user_code", -1)
    }

    fun getUserName(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("user_name", null)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.contains("user_code")
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

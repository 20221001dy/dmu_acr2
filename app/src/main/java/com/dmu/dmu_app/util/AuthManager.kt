package com.dmu.dmu_app.util

import android.content.Context
import android.util.Log
import com.dmu.dmu_app.network.SupabaseClient

object AuthManager {

    fun login(context: Context, userId: String, userName: String, usercode: Int) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isLoggedIn", true)
            .putString("userId", userId)
            .putString("username", userName)
            .putInt("usercode", usercode) // ✅ usercode 저장
            .apply()
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("userId", null)
    }

    fun getUserName(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("username", null)
    }

    fun getUserCode(context: Context): Int {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getInt("usercode", -1) // ✅ Int로 가져오고 -1 기본값
    }

    suspend fun loginWithUserId(context: Context, userId: String): Boolean {
        return try {
            val response = SupabaseClient.instance.getUserByUserId("eq.$userId")

            Log.d("로그인", "입력한 userId: $userId")
            Log.d("로그인", "응답코드: ${response.code()}, body: ${response.body()}")

            if (response.isSuccessful) {
                val userList = response.body()
                if (!userList.isNullOrEmpty()) {
                    val user = userList[0]
                    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("userId", user.userId)
                        .putString("username", user.username)
                        .putInt("usercode", user.usercode) // ✅ 저장
                        .apply()
                    true
                } else false
            } else false
        } catch (e: Exception) {
            false
        }
    }
}

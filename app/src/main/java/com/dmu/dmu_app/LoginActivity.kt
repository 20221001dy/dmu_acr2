package com.dmu.dmu_app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.dmu.dmu_app.util.AuthManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToSignup: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🧱 툴바 & 드로어 세팅
        toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.login_drawer_layout)
        navView = findViewById(R.id.login_nav_view)

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.app_name, R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_login -> {
                    Toast.makeText(this, "이미 로그인 화면입니다", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_signup -> {
                    startActivity(Intent(this, SignupActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // 🔐 로그인 UI
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToSignup = findViewById(R.id.btnGoToSignup)

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                val url = "https://bbmkqlpteslejzhflivi.supabase.co/rest/v1/user" +
                        "?user_id=eq.${email}&user_pw=eq.${password}"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM")
                    .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM")
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    if (jsonArray.length() > 0) {
                        val userJson = jsonArray.getJSONObject(0)
                        val userCode = userJson.getInt("user_code")
                        val userName = userJson.getString("user_name")

                        AuthManager.saveUserInfo(this@LoginActivity, userCode, userName)

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "서버 오류: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

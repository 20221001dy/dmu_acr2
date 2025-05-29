package com.dmu.dmu_app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editName: EditText
    private lateinit var btnSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        editEmail = findViewById(R.id.editSignupEmail)
        editPassword = findViewById(R.id.editSignupPassword)
        editName = findViewById(R.id.editSignupName)
        btnSignup = findViewById(R.id.btnSignup)

        btnSignup.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val name = editName.text.toString()

            if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) {
                signup(email, password, name)
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signup(email: String, password: String, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url = "https://bbmkqlpteslejzhflivi.supabase.co/rest/v1/user"

                val json = JSONObject()
                json.put("user_id", email)
                json.put("user_pw", password)
                json.put("user_name", name)

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM")
                    .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@SignupActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SignupActivity, "회원가입 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SignupActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

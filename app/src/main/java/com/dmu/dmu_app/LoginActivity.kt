package com.dmu.dmu_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dmu.dmu_app.network.SupabaseClient
import com.dmu.dmu_app.util.AuthManager
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_login)

        // UI 연결
        editEmail = findViewById(R.id.editLoginEmail)
        editPassword = findViewById(R.id.editLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)

        // 이메일 로그인
        btnLogin.setOnClickListener {
            val userId = editEmail.text.toString()
            if (userId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = AuthManager.loginWithUserId(this@LoginActivity, userId)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패: 사용자 없음", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 구글 로그인
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // firebase 콘솔에서 확인
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 9001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "구글 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val email = user?.email ?: ""
                    val name = user?.displayName ?: "이름없음"
                    val googleKey = user?.uid ?: ""

                    Log.d("GoogleLogin", "✅ 구글 인증 성공 → uid: $googleKey, email: $email, name: $name")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = SupabaseClient.instance.getUserByGoogleKey("eq.$googleKey")
                            if (response.isSuccessful) {
                                val userList = response.body()
                                if (!userList.isNullOrEmpty()) {
                                    val user = userList[0]

                                    Log.d("GoogleLogin", "✅ 기존 유저 로그인")

                                    withContext(Dispatchers.Main) {
                                        // ✅ usercode 추가
                                        AuthManager.login(this@LoginActivity, googleKey, name, user.usercode)
                                        Toast.makeText(this@LoginActivity, "구글 로그인 성공!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    }
                                } else {
                                    Log.d("GoogleLogin", "❌ 유저 없음 - 회원가입 필요")
                                    // 필요 시 신규 유저 insert 로직 수행
                                }
                            } else {
                                Log.e("GoogleLogin", "⛔ 오류 코드: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.e("GoogleLogin", "⛔ 예외 발생: ${e.message}")
                                Toast.makeText(this@LoginActivity, "Supabase 연동 오류: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                } else {
                    Toast.makeText(this, "파이어베이스 인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

}

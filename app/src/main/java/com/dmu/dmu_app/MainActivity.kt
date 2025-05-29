package com.dmu.dmu_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dmu.dmu_app.util.AcrCloudRepository
import com.dmu.dmu_app.util.AuthManager
import com.dmu.dmu_app.ui.helper.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: String? = null
    private lateinit var btnRecord: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    private val REQUEST_PERMISSION_CODE = 1
    private val accessKey = "8b115fcf9f2a03cec7a2d3e7916aeed1"
    private val accessSecret = "Poxw8zt1NoiTHDAM8zShvuXZ9Vn4Aq4doaUZVvHB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 컴포넌트 연결
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnRecord = findViewById(R.id.btnRecord)

        // Navigation Drawer 초기화
        NavigationDrawerHelper.setup(this, drawerLayout, navView, toolbar)

        // 권한 확인 및 녹음 버튼 설정
        checkPermissions()

        btnRecord.setOnClickListener {
            val isLoggedIn = AuthManager.isLoggedIn(this)
            if (!isLoggedIn) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            if (isRecording) stopRecording() else startRecording()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecording() {
        val cacheDir = externalCacheDir ?: cacheDir
        val audioFile = File(cacheDir, "audio_record.amr")

        // ✅ 디렉토리 없으면 생성
        if (!audioFile.parentFile.exists()) {
            audioFile.parentFile.mkdirs()
        }

        outputFile = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        isRecording = true
        btnRecord.text = "녹음 중지"
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }
        isRecording = false
        btnRecord.text = "녹음 시작"

        outputFile?.let {
            val file = File(it)
            AcrCloudRepository.identifySongFromFile(
                context = this,
                audioFile = file,
                accessKey = accessKey,
                accessSecret = accessSecret
            )
        }
    }
}

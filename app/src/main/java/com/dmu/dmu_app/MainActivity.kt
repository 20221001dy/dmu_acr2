package com.dmu.dmu_app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dmu.dmu_app.ui.helper.NavigationDrawerHelper
import com.dmu.dmu_app.util.*
import com.google.android.material.navigation.NavigationView
import java.io.File

class MainActivity : AppCompatActivity(), RecordingListener {
    private lateinit var btnRecord: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var audioRecorder: AudioRecorder

    private val accessKey = "8b115fcf9f2a03cec7a2d3e7916aeed1"
    private val accessSecret = "Poxw8zt1NoiTHDAM8zShvuXZ9Vn4Aq4doaUZVvHB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioRecorder = AudioRecorder(this)

        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnRecord = findViewById(R.id.btnRecord)

        NavigationDrawerHelper.setup(this, drawerLayout, navView, toolbar)
        PermissionManager.checkAndRequestAudioPermission(this)

        btnRecord.setOnClickListener {
            if (!AuthManager.isLoggedIn(this)) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            if (audioRecorder.isRecording) {
                audioRecorder.stop(this)
            } else {
                audioRecorder.start(this)
            }
            updateRecordButtonUI()
        }
    }

    private fun updateRecordButtonUI() {
        btnRecord.text = if (audioRecorder.isRecording) "녹음 중지" else "녹음 시작"
    }

    override fun onRecordingFinished(file: File) {
        Toast.makeText(this, "녹음 완료! 노래를 찾고 있습니다...", Toast.LENGTH_SHORT).show()
        updateRecordButtonUI()

        AcrCloudRepository.identifySongFromFile(
            context = this,
            audioFile = file,
            accessKey = accessKey,
            accessSecret = accessSecret
        )
    }

    override fun onRecordingFailed(exception: Exception) {
        Toast.makeText(this, "녹음에 실패했습니다: ${exception.message}", Toast.LENGTH_SHORT).show()
        updateRecordButtonUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
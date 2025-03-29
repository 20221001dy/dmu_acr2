package com.dmu.dmu_app

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import com.dmu.dmu_app.network.RetrofitClient

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: String? = null
    private lateinit var btnRecord: Button

    private val REQUEST_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("DebugCheck", "앱이 시작됨!")

        btnRecord = findViewById(R.id.btnRecord)

        checkPermissions()

        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        outputFile = "${externalCacheDir?.absolutePath}/audio_record.amr"
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
            Log.e("RecordingError", "Error stopping the recording: ${e.message}")
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }

        isRecording = false
        btnRecord.text = "녹음 시작"

        outputFile?.let {
            sendAudioToACRCloud(it)
        }
    }

    private fun sendAudioToACRCloud(audioFile: String) {
        val file = File(audioFile)
        val requestFile = file.asRequestBody("audio/amr".toMediaType())
        val audioPart = MultipartBody.Part.createFormData("sample", file.name, requestFile)

        val accessKeyPart = MultipartBody.Part.createFormData("access_key", "f097c66b65b074195d647b51a2251dd1")

        // 디버깅을 위한 로그 추가
        Log.d("ACRCloudRequest", "파일 경로: $audioFile")
        Log.d("ACRCloudRequest", "파일 크기: ${file.length()} bytes")
        Log.d("ACRCloudRequest", "파일 이름: ${file.name}")

        RetrofitClient.instance.identifySong(accessKeyPart, audioPart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()?.string()
                        Log.d("ACRCloudResponse", "API Response: $responseData")
                    } else {
                        Log.e("ACRCloudResponse", "API Error: ${response.code()} - ${response.message()}")
                        val errorBody = response.errorBody()?.string()
                        Log.e("ACRCloudError", "Error Body: $errorBody")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("ACRCloudResponse", "Network Error: ${t.message}")
                }
            })
    }
}

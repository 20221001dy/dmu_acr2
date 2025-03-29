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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import com.dmu.dmu_app.network.RetrofitClient
import com.dmu.dmu_app.network.ACRCloudApi

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: String? = null
    private lateinit var btnRecord: Button

    private val REQUEST_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRecord = findViewById(R.id.btnRecord)

        // 마이크 권한 확인
        checkPermissions()

        // 녹음 버튼 클릭시 녹음 시작 or 중지
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    // 마이크 권한 확인 및 요청
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
            }
        }
    }

    // 권한 요청 후 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startRecording() {
        // 녹음 파일 경로 설정
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
        if (mediaRecorder != null) {
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

            // 녹음이 끝난 후, ACR Cloud API에 요청 보내기
            outputFile?.let {
                sendAudioToACRCloud(it)
            }
        } else {
            Log.e("RecordingError", "MediaRecorder is null")
        }
    }

    private fun sendAudioToACRCloud(audioFile: String) {
        val file = File(audioFile)

        // 파일을 multipart/form-data로 변환
        val requestFile = file.asRequestBody("audio/amr".toMediaType())
        val audioPart = MultipartBody.Part.createFormData("sample", file.name, requestFile)

        // 기타 필드도 RequestBody로 생성
        val accessKey = "YOUR_ACCESS_KEY".toRequestBody("text/plain".toMediaType())
        val dataType = "audio".toRequestBody("text/plain".toMediaType())

        // API 요청 보내기
        RetrofitClient.instance.identifySong(accessKey, audioPart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()?.string()
                        Log.d("ACRCloudResponse", "API Response: $responseData")
                    } else {
                        Log.e("ACRCloudResponse", "API Error: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("ACRCloudResponse", "Network Error: ${t.message}")
                }
            })
    }
}
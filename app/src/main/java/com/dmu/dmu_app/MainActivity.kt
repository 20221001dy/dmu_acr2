package com.dmu.dmu_app

import android.os.Build
import android.media.MediaRecorder
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import com.dmu.dmu_app.network.*

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
                // 권한이 거부되었을 경우 처리
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startRecording() {
        // 녹음 파일 경로 설정
        outputFile = "${externalCacheDir?.absolutePath}/audio_record.amr"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // 마이크 소스 설정
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // 파일 형식 설정
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // 오디오 인코더 설정
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
        // MediaRecorder가 초기화되어 있을 때만 실행
        if (mediaRecorder != null) {
            try {
                mediaRecorder?.stop() // 녹음 중지
            } catch (e: IllegalStateException) {
                Log.e("RecordingError", "Error stopping the recording: ${e.message}")
                e.printStackTrace()
            } finally {
                mediaRecorder?.release() // MediaRecorder 객체 해제
                mediaRecorder = null
            }

            isRecording = false
            btnRecord.text = "녹음 시작" // 버튼 텍스트 변경

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

        // 'toMediaType()'를 사용하여 MediaType 생성
        val mediaType = "audio/amr".toMediaType()

        // File을 InputStream으로 읽고 ByteArray로 변환 후, toRequestBody() 사용
        val fileInputStream = file.inputStream()
        val byteArray = fileInputStream.readBytes()
        val requestBody = byteArray.toRequestBody(mediaType)

        // API 요청 보내기
        val authorizationHeader = "f097c66b65b074195d647b51a2251dd1" // 실제 API 키로 변경
        RetrofitClient.instance.identifySong(authorizationHeader, requestBody)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        // 성공적으로 API 호출 시 처리
                        val responseData = response.body()?.string()
                        Log.d("ACRCloudResponse", "API Response: $responseData")  // 로그 출력
                    } else {
                        // 실패 처리
                        Log.e("ACRCloudResponse", "API Error: ${response.code()} - ${response.message()}")  // 오류 로그 출력
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // 네트워크 실패 처리
                    Log.e("ACRCloudResponse", "Network Error: ${t.message}")  // 오류 로그 출력
                }
            })
    }
}

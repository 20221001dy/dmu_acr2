package com.dmu.dmu_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dmu.dmu_app.model.SongInfo
import com.dmu.dmu_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: String? = null
    private lateinit var btnRecord: Button

    private val REQUEST_PERMISSION_CODE = 1
    private val accessKey = "8b115fcf9f2a03cec7a2d3e7916aeed1"
    private val accessSecret = "Poxw8zt1NoiTHDAM8zShvuXZ9Vn4Aq4doaUZVvHB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val signatureVersion = "1"
        val dataType = "audio"
        val file = File(audioFile)
        val sampleBytes = file.length().toString()
        val signature = generateSignature(accessSecret, timestamp, signatureVersion)

        val requestFile = file.asRequestBody("audio/amr".toMediaType())
        val audioPart = MultipartBody.Part.createFormData("sample", file.name, requestFile)
        val accessKeyPart = MultipartBody.Part.createFormData("access_key", accessKey)
        val timestampPart = MultipartBody.Part.createFormData("timestamp", timestamp)
        val signaturePart = MultipartBody.Part.createFormData("signature", signature)
        val signatureVersionPart = MultipartBody.Part.createFormData("signature_version", signatureVersion)
        val dataTypePart = MultipartBody.Part.createFormData("data_type", dataType)
        val sampleBytesPart = MultipartBody.Part.createFormData("sample_bytes", sampleBytes)

        Log.d("ACRCloudRequest", "요청: accessKey=$accessKey, timestamp=$timestamp, signature=$signature")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.identifySong(
                    accessKeyPart,
                    timestampPart,
                    signaturePart,
                    signatureVersionPart,
                    dataTypePart,
                    sampleBytesPart,
                    audioPart
                ).execute()

                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    Log.d("ACRCloudResponse", "API Response: $responseData")

                    val json = JSONObject(responseData ?: "")
                    if (json.has("metadata")) {
                        val metadata = json.getJSONObject("metadata")
                        val hummingArray = metadata.optJSONArray("humming") ?: return@launch

                        if (hummingArray.length() > 0) {
                            val song = hummingArray.getJSONObject(0)

                            val title = song.optString("title", "Unknown")
                            val artistsArray = song.optJSONArray("artists")
                            val artists = if (artistsArray != null && artistsArray.length() > 0) {
                                artistsArray.getJSONObject(0).optString("name", "Unknown")
                            } else {
                                "Unknown"
                            }
                            val album = song.optJSONObject("album")?.optString("name", "Unknown")
                            val label = song.optString("label", "Unknown")
                            val releaseDate = song.optString("release_date", "Unknown")

                            val songInfo = SongInfo(
                                title = title,
                                artists = artists,
                                album = album,
                                label = label,
                                releaseDate = releaseDate
                            )

                            // 화면 전환
                            launch(Dispatchers.Main) {
                                val intent = Intent(this@MainActivity, ResultActivity::class.java)
                                intent.putExtra("songInfo", songInfo)
                                startActivity(intent)
                            }
                        } else {
                            Log.e("ACRCloudResponse", "No songs found in metadata.")
                        }
                    } else {
                        Log.e("ACRCloudResponse", "No metadata found in response.")
                    }
                } else {
                    Log.e("ACRCloudResponse", "API Error: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    Log.e("ACRCloudError", "Error Body: $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("ACRCloudResponse", "HTTP Exception: ${e.message}")
            } catch (e: IOException) {
                Log.e("ACRCloudResponse", "Network Error: ${e.message}")
            } catch (e: Exception) {
                Log.e("ACRCloudResponse", "Unexpected Error: ${e.message}")
            }
        }
    }

    // ✅ 서명 생성 함수 (반드시 필요)
    private fun generateSignature(secret: String, timestamp: String, signatureVersion: String): String {
        val method = "POST"
        val httpUri = "/v1/identify"
        val dataType = "audio"
        val stringToSign = "$method\n$httpUri\n$accessKey\n$dataType\n$signatureVersion\n$timestamp"

        return try {
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA1")
            mac.init(secretKeySpec)
            val rawHmac = mac.doFinal(stringToSign.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(rawHmac, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            Log.e("SignatureError", "Error generating signature: ${e.message}")
            ""
        }
    }
}

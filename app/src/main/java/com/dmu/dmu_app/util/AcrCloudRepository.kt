package com.dmu.dmu_app.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.dmu.dmu_app.ResultActivity
import com.dmu.dmu_app.model.SongInfo
import com.dmu.dmu_app.model.AcrModel
import com.dmu.dmu_app.network.RetrofitClient
import com.dmu.dmu_app.network.SupabaseClient
import com.dmu.dmu_app.util.SignatureUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID

object AcrCloudRepository {

    fun identifySongFromFile(
        context: Context,
        audioFile: File,
        accessKey: String,
        accessSecret: String
    ) {
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val signatureVersion = "1"
        val dataType = "audio"
        val sampleBytes = audioFile.length().toString()
        val signature = SignatureUtil.generateSignature(
            accessKey = accessKey,
            accessSecret = accessSecret,
            timestamp = timestamp,
            signatureVersion = signatureVersion
        )

        val requestFile = audioFile.asRequestBody("audio/amr".toMediaType())
        val audioPart = MultipartBody.Part.createFormData("sample", audioFile.name, requestFile)
        val accessKeyPart = MultipartBody.Part.createFormData("access_key", accessKey)
        val timestampPart = MultipartBody.Part.createFormData("timestamp", timestamp)
        val signaturePart = MultipartBody.Part.createFormData("signature", signature)
        val signatureVersionPart = MultipartBody.Part.createFormData("signature_version", signatureVersion)
        val dataTypePart = MultipartBody.Part.createFormData("data_type", dataType)
        val sampleBytesPart = MultipartBody.Part.createFormData("sample_bytes", sampleBytes)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.identifySong(
                    accessKeyPart, timestampPart, signaturePart,
                    signatureVersionPart, dataTypePart, sampleBytesPart, audioPart
                ).execute()

                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    Log.d("AcrCloudRepository", "API Response: $responseData")

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
                            } else "Unknown"
                            val album = song.optJSONObject("album")?.optString("name", "Unknown")
                            val label = song.optString("label", "Unknown")
                            val releaseDate = song.optString("release_date", "Unknown")

                            // 로그인 유저 정보 확인
                            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                            val usercode = prefs.getInt("usercode", -1)

                            // ✅ 로그인 안 된 경우 저장하지 않고 로그만 출력
                            if (usercode == -1) {
                                Log.e("Supabase", "로그인 안 되어 있어 Supabase 저장 중단됨")
                            } else {
                                val acrModel = AcrModel(
                                    acrid = UUID.randomUUID().toString(),
                                    acr_title = title,
                                    artists = artists,
                                    album = album,
                                    label = label,
                                    release_date = releaseDate,
                                    score = 0.98,
                                    usercode = usercode
                                )

                                Log.d("Supabase", "보내는 AcrModel: $acrModel")

                                launch(Dispatchers.IO) {
                                    try {
                                        val supabaseResponse = SupabaseClient.instance.postAcr(acrModel)
                                        if (supabaseResponse.isSuccessful) {
                                            Log.d("Supabase", " Supabase 저장 성공")
                                        } else {
                                            Log.e("Supabase", " 저장 실패: ${supabaseResponse.code()}")
                                            Log.e("Supabase", "Body: ${supabaseResponse.errorBody()?.string()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Supabase", " 예외 발생: ${e.message}")
                                    }
                                }
                            }

                            // 곡 정보 화면에 넘기기
                            val songInfo = SongInfo(title, artists, album, label, releaseDate)
                            CoroutineScope(Dispatchers.Main).launch {
                                val intent = Intent(context, ResultActivity::class.java)
                                intent.putExtra("songInfo", songInfo)
                                context.startActivity(intent)
                            }
                        }
                    } else {
                        Log.e("AcrCloudRepository", "No metadata found.")
                    }
                } else {
                    Log.e("AcrCloudRepository", "API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AcrCloudRepository", "Error: ${e.message}")
            }
        }
    }
}

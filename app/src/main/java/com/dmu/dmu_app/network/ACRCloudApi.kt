package com.dmu.dmu_app.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface ACRCloudApi {

    @POST("v1/identify") //임마가 엔드포인트라네요~
    fun identifySong(
        @Header("Authorization") authorization: String, // API Key
        @Body requestBody: RequestBody // 요청 데이터 (예: 음성 파일)
    ): Call<ResponseBody> // API 응답
}
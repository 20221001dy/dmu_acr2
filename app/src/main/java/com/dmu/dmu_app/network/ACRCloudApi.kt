package com.dmu.dmu_app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ACRCloudApi {
    @Multipart
    @POST("v1/identify")
    fun identifySong(
        @Part accessKey: MultipartBody.Part,  // 수정됨!
        @Part audio: MultipartBody.Part
    ): Call<ResponseBody>
}
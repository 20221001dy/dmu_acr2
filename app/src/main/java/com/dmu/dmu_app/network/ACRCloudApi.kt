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
        @Part("access_key") accessKey: RequestBody,
        @Part audio: MultipartBody.Part
    ): Call<ResponseBody>
}
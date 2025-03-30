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
        @Part accessKey: MultipartBody.Part,
        @Part timestamp: MultipartBody.Part,
        @Part signature: MultipartBody.Part,
        @Part signatureVersion: MultipartBody.Part,
        @Part dataType: MultipartBody.Part,
        @Part sampleBytes: MultipartBody.Part,
        @Part audioFile: MultipartBody.Part
    ): Call<ResponseBody>
    }
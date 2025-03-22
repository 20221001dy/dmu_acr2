package com.dmu.dmu_app.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val BASE_URL = "https://identify-ap-southeast-1.acrcloud.com"

    val instance: ACRCloudApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 기본 URL 설정
            .addConverterFactory(GsonConverterFactory.create()) // JSON 응답을 처리하기 위해 GsonConverterFactory 사용
            .build() // Retrofit 객체 빌드
            .create(ACRCloudApi::class.java) // ACRCloudApi 인터페이스와 연결
    }

    }
package com.dmu.dmu_app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private const val BASE_URL = "https://bbmkqlpteslejzhflivi.supabase.co/rest/v1/"

    val instance: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}

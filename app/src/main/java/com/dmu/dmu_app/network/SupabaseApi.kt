package com.dmu.dmu_app.network

import com.dmu.dmu_app.model.AcrModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SupabaseApi {
    @Headers(
        "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Content-Type: application/json"
    )
    @POST("acr")
    suspend fun postAcr(@Body data: AcrModel): Response<Void>
}

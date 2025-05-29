package com.dmu.dmu_app.network

import com.dmu.dmu_app.model.AcrModel
import com.dmu.dmu_app.model.UserModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

   
    @Headers(
        "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Content-Type: application/json"
    )
    @POST("acr")
    suspend fun postAcr(@Body data: AcrModel): Response<Void>

    // 일반 로그인용 user_id 조회
    @Headers(
        "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Content-Type: application/json"
    )
    @GET("user?select=*")
    suspend fun getUserByUserId(
        @Query("user_id") userId: String // 호출 시 "eq.아이디" 형태로 넘겨야 함
    ): Response<List<UserModel>>

    //구글 로그인용
    @Headers(
        "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJibWtxbHB0ZXNsZWp6aGZsaXZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQyODQ3OTcsImV4cCI6MjA1OTg2MDc5N30.w-8auzALaB5b9OLTQSjYMqcN6L2grYTcgBFMfcUnPAM",
        "Content-Type: application/json"
    )
    @GET("user?select=*")
    suspend fun getUserByGoogleKey(
        @Query("google_key") googleKey: String // 호출 시 "eq.uid" 형태로 넘겨야 함
    ): Response<List<UserModel>>
}

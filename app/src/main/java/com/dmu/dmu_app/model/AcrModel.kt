package com.dmu.dmu_app.model

import com.google.gson.annotations.SerializedName
import java.util.UUID



data class AcrModel(
    val acrid : String,
    val acr_title: String,
    val artists: String,
    val album: String?,
    val label: String?,
    val release_date: String?,
    val score: Double,
    @SerializedName("user_code")  // Supabase 컬럼명에 맞춰줌
    val usercode: Int
)

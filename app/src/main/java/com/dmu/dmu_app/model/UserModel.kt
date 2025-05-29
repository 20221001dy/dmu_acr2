package com.dmu.dmu_app.model

import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("user_code")
    val usercode: Int,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("user_pw")
    val userpw: String,

    @SerializedName("user_name")
    val username: String,

    @SerializedName("google_key")
    val googleKey: String? = null,

    @SerializedName("kakao_key")
    val kakaoKey: String? = null
)

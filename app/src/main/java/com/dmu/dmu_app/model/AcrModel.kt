package com.dmu.dmu_app.model

data class AcrModel(
    val acrid: String,
    val acr_title: String,
    val artists: String,
    val album: String?,
    val label: String?,
    val release_date: String?,
    val score: Double
)

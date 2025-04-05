package com.dmu.dmu_app.model

import java.io.Serializable

data class SongInfo(
    val title: String,
    val artists: String,
    val album: String?,
    val label: String?,
    val releaseDate: String?
) : Serializable
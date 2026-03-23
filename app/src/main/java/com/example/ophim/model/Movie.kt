package com.example.ophim.model

data class Movie(
    val _id: String?,
    val name: String,
    val slug: String,
    val origin_name: String?,
    val thumb_url: String,
    val poster_url: String?,
    val year: Int?,
    val lang: String?,
    val quality: String?,
    val episode_current: String?
)

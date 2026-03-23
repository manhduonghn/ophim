package com.example.ophim.model

data class HomeResponse(
    val data: HomeData
)

data class HomeData(
    val items: List<Movie>,
    val APP_DOMAIN_CDN_IMAGE: String
)

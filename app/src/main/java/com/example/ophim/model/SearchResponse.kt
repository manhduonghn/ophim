package com.example.ophim.model

data class SearchResponse(
    val status: String,
    val data: SearchData
)

data class SearchData(
    val items: List<Movie>,
    val params: SearchParams,
    val APP_DOMAIN_CDN_IMAGE: String
)

data class SearchParams(
    val keyword: String,
    val pagination: Pagination // Đổi từ các biến lẻ sang object Pagination
)

data class Pagination(
    val totalItems: Int,
    val totalItemsPerPage: Int,
    val currentPage: Int,
    val pageRanges: Int
)

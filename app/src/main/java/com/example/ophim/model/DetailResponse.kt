package com.example.ophim.model

data class DetailResponse(
    val status: String,
    val data: DetailData
)

data class DetailData(
    val item: MovieDetail
)

data class MovieDetail(
    val name: String,
    val content: String,
    val poster_url: String,
    val year: Int?,
    val lang: String?,
    val quality: String?,
    val time: String?,

    val episodes: List<Server>
)

data class Server(
    val server_name: String,
    val server_data: List<Episode>
)

data class Episode(
    val name: String,
    val slug: String,
    val link_m3u8: String
)

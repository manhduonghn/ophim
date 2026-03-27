package com.example.ophim.model

import com.google.gson.annotations.SerializedName

data class DetailResponse(
    val status: String,
    val message: String?,
    val data: DetailData
)

data class DetailData(
    val item: MovieDetail
)

data class MovieDetail(
    val name: String,
    val content: String?,
    
    @SerializedName("poster_url")
    val poster_url: String,

    @SerializedName("thumb_url")
    val thumb_url: String?,

    val year: Int?,
    val lang: String?,
    val quality: String?,
    val time: String?,
    val type: String?,
    val status: String?,

    @SerializedName("episode_current")
    val episode_current: String?, 

    @SerializedName("episode_total")
    val episode_total: String?,

    val episodes: List<Server>
)

data class Server(
    @SerializedName("server_name")
    val server_name: String,
    
    @SerializedName("server_data")
    val server_data: List<Episode>
)

data class Episode(
    val name: String,
    val slug: String,
    
    @SerializedName("link_m3u8")
    val link_m3u8: String,
    
    @SerializedName("link_embed")
    val link_embed: String?
)

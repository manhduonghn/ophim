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

    // ===== BỔ SUNG =====
    val year: Int?,          // năm phát hành
    val lang: String?,      // Vietsub / Lồng tiếng
    val quality: String?,   // HD / CAM / FullHD
    val time: String?,      // thời lượng (vd: 45 phút)

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

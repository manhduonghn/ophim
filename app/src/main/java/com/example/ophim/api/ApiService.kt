package com.example.ophim.api

import com.example.ophim.model.*
import retrofit2.http.*

interface ApiService {

    @GET("v1/api/home")
    suspend fun getHome(): HomeResponse

    @GET("v1/api/tim-kiem")
    suspend fun search(@Query("keyword") keyword: String): HomeResponse

    @GET("v1/api/phim/{slug}")
    suspend fun getDetail(@Path("slug") slug: String): DetailResponse
}

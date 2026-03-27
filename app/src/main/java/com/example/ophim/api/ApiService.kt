package com.example.ophim.api

import com.example.ophim.model.*
import retrofit2.http.*

interface ApiService {
    // Lấy dữ liệu trang chủ (Phim mới cập nhật)
    @GET("v1/api/home")  
    suspend fun getHome(): HomeResponse  

    // Lấy dữ liệu tìm kiếm theo từ khóa
    @GET("v1/api/tim-kiem")
    suspend fun search(
        @Query("keyword") query: String,
        @Query("page") page: Int
    ): SearchResponse
    
    // Lấy chi tiết một bộ phim
    @GET("v1/api/phim/{slug}")  
    suspend fun getDetail(@Path("slug") slug: String): DetailResponse
}

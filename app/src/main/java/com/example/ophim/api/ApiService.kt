package com.example.ophim.api

import com.example.ophim.model.*
import retrofit2.http.*

interface ApiService {

    // --- 1. PHIM TRANG CHỦ & CHI TIẾT ---
    
    @GET("v1/api/home")  
    suspend fun getHome(): HomeResponse  

    @GET("v1/api/phim/{slug}")  
    suspend fun getDetail(@Path("slug") slug: String): DetailResponse

    // --- 2. TÌM KIẾM ---

    @GET("v1/api/tim-kiem")
    suspend fun search(
        @Query("keyword") query: String,
        @Query("page") page: Int
    ): SearchResponse

    // --- 3. LẤY DANH MỤC CHO MENU (Sử dụng FilterResponse) ---

    // Gọi đến: https://ophim1.com/v1/api/the-loai
    @GET("v1/api/the-loai")
    suspend fun getCategories(): FilterResponse

    // Gọi đến: https://ophim1.com/v1/api/quoc-gia
    @GET("v1/api/quoc-gia")
    suspend fun getCountries(): FilterResponse

    // --- 4. LỌC PHIM TỔNG HỢP (Dùng cho FilterActivity) ---

    /**
     * Hàm này cực kỳ linh hoạt, dùng để tải danh sách phim từ bất kỳ path nào.
     * @param fullPath Ví dụ: "v1/api/danh-sach/phim-bo" hoặc "v1/api/the-loai/hanh-dong"
     */
    @GET
    suspend fun getMoviesByPath(
        @Url fullPath: String,
        @Query("page") page: Int
    ): SearchResponse
}

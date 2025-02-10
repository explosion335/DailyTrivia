package com.example.dailytrivia.network

import com.example.dailytrivia.database.CategoryResponse
import retrofit2.http.GET

interface ApiService {
    @GET("api_category.php")
    suspend fun getCategories(): CategoryResponse
}

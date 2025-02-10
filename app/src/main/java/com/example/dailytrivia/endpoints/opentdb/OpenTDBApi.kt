package com.example.dailytrivia.endpoints.opentdb

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.dailytrivia.data.model.TriviaResponse
import com.example.dailytrivia.data.model.TokenResponse
import retrofit2.http.QueryMap

interface OpenTDBApi {
    @GET("api.php")
    suspend fun getQuestions(@QueryMap queryParams: Map<String, String>): TriviaResponse

//    @GET("api_token.php?command=request")
//    fun getToken(): Call<TokenResponse>
//
//    @GET("api_token.php?command=reset")
//    fun resetToken(@Query("token") token: String): Call<TokenResponse>
}

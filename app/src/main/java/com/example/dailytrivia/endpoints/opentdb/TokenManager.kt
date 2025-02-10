//package com.example.dailytrivia.endpoints.opentdb
//
//class TokenManager {
//    suspend fun fetchToken(): String {
//        val response = RetrofitInstance.api.getToken().execute()
//        return if (response.isSuccessful && response.body()?.responseCode == 0) {
//            response.body()?.token.orEmpty()
//        } else {
//            throw Exception("Failed to fetch token")
//        }
//    }
//
//    suspend fun resetToken(token: String): String {
//        val response = RetrofitInstance.api.resetToken(token).execute()
//        return if (response.isSuccessful && response.body()?.responseCode == 0) {
//            response.body()?.token.orEmpty()
//        } else {
//            throw Exception("Failed to reset token")
//        }
//    }
//
//}
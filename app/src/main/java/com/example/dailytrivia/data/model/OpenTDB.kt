package com.example.dailytrivia.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class TriviaResponse(
    @Json(name = "response_code") val responseCode: Int,
    val results: List<Question>
) : Parcelable


@Parcelize
data class Question(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    @Json(name = "correct_answer") val correctAnswer: String,
    @Json(name = "incorrect_answers") val incorrectAnswers: List<String>
) : Parcelable

data class TokenResponse(
    @Json(name = "response_code") val responseCode: Int,
    val token: String
)

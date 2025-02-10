package com.example.dailytrivia.database

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize


@Parcelize
data class CategoryResponse(
    @Json(name = "trivia_categories") val triviaCategories: List<Category>
) : Parcelable

@Parcelize
data class Category(
    val id: Int,
    val name: String
) : Parcelable
package com.example.dailytrivia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trivia_responses")
data class TriviaResponseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timestamp: Long
)

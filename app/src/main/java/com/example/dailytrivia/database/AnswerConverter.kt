package com.example.dailytrivia.database

import androidx.room.TypeConverter
// https://stackoverflow.com/a/44615752
class AnswerConverter {

    @TypeConverter
    fun storedStringToAnswers(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun answersToStoredString(answers: List<String>): String {
        return answers.joinToString(",")
    }
}

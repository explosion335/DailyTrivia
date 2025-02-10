package com.example.dailytrivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.dailytrivia.database.TriviaResponseDao

class TriviaViewModel(private val triviaResponseDao: TriviaResponseDao) : ViewModel() {

    val allTriviaResponses = triviaResponseDao.getAllTriviaResponses().asLiveData()
    val correctResponseCount = triviaResponseDao.getCorrectResponseCount().asLiveData()
    val totalResponseCount = triviaResponseDao.getTotalResponseCount().asLiveData()

    fun getAccuracy(): Float {
        val correct = correctResponseCount.value ?: 0
        val total = totalResponseCount.value ?: 0
        return if (total > 0) correct.toFloat() / total else 0f
    }
}

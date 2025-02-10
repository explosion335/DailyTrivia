package com.example.dailytrivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dailytrivia.database.TriviaResponseDao

class TriviaViewModelFactory(
    private val triviaResponseDao: TriviaResponseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TriviaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TriviaViewModel(triviaResponseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

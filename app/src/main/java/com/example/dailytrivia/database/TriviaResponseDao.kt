package com.example.dailytrivia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriviaResponseDao {

    @Insert
    suspend fun insert(response: TriviaResponseEntity)

    @Query("SELECT * FROM trivia_responses ORDER BY timestamp DESC")
    fun getAllTriviaResponses(): Flow<List<TriviaResponseEntity>>

    @Query("SELECT COUNT(*) FROM trivia_responses WHERE isCorrect = 1")
    fun getCorrectResponseCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trivia_responses")
    fun getTotalResponseCount(): Flow<Int>
}

package com.example.dailytrivia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Query("SELECT * FROM profile WHERE userId = :userId")
    suspend fun getProfilesByUserId(userId: Int): List<ProfileEntity>

    @Query("SELECT * FROM profile WHERE id = :profileId LIMIT 1")
    suspend fun getProfileByProfileID(profileId: Int): ProfileEntity?

    @Query("SELECT id FROM profile WHERE userId = :userId")
    suspend fun getProfileIdByUserId(userId: Int): List<Int>

    @Query("UPDATE profile SET numberOfQuestions = :numberOfQuestions, difficulty = :difficulty, type = :type, encoding = :encoding, categoryId = :categoryId WHERE id= :id")
    suspend fun updateProfileByUserId(
        id: Int,
//        profileName: String,
        numberOfQuestions: Int,
        difficulty: String,
        type: String,
        encoding: String,
        categoryId: Int
    )
}


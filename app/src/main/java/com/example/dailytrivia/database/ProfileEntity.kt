package com.example.dailytrivia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "profile",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
        )
    ]
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    var numberOfQuestions: Int,
    var difficulty: String,
    var type: String,
    var encoding: String,
    var categoryId: Int
)
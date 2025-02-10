package com.example.dailytrivia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dailytrivia.database.ProfileEntity
import com.example.dailytrivia.database.ProfileDao
import com.example.dailytrivia.database.UserDao
import com.example.dailytrivia.database.UserEntity
import com.example.dailytrivia.database.CategoryDao
import com.example.dailytrivia.database.CategoryEntity
import com.example.dailytrivia.database.TriviaResponseDao
import com.example.dailytrivia.database.TriviaResponseEntity

@Database(
    entities = [ProfileEntity::class, UserEntity::class, CategoryEntity::class, TriviaResponseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun triviaResponseDao(): TriviaResponseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_trivia_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
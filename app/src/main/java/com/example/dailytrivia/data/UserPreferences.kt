package com.example.dailytrivia.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for accessing DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    // Access DataStore
    private val dataStore = context.dataStore

    companion object {
        private val NUMBER_OF_QUESTIONS_KEY = intPreferencesKey("number_of_questions")
        private val DIFFICULTY_KEY = stringPreferencesKey("difficulty")
        private val TYPE_KEY = stringPreferencesKey("type")
        private val ENCODING_KEY = stringPreferencesKey("encoding")
        private val SELECTED_CATEGORIES_KEY = stringSetPreferencesKey("selected_categories")

        // Default values
        private const val DEFAULT_NUMBER_OF_QUESTIONS = 10
        private const val DEFAULT_DIFFICULTY = "any"
        private const val DEFAULT_TYPE = "any"
        private const val DEFAULT_ENCODING = "url3986"
        private val DEFAULT_SELECTED_CATEGORIES = emptySet<String>()
    }

    val numberOfQuestions: Flow<Int> = dataStore.data.map { preferences ->
        preferences[NUMBER_OF_QUESTIONS_KEY] ?: DEFAULT_NUMBER_OF_QUESTIONS
    }

    val difficulty: Flow<String> = dataStore.data.map { preferences ->
        preferences[DIFFICULTY_KEY] ?: DEFAULT_DIFFICULTY
    }

    val type: Flow<String> = dataStore.data.map { preferences ->
        preferences[TYPE_KEY] ?: DEFAULT_TYPE
    }

    val encoding: Flow<String> = dataStore.data.map { preferences ->
        preferences[ENCODING_KEY] ?: DEFAULT_ENCODING
    }

    val selectedCategories: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SELECTED_CATEGORIES_KEY] ?: DEFAULT_SELECTED_CATEGORIES
    }

    // Save preferences
    suspend fun saveNumberOfQuestions(value: Int) = savePreference(NUMBER_OF_QUESTIONS_KEY, value)

    suspend fun saveDifficulty(value: String) = savePreference(DIFFICULTY_KEY, value)

    suspend fun saveType(value: String) = savePreference(TYPE_KEY, value)

    suspend fun saveEncoding(value: String) = savePreference(ENCODING_KEY, value)

    suspend fun saveSelectedCategories(categories: Set<String>) = savePreference(SELECTED_CATEGORIES_KEY, categories)

    // Generic function for saving preferences
    private suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}

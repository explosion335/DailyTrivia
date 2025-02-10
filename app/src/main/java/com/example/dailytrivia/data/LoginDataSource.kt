package com.example.dailytrivia.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dailytrivia.data.model.LoggedInUser
import com.example.dailytrivia.database.UserDao
import com.example.dailytrivia.database.UserEntity
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import kotlin.random.Random
import java.io.IOException

class LoginDataSource(
    context: Context,
    private val userDao: UserDao,
    private val argon2Kt: Argon2Kt = Argon2Kt()
) {

    companion object {
        private const val MEMORY_COST_KIB = 65536
        private const val ITERATIONS = 5
        private const val PARALLELISM = 2
        private const val SALT_LENGTH_BYTES = 16
        private const val PREFS_NAME = "user_session"
        private const val PREFS_KEY_USER_ID = "user_id"
        private const val PREFS_KEY_USERNAME = "username"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val user = userDao.getUserByUsername(username)

            val loggedInUser = if (user != null) {
                // Verify the password against the stored hash
                if (verifyPassword(user.hashedPassword, password)) {
                    LoggedInUser(user.id.toString(), user.username)
                } else {
                    return Result.Error(Exception("Invalid credentials"))
                }
            } else {
                // Create a new user with a hashed password
                val hashedPassword = hashPassword(password)
                val newUser = UserEntity(
                    id = 0, // Auto-increment
                    username = username,
                    hashedPassword = hashedPassword
                )
                val userId = userDao.insertUser(newUser)
                LoggedInUser(userId.toString(), username)
            }

            // Save session to SharedPreferences
            saveUserSession(loggedInUser)

            Result.Success(loggedInUser)
        } catch (e: Exception) {
            Result.Error(IOException("Authentication failed", e))
        }
    }

    fun logout() {
        // Clear session from SharedPreferences
        clearUserSession()
    }

    fun getLoggedInUser(): LoggedInUser? {
        val userId = sharedPreferences.getString(PREFS_KEY_USER_ID, null)
        val username = sharedPreferences.getString(PREFS_KEY_USERNAME, null)
        return if (userId != null && username != null) {
            LoggedInUser(userId, username)
        } else {
            null
        }
    }

    private fun saveUserSession(user: LoggedInUser) {
        sharedPreferences.edit()
            .putString(PREFS_KEY_USER_ID, user.userId)
            .putString(PREFS_KEY_USERNAME, user.displayName)
            .apply()
    }

    private fun clearUserSession() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }

    private fun hashPassword(password: String): String {
        // Generate a random salt
        val salt = Random.Default.nextBytes(SALT_LENGTH_BYTES)

        // Hash the password
        val hashResult = argon2Kt.hash(
            mode = Argon2Mode.ARGON2_I,
            password = password.toByteArray(),
            salt = salt,
            tCostInIterations = ITERATIONS,
            mCostInKibibyte = MEMORY_COST_KIB,
            parallelism = PARALLELISM
        )

        // Return the encoded output as a string
        return hashResult.encodedOutputAsString()
    }

    private fun verifyPassword(hashedPassword: String, password: String): Boolean {
        return try {
            argon2Kt.verify(
                mode = Argon2Mode.ARGON2_I,
                encoded = hashedPassword,
                password = password.toByteArray() // Convert password to ByteArray
            )
        } catch (_: Exception) {
            false
        }
    }
}

package com.example.dailytrivia

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.transition.TransitionManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.dailytrivia.adapter.NavPagerAdapter
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.database.Category
import com.example.dailytrivia.database.CategoryEntity
import com.example.dailytrivia.database.CategoryResponse
import com.example.dailytrivia.database.ProfileEntity
import com.example.dailytrivia.database.TriviaResponseEntity
import com.example.dailytrivia.database.UserEntity
import com.example.dailytrivia.endpoints.opentdb.RetrofitInstance
import com.example.dailytrivia.network.ApiService
import com.example.dailytrivia.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("daily_trivia_prefs", MODE_PRIVATE)
        appDatabase = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch {
            val userDao = appDatabase.userDao()
            val existingTestUser = userDao.getUserByUsername("t")

            if (existingTestUser == null) {
                val testUser = UserEntity(
                    id = 1,
                    username = "t",
                    hashedPassword = "\$argon2i\$v=19\$m=65536,t=5,p=2\$npLXLRT78KStUyylaKG76g\$oM//7jd63s4ET/XjXLH+5MdrhINi1g7xWLQScWTeLds"
                )
                userDao.insertUser(testUser)

                val testProfile = ProfileEntity(
                    userId = testUser.id,
                    numberOfQuestions = 10,
                    difficulty = "any",
                    type = "any",
                    encoding = "any",
                    categoryId = 9
                )
                appDatabase.profileDao().insertProfile(testProfile)

                val testTriviaResponses = listOf(
                    TriviaResponseEntity(
                        question = "In Magic: The Gathering, what term for blocking was established in the Portal set?",
                        userAnswer = "Intercepting",
                        correctAnswer = "Intercepting",
                        isCorrect = true,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "What does film maker Dan Bell typically focus his films on?",
                        userAnswer = "Action Films",
                        correctAnswer = "Abandoned Buildings and Dead Malls",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "Who was the lead singer and frontman of rock band R.E.M?",
                        userAnswer = "Chris Martin",
                        correctAnswer = "Michael Stipe",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "Excluding their instructor, how many members of Class VII are there in the game 'Legend of Heroes: Trails of Cold Steel'?",
                        userAnswer = "9",
                        correctAnswer = "9",
                        isCorrect = true,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "The communication protocol NFC stands for Near-Field Control.",
                        userAnswer = "True",
                        correctAnswer = "False",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "The most frequent subconscious activity repeated by the human body is blinking.",
                        userAnswer = "True",
                        correctAnswer = "False",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "What is the name of a rabbit's abode?",
                        userAnswer = "Den",
                        correctAnswer = "Burrow",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "What was the name of the Secret Organization in the Hotline Miami series?",
                        userAnswer = "USSR's Blessings",
                        correctAnswer = "50 Blessings",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "Who directed 'E.T. the Extra-Terrestrial' (1982)?",
                        userAnswer = "James Cameron",
                        correctAnswer = "Steven Spielberg",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    ), TriviaResponseEntity(
                        question = "What was the first ever entry written for the SCP Foundation collaborative writing project?",
                        userAnswer = "SCP-999",
                        correctAnswer = "SCP-173",
                        isCorrect = false,
                        timestamp = System.currentTimeMillis()
                    )
                )

                for (response in testTriviaResponses) {
                    appDatabase.triviaResponseDao().insert(response)
                }
            }
            sharedPreferences = getSharedPreferences("daily_trivia_prefs", MODE_PRIVATE)
            appDatabase = AppDatabase.getDatabase(applicationContext)

            val loggedInUserId = sharedPreferences.getString("USER_ID", null)
            val loggedInUsername = sharedPreferences.getString("USERNAME", null)
            if (loggedInUserId == null || loggedInUsername == null) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
        }

        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        viewPager.isUserInputEnabled = false
        viewPager.adapter = NavPagerAdapter(this)
        fetchAndUpdateCategories()
        // disable leaderboardButton due to not implemented (not bonus feature either)
        bottomNavigation.menu.findItem(R.id.leaderboardButton).isEnabled = false
        bottomNavigation.setOnItemSelectedListener { item ->
            val pageIndex = when (item.itemId) {
                R.id.overviewButton -> 0
                R.id.leaderboardButton -> 1
                R.id.statsButton -> 2
                R.id.settingsButton -> 3
                else -> null
            }

            pageIndex?.let {
                val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)
                TransitionManager.beginDelayedTransition(viewPager, sharedAxis)
                viewPager.setCurrentItem(it, false)
                true
            } == true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun fetchAndUpdateCategories() {
        lifecycleScope.launch {
            try {
                val apiService =
                    RetrofitInstance.RetrofitClient.getClient().create(ApiService::class.java)
                val categoriesResponse: CategoryResponse = apiService.getCategories()

                if (categoriesResponse.triviaCategories.isNotEmpty()) {
                    val existingCategories = appDatabase.categoryDao().getAllCategories()
                    val existingCategoryMap =
                        existingCategories.associateBy { Pair(it.id, it.name) }
                    val newCategories =
                        categoriesResponse.triviaCategories.map { it.toCategoryEntity() }

                    val hasConflicts = newCategories.any { newCategory ->
                        val key = Pair(newCategory.id, newCategory.name)
                        !existingCategoryMap.containsKey(key) || existingCategoryMap.any { (existingKey, _) ->
                            existingKey.first == newCategory.id && existingKey.second != newCategory.name || existingKey.second == newCategory.name && existingKey.first != newCategory.id
                        }
                    }

                    if (hasConflicts) {
                        appDatabase.categoryDao().clearCategories()
                        appDatabase.categoryDao().insertCategories(newCategories)
                    } else {
                        val uniqueNewCategories = newCategories.filter { newCategory ->
                            val key = Pair(newCategory.id, newCategory.name)
                            !existingCategoryMap.containsKey(key)
                        }
                        if (uniqueNewCategories.isNotEmpty()) {
                            appDatabase.categoryDao().insertCategories(uniqueNewCategories)
                        }
                    }
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "${newCategories.size} " + getString(R.string.categories_loaded),
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.dismiss) {}.show()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.no_category),
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.dismiss) {}.show()
                }
            } catch (e: Exception) {
                if (e.message == getString(R.string.http_error_429)) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.error_rate_limit_exceeded),
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.dismiss) {}.show()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.dismiss) {}.show()
                }
            }
        }
    }

    fun Category.toCategoryEntity(): CategoryEntity {
        return CategoryEntity(id = this.id, name = this.name)
    }

}

package com.example.dailytrivia

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dailytrivia.data.model.Question
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.database.TriviaResponseEntity
import com.example.dailytrivia.ui.login.QuestionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class PlayQuizActivity : AppCompatActivity(),
    BooleanAnswerFragment.AnswerCallback,
    MultipleChoiceAnswerFragment.AnswerCallback {

    private var currentQuestionIndex = 0
    private lateinit var questions: List<Question>
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_quiz)

        database = AppDatabase.getDatabase(applicationContext)
        questions = intent.serializable("QUESTIONS") ?: emptyList()

        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions available.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadPowerUpFragment()
            loadQuestionFragment()
        }
    }

    private fun loadPowerUpFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.powerUpFragmentContainer, PowerUpFragment())
            .commit()
    }

    private fun loadQuestionFragment() {
        if (currentQuestionIndex >= questions.size) return

        val currentQuestion = questions[currentQuestionIndex]

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.questionFragmentContainer,
                QuestionFragment.newInstance(currentQuestion.question)
            )
            .commit()

        when (currentQuestion.type) {
            "multiple" -> {
                val allAnswers =
                    (listOf(currentQuestion.correctAnswer) + currentQuestion.incorrectAnswers).shuffled()
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.answerFragmentContainer,
                        MultipleChoiceAnswerFragment.newInstance(
                            allAnswers.toTypedArray(),
                            currentQuestion.correctAnswer
                        )
                    )
                    .commit()
            }

            "boolean" -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.answerFragmentContainer,
                        BooleanAnswerFragment.newInstance(currentQuestion.correctAnswer)
                    )
                    .commit()
            }
        }
    }

    override fun onAnswerSelected(isCorrect: Boolean, userAnswer: String) {
        saveUserResponse(isCorrect, userAnswer)

        Toast.makeText(this, if (isCorrect) "Correct!" else "Wrong Answer!", Toast.LENGTH_SHORT)
            .show()
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            loadQuestionFragment()
        } else {
            showQuizFinishedMessage()
        }
    }


    private fun saveUserResponse(isCorrect: Boolean, userAnswer: String) {
        val currentQuestion = questions[currentQuestionIndex]
        val response = TriviaResponseEntity(
            id = 0,
            question = currentQuestion.question,
            userAnswer = userAnswer,
            correctAnswer = currentQuestion.correctAnswer,
            isCorrect = isCorrect,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            database.triviaResponseDao().insert(response)
            withContext(Dispatchers.Main) {
            }
        }
    }

    private fun showQuizFinishedMessage() {
        Toast.makeText(this, getString(R.string.quiz_completed), Toast.LENGTH_LONG).show()
        finish()
    }

    @SuppressLint("NewApi")
    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
            key,
            T::class.java
        )

        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }
}

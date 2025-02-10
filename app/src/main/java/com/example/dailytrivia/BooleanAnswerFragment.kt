package com.example.dailytrivia

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class BooleanAnswerFragment : Fragment() {

    private lateinit var correctAnswer: String
    private var answerCallback: AnswerCallback? = null

    interface AnswerCallback {
        fun onAnswerSelected(isCorrect: Boolean, userAnswer: String)
    }

    companion object {
        private const val ARG_CORRECT_ANSWER = "correct_answer"

        fun newInstance(correctAnswer: String): BooleanAnswerFragment {
            val fragment = BooleanAnswerFragment()
            val args = Bundle().apply {
                putString(ARG_CORRECT_ANSWER, correctAnswer)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AnswerCallback) {
            answerCallback = context
        } else {
            throw RuntimeException("$context must implement AnswerCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        answerCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        correctAnswer = arguments?.getString(ARG_CORRECT_ANSWER)
            ?: throw IllegalStateException(getString(R.string.correct_answer_must_be_provided))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_boolean_answer, container, false)

        val trueButton = view.findViewById<Button>(R.id.trueButton)
        val falseButton = view.findViewById<Button>(R.id.falseButton)

        trueButton.setOnClickListener { handleAnswerSelection(trueButton, "True") }
        falseButton.setOnClickListener { handleAnswerSelection(falseButton, "False") }

        return view
    }

    private fun handleAnswerSelection(button: Button, selectedAnswer: String) {
        val isCorrect = selectedAnswer.equals(correctAnswer, ignoreCase = true)

        answerCallback?.onAnswerSelected(isCorrect, userAnswer = selectedAnswer)
    }
}

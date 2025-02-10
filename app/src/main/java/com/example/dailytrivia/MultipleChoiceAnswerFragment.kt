package com.example.dailytrivia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dailytrivia.databinding.FragmentMultipleChoiceAnswerBinding

class MultipleChoiceAnswerFragment : Fragment() {

    private var _binding: FragmentMultipleChoiceAnswerBinding? = null
    private val binding get() = _binding!!

    private lateinit var answers: Array<String>
    private lateinit var correctAnswer: String

    interface AnswerCallback {
        fun onAnswerSelected(isCorrect: Boolean, userAnswer: String)
    }

    companion object {
        fun newInstance(
            answers: Array<String>,
            correctAnswer: String
        ): MultipleChoiceAnswerFragment {
            val fragment = MultipleChoiceAnswerFragment()
            val args = Bundle()
            args.putStringArray("ANSWERS", answers)
            args.putString("CORRECT_ANSWER", correctAnswer)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            answers = it.getStringArray("ANSWERS") ?: emptyArray()
            correctAnswer = it.getString("CORRECT_ANSWER") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMultipleChoiceAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )

        buttons.forEachIndexed { index, button ->
            if (index < answers.size) {
                val answerText = answers[index]

                button.text = answerText
                button.contentDescription = getString(R.string.answer_option) + " $answerText"
                button.visibility = View.VISIBLE

                button.setOnClickListener {
                    val isCorrect = answerText == correctAnswer
                    (activity as? AnswerCallback)?.onAnswerSelected(isCorrect, answerText)
                }
            } else {
                // In case number of answers are not 4
                button.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

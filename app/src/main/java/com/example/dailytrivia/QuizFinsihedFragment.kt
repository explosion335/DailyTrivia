package com.example.dailytrivia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuizFinishedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_finished, container, false)

        // Set up UI
        val finishMessage = view.findViewById<TextView>(R.id.finishMessage)
        finishMessage.text = getString(R.string.quiz_finished_message)

        val restartButton = view.findViewById<Button>(R.id.restartButton)
        restartButton.setOnClickListener {
            activity?.finish() // Go back to QuizSettingsFragment
        }

        return view
    }
}

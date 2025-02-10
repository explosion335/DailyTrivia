package com.example.dailytrivia

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.databinding.FragmentStatsBinding
import com.example.dailytrivia.ui.TriviaResponseAdapter
import com.example.dailytrivia.ui.TriviaViewModel
import com.example.dailytrivia.ui.TriviaViewModelFactory

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val triviaViewModel: TriviaViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        TriviaViewModelFactory(database.triviaResponseDao())
    }

    private lateinit var adapter: TriviaResponseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        adapter = TriviaResponseAdapter(mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe trivia responses and update adapter using DiffUtil
        triviaViewModel.allTriviaResponses.observe(viewLifecycleOwner) { responses ->
            adapter.updateData(responses)
        }

        // Observe performance stats and update the UI
        triviaViewModel.correctResponseCount.observe(viewLifecycleOwner) { correct ->
            triviaViewModel.totalResponseCount.observe(viewLifecycleOwner) { total ->
                val accuracy = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0
                binding.accuracyText.text = "Accuracy: $accuracy%"
                binding.totalResponsesText.text = "Total Responses: $total"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.dailytrivia

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.dailytrivia.databinding.FragmentJoinBinding
import kotlinx.coroutines.launch

class JoinFragment : Fragment() {

    private lateinit var viewModel: MultiplayerTriviaViewModel
    private lateinit var binding: FragmentJoinBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        println(4)
        binding = FragmentJoinBinding.inflate(inflater, container, false)
        println(4)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println(4)
        viewModel = ViewModelProvider(requireActivity())[MultiplayerTriviaViewModel::class.java]
        println(4)
        binding.joinGameButton.setOnClickListener {
            viewModel.discoverAndJoinLocalGame()
        }
        println(4)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gameState.collect { state ->
                    when (state) {
                        is MultiplayerTriviaViewModel.GameState.Connecting -> {
                            binding.statusTextView.text = getString(R.string.searching_for_a_game)
                            binding.joinGameButton.isEnabled = false
                        }

                        is MultiplayerTriviaViewModel.GameState.Connected -> {
                            binding.statusTextView.text = getString(R.string.connected_to_game)
                            binding.joinGameButton.isEnabled = true

                            setupGameStartListener()
                        }

                        is MultiplayerTriviaViewModel.GameState.Starting -> {
                            binding.statusTextView.text = getString(R.string.game_starting)
                        }

                        is MultiplayerTriviaViewModel.GameState.Error -> {
                            binding.statusTextView.text = buildString {
                                append(getString(R.string.error))
                                append(": ")
                                append(state.message)
                            }
                            binding.joinGameButton.isEnabled = true
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect { players ->
                    if (players.isNotEmpty()) {
                        val lastJoinedPlayer = players.last()
                        binding.statusTextView.append(
                            getString(
                                R.string.joined_the_game,
                                lastJoinedPlayer.name
                            ))
                    }
                }
            }
        }
    }

    private fun setupGameStartListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gameState.collect { state ->
                    if (state is MultiplayerTriviaViewModel.GameState.Starting) {
                        val intent = Intent(requireContext(), MultiplayerPlayQuizActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

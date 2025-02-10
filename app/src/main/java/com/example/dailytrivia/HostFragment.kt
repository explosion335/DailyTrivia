package com.example.dailytrivia

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.dailytrivia.databinding.FragmentHostBinding
import kotlinx.coroutines.launch
import java.util.Locale

class HostFragment : Fragment() {

    private lateinit var viewModel: MultiplayerTriviaViewModel
    private lateinit var binding: FragmentHostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MultiplayerTriviaViewModel::class.java]

        binding.playerNameEditText.setText(String.format(Locale.getDefault(), "Name"))

        binding.hostGameButton.setOnClickListener {
            val playerName = binding.playerNameEditText.text.toString()
            if (playerName.isNotBlank()) {
                viewModel.hostGameSession(playerName)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.enter_your_name), Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.startGameButton.setOnClickListener {
            if (viewModel.players.value.size > 1) {
                viewModel.startGameAsHost()
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.not_enough_players), Toast.LENGTH_SHORT
                ).show()
            }
        }

        observeGameState()
        observePlayers()
    }

    private fun observeGameState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gameState.collect { state ->
                    when (state) {
                        is MultiplayerTriviaViewModel.GameState.Hosting -> {
                            binding.statusTextView.text = getString(R.string.hosting_game)
                        }
                        is MultiplayerTriviaViewModel.GameState.Connected -> {
                            binding.statusTextView.text =
                                getString(R.string.game_started_players_can_now_join)
                        }
                        is MultiplayerTriviaViewModel.GameState.Error -> {
                            binding.statusTextView.text = buildString {
                                append(getString(R.string.error))
                                append(": ")
                                append(state.message)
                            }
                        }
                        else -> {
                            Log.d("HostFragment", "Unhandled game state: $state")
                        }
                    }
                }
            }
        }
    }

    private fun observePlayers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect { players ->
                    if (players.isNotEmpty()) {
                        val lastJoinedPlayer = players.last()
                        binding.statusTextView.append("\n${lastJoinedPlayer.name} joined the game.")
                    }
                }
            }
        }
    }
}

package com.example.dailytrivia

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytrivia.MultiplayerTriviaViewModel.TriviaQuestion
import com.example.dailytrivia.databinding.FragmentMultiplayerPlayQuizBinding
import kotlinx.coroutines.launch

class MultiplayerPlayQuizActivity : AppCompatActivity() {
    private lateinit var viewModel: MultiplayerTriviaViewModel
    private lateinit var binding: FragmentMultiplayerPlayQuizBinding
    private lateinit var playersScoreAdapter: PlayerScoreAdapter

    private var currentPlayerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMultiplayerPlayQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MultiplayerTriviaViewModel::class.java]

        initializeUIComponents()
        observeViewModel()
        setupAnswerButtons()
    }

    private fun initializeUIComponents() {
        playersScoreAdapter = PlayerScoreAdapter()
        binding.playersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.playersRecyclerView.adapter = playersScoreAdapter

        val answerButtons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentQuestion.collect { question ->
                question?.let { updateQuestionUI(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.players.collect { players ->
                updatePlayersUI(players)
                currentPlayerId = players.firstOrNull()?.id
            }
        }

        lifecycleScope.launch {
            viewModel.gameState.collect { gameState ->
                handleGameState(gameState)
            }
        }
    }

    private fun setupAnswerButtons() {
        val answerButtons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )

        answerButtons.forEach { button ->
            button.setOnClickListener {
                val selectedAnswer = button.text.toString()
                currentPlayerId?.let { playerId ->
                    viewModel.sendPlayerAnswer(playerId, selectedAnswer)
                } ?: run {
                    Toast.makeText(this, "Player not identified", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateQuestionUI(question: TriviaQuestion) {
        binding.questionTextView.text = question.question
        val answerButtons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )
        val answers = (listOf(question.correctAnswer) + question.incorrectAnswers).shuffled()
        answerButtons.forEachIndexed { index, button ->
            if (index < answers.size) {
                button.text = answers[index]
                button.visibility = View.VISIBLE
            } else {
                button.visibility = View.GONE
            }
        }
    }

    private fun updatePlayersUI(players: List<MultiplayerTriviaViewModel.Player>) {
        playersScoreAdapter.submitList(players)
    }

    private fun handleGameState(gameState: MultiplayerTriviaViewModel.GameState) {
        when (gameState) {
            is MultiplayerTriviaViewModel.GameState.Error -> {
                Toast.makeText(this, "Error: ${gameState.message}", Toast.LENGTH_LONG).show()
            }
            is MultiplayerTriviaViewModel.GameState.Disconnected -> {
                Toast.makeText(this, "Disconnected from game", Toast.LENGTH_SHORT).show()
                finish()
            }
            else -> {}
        }
    }
    class PlayerScoreAdapter : RecyclerView.Adapter<PlayerScoreAdapter.PlayerScoreViewHolder>() {
        private var players: List<MultiplayerTriviaViewModel.Player> = emptyList()

        fun submitList(newPlayers: List<MultiplayerTriviaViewModel.Player>) {
            players = newPlayers
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerScoreViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_player_score, parent, false)
            return PlayerScoreViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlayerScoreViewHolder, position: Int) {
            holder.bind(players[position])
        }

        override fun getItemCount() = players.size

        class PlayerScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
            private val playerScoreTextView: TextView = itemView.findViewById(R.id.playerScoreTextView)

            @SuppressLint("SetTextI18n")
            fun bind(player: MultiplayerTriviaViewModel.Player) {
                playerNameTextView.text = player.name
                playerScoreTextView.text = player.score.toString()
            }
        }
    }
}

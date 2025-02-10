package com.example.dailytrivia.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytrivia.databinding.ItemTriviaResponseBinding
import com.example.dailytrivia.database.TriviaResponseEntity

class TriviaResponseAdapter(
    private var responses: MutableList<TriviaResponseEntity>
) : RecyclerView.Adapter<TriviaResponseAdapter.TriviaViewHolder>() {

    class TriviaViewHolder(private val binding: ItemTriviaResponseBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(response: TriviaResponseEntity) {
            binding.questionText.text = response.question
            binding.userAnswerText.text = "Your Answer: ${response.userAnswer}"
            binding.correctAnswerText.text = "Correct Answer: ${response.correctAnswer}"
            binding.resultText.text = if (response.isCorrect) "Correct" else "Wrong"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TriviaViewHolder {
        val binding = ItemTriviaResponseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TriviaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TriviaViewHolder, position: Int) {
        holder.bind(responses[position])
    }

    override fun getItemCount(): Int = responses.size

    /**
     * Update the entire list with new data.
     * This uses DiffUtil to calculate changes more efficiently.
     */
    fun updateData(newData: List<TriviaResponseEntity>) {
        val diffCallback = TriviaDiffCallback(responses, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        responses.clear()
        responses.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Add a single new item.
     */
    fun addItem(response: TriviaResponseEntity) {
        responses.add(response)
        notifyItemInserted(responses.size - 1)
    }

    /**
     * Remove an item by position.
     */
    fun removeItem(position: Int) {
        if (position in responses.indices) {
            responses.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Update a specific item by position.
     */
    fun updateItem(position: Int, updatedResponse: TriviaResponseEntity) {
        if (position in responses.indices) {
            responses[position] = updatedResponse
            notifyItemChanged(position)
        }
    }
}

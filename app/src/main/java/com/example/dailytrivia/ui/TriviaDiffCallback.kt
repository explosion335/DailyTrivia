package com.example.dailytrivia.ui
import androidx.recyclerview.widget.DiffUtil
import com.example.dailytrivia.database.TriviaResponseEntity

class TriviaDiffCallback(
    private val oldList: List<TriviaResponseEntity>,
    private val newList: List<TriviaResponseEntity>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

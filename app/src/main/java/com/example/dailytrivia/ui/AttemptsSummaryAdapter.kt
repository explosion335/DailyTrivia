//package com.example.dailytrivia.ui
//
//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.dailytrivia.R
//import com.example.dailytrivia.database.AnswerHistogram
//
//class AttemptsSummaryAdapter(
//    private var summaries: MutableList<AnswerHistogram>
//) : RecyclerView.Adapter<AttemptsSummaryAdapter.ViewHolder>() {
//
//    fun updateData(newSummaries: List<AnswerHistogram>) {
//        summaries = newSummaries.toMutableList()
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attempt_summary, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val summary = summaries[position]
//        holder.bind(summary)
//    }
//
//    override fun getItemCount(): Int = summaries.size
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val questionText: TextView = itemView.findViewById(R.id.questionText)
//        private val correctCountText: TextView = itemView.findViewById(R.id.questionText)
//        private val incorrectCountText: TextView = itemView.findViewById(R.id.questionText)
//        private val totalAttemptsText: TextView = itemView.findViewById(R.id.questionText)
//        private val lastAttemptText: TextView = itemView.findViewById(R.id.questionText)
//
//        @SuppressLint("SetTextI18n")
//        fun bind(summary: AnswerHistogram) {
//            questionText.text = summary.question
//            correctCountText.text = "Correct: ${summary.correctCount}"
//            incorrectCountText.text = "Incorrect: ${summary.incorrectCount}"
//            totalAttemptsText.text = "Attempts: ${summary.totalAttempts}"
//            lastAttemptText.text = "Last Attempt: ${java.text.DateFormat.getDateTimeInstance().format(summary.lastAttemptTimestamp)}"
//        }
//    }
//}

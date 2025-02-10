package com.example.dailytrivia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dailytrivia.data.model.TriviaResponse
import com.example.dailytrivia.databinding.FragmentHomeBinding
import com.example.dailytrivia.endpoints.opentdb.OpenTDBApi
import com.example.dailytrivia.endpoints.opentdb.RetrofitInstance
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import com.google.android.material.snackbar.Snackbar
import java.lang.String.format
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.statusTitle.text = getString(R.string.not_updated)

        binding.updateBtn.setOnClickListener {
            fetchTriviaQuestion()
        }

        binding.playBtn.setOnClickListener {
            val intent = Intent(activity, QuizActivity::class.java)
            startActivity(intent)
        }

        binding.playMultiplayerBtn.setOnClickListener {
            val intent = Intent(activity, MultiplayerActivity::class.java)
            startActivity(intent)
        }

        binding.notifyBtn.setOnClickListener {
            showTimePicker()
        }

        return binding.root
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(12)
            .setMinute(0).setTitleText("Select Reminder Time").build()

        picker.show(parentFragmentManager, "MaterialTimePicker")

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            scheduleNotification(hour, minute)
        }
    }

    private fun scheduleNotification(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val scheduleTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (scheduleTime.before(now)) {
            scheduleTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delayInMillis = scheduleTime.timeInMillis - now.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().setInitialDelay(
            delayInMillis, TimeUnit.MILLISECONDS
        ).build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)

        Toast.makeText(requireContext(), "Notification scheduled!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun fetchTriviaQuestion() {
        lifecycleScope.launch {
            try {
                val userAPI =
                    RetrofitInstance.RetrofitClient.getClient().create(OpenTDBApi::class.java)
                val queryParams = mutableMapOf<String, String>()
                queryParams["amount"] = "50"
                val usersResponse: TriviaResponse = userAPI.getQuestions(queryParams)
                if (usersResponse.responseCode == 0) {
                    println("Parsed Response: $usersResponse")
                    binding.statusTitle.text = format(
                        Locale.getDefault(),
                        getString(R.string.last_connected_at),
                    )
                    binding.statusSummary.text = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    binding.updateTitle.text = format(
                        Locale.getDefault(),
                        resources.getQuantityString(R.plurals.questions_available, 50),
                        usersResponse.results.size
                    )
                } else {
                    println("Error: ${usersResponse.responseCode}")
                }
            } catch (e: Exception) {
                if (e.message == "HTTP 429 ") {
                    view?.let {
                        Snackbar.make(
                            it.findViewById(R.id.fragment_container),
                            getString(R.string.error_rate_limit_exceeded),
                            Snackbar.LENGTH_SHORT
                        ).setAction(R.string.dismiss) {}.show()
                    }
                } else if (e.message == getString(R.string.chain_validation_failed)) {
                    view?.let {
                        Snackbar.make(
                            it.findViewById(R.id.fragment_container),
                            getString(R.string.time_error),
                            Snackbar.LENGTH_SHORT
                        ).setAction(R.string.dismiss) {}.show()
                    }
                } else {
                    view?.let {
                        Snackbar.make(
                            it.findViewById(R.id.fragment_container),
                            e.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.dismiss) {}.show()
                    }
                }
            }
        }
    }
}

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }


    private fun sendNotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "TRIVIA_REMINDER", "Trivia Reminder", NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, "TRIVIA_REMINDER")
            .setContentTitle("Trivia Reminder").setContentText("Trivia Questions are available!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()

        notificationManager.notify(1, notification)
    }
}
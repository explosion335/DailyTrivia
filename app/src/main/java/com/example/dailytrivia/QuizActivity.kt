package com.example.dailytrivia

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.databinding.ActivityQuizBinding
import com.google.android.material.transition.MaterialSharedAxis
import com.example.dailytrivia.database.ProfileEntity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dailytrivia.data.LoginDataSource

class QuizActivity : AppCompatActivity(), ProfileSelectionFragment.OnProfileSelectedListener {

    lateinit var binding: ActivityQuizBinding
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = AppDatabase.getDatabase(applicationContext)
        val currentUserId = getCurrentUserId()
        loadProfilesForUser(currentUserId)

        if (savedInstanceState == null) {
            showProfileSelectionFragment(currentUserId)
        }

        binding.fabAddProfile.setOnClickListener {
            createNewProfile(currentUserId)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun loadProfilesForUser(userId: Int) {
        lifecycleScope.launch {
            val profiles = appDatabase.profileDao().getProfilesByUserId(userId)
            handleProfileList(profiles)
        }
    }

    private fun handleProfileList(profiles: List<ProfileEntity>) {
        if (profiles.isNotEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "${profiles.size} " + getString(R.string.profile_loaded),
                Snackbar.LENGTH_SHORT
            ).setAction(R.string.dismiss) {}.show()
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.no_profile_found_user),
                Snackbar.LENGTH_SHORT
            ).setAction(R.string.dismiss) {}.show()
        }
    }

    private fun showProfileSelectionFragment(userId: Int) {
        val fragment = ProfileSelectionFragment().apply {
            arguments = Bundle().apply {
                putInt("userId", userId)
            }
        }

        fragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        fragment.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onProfileSelected(profileId: String) {
        val quizSettingsFragment = QuizSettingsFragment().apply {
            arguments = Bundle().apply {
                putString("profileId", profileId)
            }
        }

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, quizSettingsFragment)
            .addToBackStack(null)
            .commit()

    }

    private fun createNewProfile(currentUserId: Int) {
        lifecycleScope.launch {
            val newProfileId = withContext(Dispatchers.IO) {
                val newProfile = ProfileEntity(
                    id = 0,
                    userId = currentUserId,
                    numberOfQuestions = 10,
                    difficulty = getString(R.string.any).lowercase(),
                    type = getString(R.string.any).lowercase(),
                    encoding = getString(R.string.any).lowercase(),
                    categoryId = 0
                )
                appDatabase.profileDao().insertProfile(newProfile)
            }

            val quizSettingsFragment = QuizSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString("profileId", newProfileId.toString())
                }
            }
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, quizSettingsFragment)
                .addToBackStack(null)
                .commit()

            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.created_new_profile_with_id) + "$newProfileId",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentUserId(): Int {
        val loginDataSource = LoginDataSource(
            context = applicationContext,
            userDao = appDatabase.userDao()
        )

        val loggedInUser = loginDataSource.getLoggedInUser()
        return loggedInUser?.userId?.toInt() ?: run {
            // If somehow user is not logged in
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.error_not_logged_in),
                Snackbar.LENGTH_SHORT
            ).setAction(R.string.dismiss) {}.show()
            finish()
            0
        }
    }

}

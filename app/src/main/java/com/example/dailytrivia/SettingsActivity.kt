package com.example.dailytrivia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.dailytrivia.ui.login.LoginActivity



class SettingsActivity : AppCompatActivity() {
    @Suppress("SpellCheckingInspection")
    override fun onCreate(savedInstanceState: Bundle?) {
//        this.theme.applyStyle(
//            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference,
//            true
//        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val categoryPreference = findPreference<Preference>("logout")
            categoryPreference?.setOnPreferenceClickListener {
                clearUserSession()
                navigateToLoginActivity()
                true
            }
        }

        private fun clearUserSession() {
            val sharedPreferences = requireActivity().getSharedPreferences(
                "daily_trivia_prefs",
                MODE_PRIVATE
            )
            sharedPreferences.edit().clear().apply()
        }

        private fun navigateToLoginActivity() {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}

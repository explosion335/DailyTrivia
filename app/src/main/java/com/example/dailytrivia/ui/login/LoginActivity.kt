package com.example.dailytrivia.ui.login

import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.dailytrivia.R
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.data.LoginDataSource
import com.example.dailytrivia.data.LoginRepository
import com.example.dailytrivia.databinding.ActivityLoginBinding
import io.appwrite.Client
import io.appwrite.enums.OAuthProvider
import io.appwrite.services.Account
import kotlinx.coroutines.launch
import com.example.dailytrivia.Config
import com.example.dailytrivia.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.lambdapioneer.argon2kt.Argon2Kt
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("daily_trivia_prefs", MODE_PRIVATE)

        // Check if a user is already logged in
        val loggedInUserId = sharedPreferences.getString("USER_ID", null)
        val loggedInUsername = sharedPreferences.getString("USERNAME", null)

        if (loggedInUserId != null && loggedInUsername != null) {
            navigateToMainActivity(loggedInUserId, loggedInUsername)
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val userDao = database.userDao()
        val dataSource = LoginDataSource(
            context = applicationContext,
            userDao = userDao,
            argon2Kt = Argon2Kt()
        )

        val repository = LoginRepository(dataSource)

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(repository))[LoginViewModel::class.java]

        // Set up the layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.loginField
        val password = binding.passwordField
        val login = binding.login
        val loading = binding.loading

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            login.isEnabled = loginState.isDataValid

            username.error = loginState.usernameError?.let { getString(it) }
            password.error = loginState.passwordError?.let { getString(it) }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer
            loading.visibility = View.GONE

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                val userId = loginResult.success.userId
                val displayName = loginResult.success.displayName
                saveUserSession(userId.toString(), displayName)
                updateUiWithUser(loginResult.success)
                navigateToMainActivity(userId.toString(), displayName)
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(username.text.toString(), password.text.toString())
        }

        password.afterTextChanged {
            loginViewModel.loginDataChanged(username.text.toString(), password.text.toString())
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    loginViewModel.login(username.text.toString(), password.text.toString())
                } catch (_: Exception) {
                    loading.visibility = View.GONE
                    showLoginFailed(R.string.login_failed)
                }
            }
        }

        // OAuth login with GitHub using Appwrite
        val client = Client(applicationContext)
            .setEndpoint(Config.ENDPOINT)
            .setProject(Config.PROJECT)
            .setSelfSigned(true)

        val account = Account(client)
        val btnGithub = findViewById<Button>(R.id.btn_github)
        btnGithub.setOnClickListener {
            lifecycleScope.launch {
                try {
                    account.createOAuth2Session(
                        activity = this@LoginActivity,
                        provider = OAuthProvider.GITHUB
                    )

                    val result = account.get()
                    val email = result.email



                    try {
                        account.create(
                            userId = "unique()",
                            email = email,
                            password = "12345678"
                        )
                        loginViewModel.login(email.toString(), "12345678")
                    } catch (_: Exception) {
                        loginViewModel.login(email.toString(), "12345678")
                    }

                    saveUserSession(result.id, email)
                    navigateToMainActivity(result.id, email)
                } catch (e: Exception) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.no_profile_found_user),
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.dismiss) {}.show()
                }
            }
        }
    }

    private fun saveUserSession(userId: String, username: String) {
        sharedPreferences.edit().apply {
            putString("USER_ID", userId)
            putString("USERNAME", username)
            apply()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        Toast.makeText(applicationContext, "$welcome $displayName", Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity(userId: String, username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USERNAME", username)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        startActivity(Intent(this, MainActivity::class.java), bundle)
        finish()
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

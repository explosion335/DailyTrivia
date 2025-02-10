package com.example.dailytrivia.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dailytrivia.data.LoginRepository
import com.example.dailytrivia.data.Result

import com.example.dailytrivia.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    suspend fun login(username: String, password: String) {
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName, userId = result.data.userId.toInt()))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        when {
            username.isEmpty() -> _loginForm.value =
                LoginFormState(usernameError = R.string.username_too_short)

            username.length > 254 -> _loginForm.value =
                LoginFormState(usernameError = R.string.username_too_long)

            !isUserNameValid(username) -> _loginForm.value =
                LoginFormState(usernameError = R.string.no_special_characters_username)

            password.length < 8 -> _loginForm.value =
                LoginFormState(passwordError = R.string.password_too_short)

            password.length > 254 -> _loginForm.value =
                LoginFormState(passwordError = R.string.password_too_long)

            else -> _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // Username validation
    private fun isUserNameValid(username: String): Boolean {
        val usernameRegex = "^[a-zA-Z0-9_-]{1,254}$".toRegex()
        return username.matches(usernameRegex)
    }

    // Password validation
//    private fun isPasswordValid(password: String): Boolean {
//        return password.length in 8..254
//    }
}

package com.example.dailytrivia.ui.login


/**
 * Define User details below (exposed to the UI) after authentication
 */
data class LoggedInUserView(
    val displayName: String,
    val userId: Int
)
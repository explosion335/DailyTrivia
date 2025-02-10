package com.example.dailytrivia.services

import android.content.Context
import io.appwrite.Client

@Suppress("SpellCheckingInspection")
object Appwrite {
    private const val ENDPOINT = "https://cloud.appwrite.io/v1"
    private const val PROJECT_ID = "672d1b19001f9c86e7bf"

    private lateinit var client: Client
    internal lateinit var account: AccountService
    fun init(context: Context) {
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)
        account = AccountService(client)
    }
}

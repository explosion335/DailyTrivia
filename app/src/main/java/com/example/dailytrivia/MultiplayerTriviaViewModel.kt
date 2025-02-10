package com.example.dailytrivia

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.URI
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.util.UUID

class MultiplayerTriviaViewModel(application: Application) : AndroidViewModel(application) {
    private val applicationContext = application.applicationContext

    private val _gameState = MutableStateFlow<GameState>(GameState.Disconnected)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _currentQuestion = MutableStateFlow<TriviaQuestion?>(null)
    val currentQuestion: StateFlow<TriviaQuestion?> = _currentQuestion.asStateFlow()

    private var localWebSocketServer: TriviaWebSocketServer? = null
    private var localWebSocketClient: org.java_websocket.client.WebSocketClient? = null

    private val playerNameGenerator = PlayerNameGenerator()

    sealed class GameState {
        object Disconnected : GameState()
        object Hosting : GameState()
        object Connecting : GameState()
        object Connected : GameState()
        object Starting : GameState()
        data class Error(val message: String) : GameState()
    }

    data class Player(
        val id: String, val name: String, val score: Int = 0
    )

    data class TriviaQuestion(
        val question: String, val correctAnswer: String, val incorrectAnswers: List<String>
    )

    inner class TriviaWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
        private val connectedClients = mutableSetOf<WebSocket>()

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            Log.d("WebSocketServer", "New connection from ${conn.remoteSocketAddress}")
            connectedClients.add(conn)

            val playerId = generateUniquePlayerId()
            val playerName = playerNameGenerator.generateName()

            val playerJoinMessage = JSONObject().apply {
                put("type", MessageType.PLAYER_JOIN.name)
                put("payload", JSONObject().apply {
                    put("playerId", playerId)
                    put("playerName", playerName)
                })
            }

            broadcast(playerJoinMessage.toString())

            viewModelScope.launch {
                val newPlayer = Player(id = playerId, name = playerName)
                _players.value += newPlayer
            }
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            Log.d("WebSocketServer", "Connection closed from ${conn.remoteSocketAddress}")
            connectedClients.remove(conn)

            viewModelScope.launch {
                _players.value = _players.value.filter { it.id != conn.hashCode().toString() }
            }
        }

        override fun onMessage(conn: WebSocket, message: String) {
            Log.d("WebSocketServer", "Received message: $message")
            val jsonMessage = JSONObject(message)
            val type = MessageType.valueOf(jsonMessage.getString("type"))

            when (type) {
                MessageType.PLAYER_JOIN -> {
                    Log.d("WebSocketServer", "Player join message received: $message")
                }

                MessageType.PLAYER_ANSWER -> {
                    broadcast(message)
                }

                else -> {
                    Log.d("WebSocketServer", "Unhandled message type: $type")
                }
            }
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            Log.e("WebSocketServer", "Error occurred", ex)
        }

        override fun onStart() {
            Log.d("WebSocketServer", "Server started successfully")
        }

        private fun generateUniquePlayerId(): String {
            return UUID.randomUUID().toString()
        }
    }

    class PlayerNameGenerator {
        private val adjectives = listOf(
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10"
        )
        private val animals = listOf(
            "Wolf", "Eagle", "Lion", "Tiger", "Hawk", "Fox", "Bear", "Falcon", "Dragon", "Panther"
        )
        private var counter = 0

        fun generateName(): String {
            val adjective = adjectives[counter % adjectives.size]
            val noun = animals[counter % animals.size]
            counter++
            return "$adjective$noun"
        }
    }

    fun hostGameSession(playerName: String, port: Int = 8888) {
        viewModelScope.launch {
            _gameState.value = GameState.Hosting

            try {
                localWebSocketServer = TriviaWebSocketServer(port)
                localWebSocketServer?.start()

                val hostPlayerId = UUID.randomUUID().toString()
                _players.value = listOf(
                    Player(id = hostPlayerId, name = playerName)
                )

                broadcastHostDiscovery(playerName, port)

                _gameState.value = GameState.Connected
                println(1)
            } catch (e: Exception) {
                _gameState.value = GameState.Error("Failed to host game: ${e.message}")
            }
        }
    }

    fun discoverAndJoinLocalGame(timeout: Long = 5000) {
        viewModelScope.launch {
            _gameState.value = GameState.Connecting

            try {
                val hostAddress = discoverLocalGameHost(timeout)

                if (hostAddress != null) {
                    connectToLocalHost(hostAddress.hostAddress!!, 8888)
                } else {
                    _gameState.value = GameState.Error("No local game host found")
                }
            } catch (e: Exception) {
                _gameState.value = GameState.Error("Discovery failed: ${e.message}")
            }
        }
    }

    private fun broadcastHostDiscovery(playerName: String, port: Int) {
        val localIp = getLocalIpAddress()
        val broadcastAddress = getBroadcastAddress()

        if (broadcastAddress != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    DatagramSocket().use { socket ->
                        val message = JSONObject().apply {
                            put("type", MessageType.PLAYER_JOIN.name)
                            put("payload", JSONObject().apply {
                                put("playerName", playerName)
                                put("hostIp", localIp)
                                put("port", port)
                                put("playerId", UUID.randomUUID().toString())
                            })
                        }.toString()

                        val data = message.toByteArray()
                        val packet = DatagramPacket(
                            data, data.size, InetAddress.getByName(broadcastAddress), port
                        )
                        socket.broadcast = true
                        socket.send(packet)

                        Log.d("HostDiscovery", "Broadcast player join: $message")
                    }
                } catch (e: Exception) {
                    Log.e("HostDiscovery", "Error broadcasting player join: ${e.message}", e)
                }
            }
        } else {
            Log.e("HostDiscovery", "Failed to determine broadcast address")
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getBroadcastAddress(): String? {
        return try {
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION") val dhcpInfo = wifiManager.dhcpInfo
            if (dhcpInfo != null) {
                val broadcast = (dhcpInfo.ipAddress and dhcpInfo.netmask.inv()) or dhcpInfo.netmask
                String.format(
                    "%d.%d.%d.%d",
                    broadcast and 0xff,
                    broadcast shr 8 and 0xff,
                    broadcast shr 16 and 0xff,
                    broadcast shr 24 and 0xff
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Error calculating broadcast address", e)
            null
        }
    }

    private suspend fun discoverLocalGameHost(timeout: Long): InetAddress? =
        withContext(Dispatchers.IO) {
            val subnet = getLocalSubnet()
            // Localhost

            // Scan the local subnet
            for (i in 1..254) {
                val testIp = "$subnet.$i"
                try {
                    val address = InetAddress.getByName(testIp)
                    if (address.isReachable(timeout.toInt())) {
                        return@withContext address
                    }
                } catch (e: Exception) {
                    Log.d("HostDiscovery", "Error checking $testIp: ${e.message}")
                }
            }

            null
        }

    private val wifiManager by lazy {
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun getLocalIpAddress(): String {
        try {
            NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
                .filter { !it.isLoopbackAddress && it is java.net.Inet4Address }.firstOrNull()
                ?.let { return it.hostAddress }
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Error getting local IP via network interfaces", e)
        }

        // Fallback to WiFi manager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }

    private fun getLocalSubnet(): String {
        val localIp = getLocalIpAddress()
        return localIp.substringBeforeLast(".")
    }

    private fun connectToLocalHost(host: String, port: Int) {
        localWebSocketClient = object : org.java_websocket.client.WebSocketClient(
            URI("ws://$host:$port")
        ) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocketClient", "Connection opened")
                _gameState.value = GameState.Connected
            }


            override fun onMessage(message: String?) {
                message?.let { handleIncomingMessage(it) }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocketClient", "Connection closed")
                _gameState.value = GameState.Disconnected
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocketClient", "Connection error", ex)
                _gameState.value = GameState.Error("Connection failed: ${ex?.message}")
            }
        }

        localWebSocketClient?.connect()
    }

    private fun handleIncomingMessage(message: String) {
        val jsonMessage = JSONObject(message)
        val type = MessageType.valueOf(jsonMessage.getString("type"))
        val payload = jsonMessage.getJSONObject("payload")

        when (type) {
            MessageType.PLAYER_JOIN -> {
                val player = Player(
                    id = payload.getString("playerId"),
                    name = payload.optString("playerName", "Player")
                )

                // Ensure the player is not already in the list
                if (_players.value.none { it.id == player.id }) {
                    _players.value += player
                    Log.d("WebSocket", "Player joined: $player")
                    Log.d("WebSocket", "Total players: ${_players.value.size}")
                }
            }

            MessageType.NEW_QUESTION -> {
                val question = TriviaQuestion(question = payload.getString("question"),
                    correctAnswer = payload.getString("correctAnswer"),
                    incorrectAnswers = payload.getJSONArray("incorrectAnswers")
                        .let { (0 until it.length()).map { i -> it.getString(i) } })
                _currentQuestion.value = question
            }

            MessageType.GAME_START -> {
                _gameState.value = GameState.Starting
            }

            MessageType.PLAYER_ANSWER -> {

                Log.d("WebSocket", "Player answer received: $message")
            }

            else -> Log.d("WebSocket", "Unhandled message type: $type")
        }
    }

    private fun fetchTriviaQuestion(): TriviaQuestion? {
        return TriviaQuestion(
            question = "What is the capital of France?",
            correctAnswer = "Paris",
            incorrectAnswers = listOf("London", "Berlin", "Madrid")
        )
    }
//
//    private fun broadcastTriviaQuestion(triviaQuestion: TriviaQuestion) {
//        val message = JSONObject().apply {
//            put("type", MessageType.NEW_QUESTION.name)
//            put("payload", JSONObject().apply {
//                put("question", triviaQuestion.question)
//                put("correctAnswer", triviaQuestion.correctAnswer)
//                put("incorrectAnswers", triviaQuestion.incorrectAnswers)
//            })
//        }
//        localWebSocketServer?.broadcast(message.toString())
//    }

    fun sendPlayerAnswer(playerId: String, answer: String) {
        viewModelScope.launch {
            try {
                val answerMessage = JSONObject().apply {
                    put("type", MessageType.PLAYER_ANSWER.name)
                    put("payload", JSONObject().apply {
                        put("playerId", playerId)
                        put("answer", answer)
                    })
                }

                localWebSocketClient?.send(answerMessage.toString())
                localWebSocketServer?.broadcast(answerMessage.toString())
            } catch (e: Exception) {
                Log.e("sendPlayerAnswer", "Error sending player answer", e)
            }
        }
    }


    enum class MessageType {
        HOST_JOIN, PLAYER_JOIN, NEW_QUESTION, PLAYER_ANSWER, GAME_START, GAME_END, GAME_ERROR
    }

    fun startGameAsHost() {
        viewModelScope.launch {
            val triviaQuestion = fetchTriviaQuestion()

            triviaQuestion?.let {
                _currentQuestion.value = it

                val message = JSONObject().apply {
                    put("type", MessageType.NEW_QUESTION.name)
                    put("payload", JSONObject().apply {
                        put("question", it.question)
                        put("correctAnswer", it.correctAnswer)
                        put("incorrectAnswers", JSONArray(it.incorrectAnswers))
                    })
                }
                localWebSocketServer?.broadcast(message.toString())

                val gameStartMessage = JSONObject().apply {
                    put("type", MessageType.GAME_START.name)
                }
                localWebSocketServer?.broadcast(gameStartMessage.toString())

                _gameState.value = GameState.Starting
                val intent =
                    Intent(applicationContext, MultiplayerPlayQuizActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                applicationContext.startActivity(intent)
            }
        }
    }

    fun setPlayerName(playerId: String, newName: String) {
        viewModelScope.launch {
            // Update the player's name in the local players list
            _players.value = _players.value.map { player ->
                if (player.id == playerId) {
                    player.copy(name = newName)
                } else {
                    player
                }
            }

            val nameChangeMessage = JSONObject().apply {
                put(
                    "type", MessageType.PLAYER_JOIN.name
                )
                put("payload", JSONObject().apply {
                    put("playerId", playerId)
                    put("playerName", newName)
                })
            }

            localWebSocketServer?.broadcast(nameChangeMessage.toString())
        }
    }

    private fun generateUniquePlayerId(): String {
        return UUID.randomUUID().toString()
    }

    override fun onCleared() {
        super.onCleared()
        localWebSocketServer?.stop()
        localWebSocketClient?.close()
    }
}

package com.kyhsgeekcode.screenshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kyhsgeekcode.screenshare.ui.theme.ScreenShareTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import org.webrtc.PeerConnection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenShareTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var name by remember { mutableStateOf("Hello, World!") }
                    var serverAddress by remember { mutableStateOf("ws://192.168.0.1:1234") }
                    Column {
                        Text(text = "WebRTC Test: Set name below.")
                        TextField(value = name, onValueChange = {
                            name = it
                        })
                        Text(text = "Set server address below.")
                        TextField(value = serverAddress, onValueChange = {
                            serverAddress = it
                        })
                        Button(onClick = {
                            startWebRTC(name, serverAddress)
                        }) {
                            Text(text = "Start")
                        }
                    }
                }
            }
        }
    }

    private fun startWebRTC(name: String, serverAddress: String) {
        // 1. connect websocket to server
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(serverAddress)
            .build()
        val listener: WebSocketListener = WebSocketListener()

        client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()

        // 2. init webrtc
        val (peerConnectionFactory, constraints) = initWebRTC()
        val teacherPeer = createPeer(
            peerConnectionFactory,
            // https://developer.mozilla.org/en-US/docs/Web/API/RTCIceServer/urls
            // STUN server spec: https://datatracker.ietf.org/doc/html/rfc5389
            // STUN server list: https://www.voip-info.org/stun/
            // STUN server role: get ip, port of self from outside
            listOf(
                PeerConnection.IceServer.builder("stun.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun1.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun2.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun3.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun4.l.google.com:19302").createIceServer(),
            )
        )
        // 3. set local description  and send offer to remote via server
        setTeacherPeer(
            constraints,
            "teacher uuid",
            teacherPeer,
            mutableMapOf()
        )
        // 5. wait for answer (websocket)

        // 6. set remote description
        // 7. send ice candidates to remote via server
        // 8. wait for ice candidates from remote
        // 9. set remote ice candidates
        // 10. start streaming
    }
}

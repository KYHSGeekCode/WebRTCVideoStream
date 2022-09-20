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
                            startRTCCall(name, serverAddress)
                        }) {
                            Text(text = "Start")
                        }
                    }
                }
            }
        }
    }

    private fun startRTCCall(name: String, signalServerAddress: String) {
        val webRTCCaller = WebRTCCaller(applicationContext, name, signalServerAddress, iceServers)
        webRTCCaller.call()
    }
}

// https://developer.mozilla.org/en-US/docs/Web/API/RTCIceServer/urls
// STUN server spec: https://datatracker.ietf.org/doc/html/rfc5389
// STUN server list: https://www.voip-info.org/stun/
// STUN server role: get ip, port of self from outside
val iceServers = listOf(
    PeerConnection.IceServer.builder("stun.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun1.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun2.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun3.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun4.l.google.com:19302").createIceServer(),
)
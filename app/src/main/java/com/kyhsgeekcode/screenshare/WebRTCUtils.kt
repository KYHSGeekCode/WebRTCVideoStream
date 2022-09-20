package com.kyhsgeekcode.screenshare

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.*
import org.webrtc.*

// https://iamheesoo.github.io/blog/android-webrtc01
// https://iamheesoo.github.io/blog/android-webrtc02
// http://jaynewho.com/post/36
// https://medium.com/@hyun.sang/webrtc-webrtc%EB%9E%80-43df68cbe511
// https://engineering.linecorp.com/ko/blog/the-architecture-behind-chatting-on-line-live/

open class LoggingSdpObserver : SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Log.d("WebRTCUtils", "onCreateSuccess() called")
    }

    override fun onSetSuccess() {
        Log.d("WebRTCUtils", "onSetSuccess() called")
    }

    override fun onCreateFailure(p0: String?) {
        Log.d("WebRTCUtils", "onCreateFailure() called with: s = [$p0]")
    }

    override fun onSetFailure(p0: String?) {
        Log.d("WebRTCUtils", "onSetFailure() called with: s = [$p0]")
    }
}

class WebRTCCaller(
    applicationContext: Context,
    private val name: String,
    private val serverAddress: String,
    private val iceServers: List<PeerConnection.IceServer>
) :
    WebSocketListener(),
    PeerConnection.Observer {
    private var peerConnectionFactory: PeerConnectionFactory = run {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        PeerConnectionFactory.builder().createPeerConnectionFactory()
    }


    var websocket: WebSocket? = null
    private var audioStream: MediaStream
    private var videoStream: MediaStream

    private val sdpConstraints: MediaConstraints = MediaConstraints()

    // in case of multiple connections
    private val peerMap = mutableMapOf<String, PeerConnection>()

    var teacherPeer: PeerConnection? = null

    init {
        val audioConstraints = MediaConstraints()
        val audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        val videoSource = peerConnectionFactory.createVideoSource(true)
        val localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)
        val localVideoTrack = peerConnectionFactory.createVideoTrack("102", videoSource)
        localAudioTrack.setEnabled(true)
        localVideoTrack.setEnabled(true)
        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"))
        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"))
        audioStream = peerConnectionFactory.createLocalMediaStream("103")
        videoStream = peerConnectionFactory.createLocalMediaStream("104")
        audioStream.addTrack(localAudioTrack)
        videoStream.addTrack(localVideoTrack)
    }

    // websocket
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
    }

    // 5. wait for answer (websocket)
    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        // if it is answer

        // if it is offer
        val teacherPeer = teacherPeer
        if (teacherPeer == null) {
            Log.e("WebRTCUtils", "teacherPeer is null")
            return
        }
        // 6. set remote description
        teacherPeer.createAnswer(object : LoggingSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {

                teacherPeer.setLocalDescription(LoggingSdpObserver(), sessionDescription)
                /**
                 * createOffer()한 sdp를 서버로 전송
                 */
                // really send the sdp to the remote peer
                webSocket.send(
                    buildJsonObject {
                        put("type", "answer")
                        put("sdp", sessionDescription.description)
                    }.toString()
                )
            }
        }, sdpConstraints)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        TODO("Not yet implemented")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        TODO("Not yet implemented")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        TODO("Not yet implemented")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        TODO("Not yet implemented")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        TODO("Not yet implemented")
    }

    override fun onAddStream(p0: MediaStream?) {
        TODO("Not yet implemented")
        // gotRemoteStream(mediaStream)
    }

    override fun onRemoveStream(p0: MediaStream?) {
        TODO("Not yet implemented")
    }

    override fun onDataChannel(p0: DataChannel?) {
        TODO("Not yet implemented")
    }

    override fun onRenegotiationNeeded() {
        TODO("Not yet implemented")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        TODO("Not yet implemented")
    }

    private fun createPeerConnection(): PeerConnection? {
        val newPeer = peerConnectionFactory.createPeerConnection(
            iceServers,
            this,
        ) ?: run {
            Log.e("WebRTCCaller", "createPeerConnection failed")
            return null
        }
        newPeer.addStream(videoStream)
        newPeer.addStream(audioStream)
        return newPeer
    }

    private fun connectSignalServer() {
        // 1. connect websocket to singal server
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("$serverAddress/$name")
            .build()

        websocket = client.newWebSocket(request, this)
        client.dispatcher.executorService.shutdown()
    }

    fun call() {
        connectSignalServer()
        // create peer connection and register ice handler and data channel handler
        teacherPeer = createPeerConnection()
        val teacherPeer = teacherPeer
        // 3. set local description  and send offer to remote via server
        if (teacherPeer == null) {
            Log.e("WebRTCCaller", "createPeerConnection failed")
            return
        }
        /*
         * The createOffer() method of the RTCPeerConnection interface initiates
         * the creation of an SDP offer for the purpose of
         * starting a new WebRTC connection to a remote peer.
         * The SDP offer includes information about any MediaStreamTrack objects already attached to
         * the WebRTC session, codec, and options supported by the browser,
         * and any candidates already gathered by the ICE agent, for the purpose of being sent over the
         * signaling channel to a potential peer to request a connection or to update the configuration
         * of an existing connection.
         *
         * The return value is a Promise which, when the offer has been created,
         * is resolved with a RTCSessionDescription object containing the newly-created offer.
         */
        teacherPeer.createOffer(object : LoggingSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                // Set the local description and send the sdp description to the remote peer via websocket.
                val webSocket = websocket ?: run {
                    Log.e("WebRTCUtils", "websocket is null")
                    return
                }
                Log.i(
                    "WebRTCUtils",
                    "onCreateSuccess() called with: sessionDescription = [$sessionDescription]"
                )
                teacherPeer.setLocalDescription(LoggingSdpObserver(), sessionDescription)

                /**
                 * createOffer()한 sdp를 서버로 전송
                 */
                // really send the sdp to the remote peer
                webSocket.send(
                    buildJsonObject {
                        put("type", "offer")
                        put("sdp", sessionDescription.description)
                    }.toString()
                )
            }
        }, sdpConstraints)


        // 7. send ice candidates to remote via server
        // 8. wait for ice candidates from remote
        // 9. set remote ice candidates
        // 10. start streaming
    }
}
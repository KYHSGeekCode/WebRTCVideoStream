package com.kyhsgeekcode.screenshare

import android.util.Log
import org.webrtc.*

// https://iamheesoo.github.io/blog/android-webrtc01
// https://iamheesoo.github.io/blog/android-webrtc02
// http://jaynewho.com/post/36
// https://medium.com/@hyun.sang/webrtc-webrtc%EB%9E%80-43df68cbe511
// https://engineering.linecorp.com/ko/blog/the-architecture-behind-chatting-on-line-live/

fun initWebRTC(): Pair<PeerConnectionFactory, MediaConstraints> {
    val peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    val audioConstraints = MediaConstraints()
    val audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
    val videoSource = peerConnectionFactory.createVideoSource(true)
    val localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)
    val localVideoTrack = peerConnectionFactory.createVideoTrack("102", videoSource)
    localAudioTrack.setEnabled(true)
    localVideoTrack.setEnabled(true)
    val sdpConstraints = MediaConstraints()
    sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"))
    sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"))
    val audioStream = peerConnectionFactory.createLocalMediaStream("103")
    val videoStream = peerConnectionFactory.createLocalMediaStream("104")
    audioStream.addTrack(localAudioTrack)
    videoStream.addTrack(localVideoTrack)
    return peerConnectionFactory to sdpConstraints
}

/*
 * Called when the student finds a teacher.
 */
fun setTeacherPeer(
    sdpConstraints: MediaConstraints,
    peerUuid: String,
    newPeer: PeerConnection,
    peerMap: MutableMap<String, PeerConnection>
) {
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
    newPeer.createOffer(object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            createdDescription(sessionDescription, peerUuid, peerMap)
        }

        override fun onSetSuccess() {
            Log.d("WebRTCUtils", "onSetSuccess() called");
        }

        override fun onCreateFailure(p0: String?) {
            Log.d("WebRTCUtils", "onCreateFailure() called with: s = [$p0]");
        }

        override fun onSetFailure(p0: String?) {
            Log.d("WebRTCUtils", "onSetFailure() called with: s = [$p0]");
        }
    }, sdpConstraints)
}

// Set the local description and send the sdp description to the remote peer via websocket.
fun createdDescription(
    sessionDescription: SessionDescription?,
    peerUuid: String,
    peerMap: MutableMap<String, PeerConnection>
) {
    Log.i("WebRTCUtils", "createdDescription")
    val tempPeer: PeerConnection? = peerMap[peerUuid]
    tempPeer?.setLocalDescription(object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {
            Log.d(
                "WebRTCUtils",
                "onCreateSuccess() called with: sessionDescription = [$sessionDescription]"
            )
        }

        override fun onSetSuccess() {
            Log.d("WebRTCUtils", "onSetSuccess() called");
        }

        override fun onCreateFailure(p0: String?) {
            Log.d("WebRTCUtils", "onCreateFailure() called with: s = [$p0]");
        }

        override fun onSetFailure(p0: String?) {
            Log.d("WebRTCUtils", "onSetFailure() called with: s = [$p0]");
        }

    }, sessionDescription)
    /**
     * createOffer()한 sdp를 서버로 전송
     */
    // really send the sdp to the remote peer
}

fun createAnswer(
    tempPeer: PeerConnection,
    peerConnection: PeerConnection,
    peerUuid: String,
    peerMap: HashMap<String, PeerConnection>
) {
    peerConnection.createAnswer(object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription?) {
            tempPeer.setLocalDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    TODO("Not yet implemented")
                }

                override fun onSetSuccess() {
                    TODO("Not yet implemented")
                }

                override fun onCreateFailure(p0: String?) {
                    TODO("Not yet implemented")
                }

                override fun onSetFailure(p0: String?) {
                    TODO("Not yet implemented")
                }

            }, sessionDescription)
            createdDescription(sessionDescription, peerUuid, peerMap)
        }

        override fun onSetSuccess() {
            TODO("Not yet implemented")
        }

        override fun onCreateFailure(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun onSetFailure(p0: String?) {
            TODO("Not yet implemented")
        }
    }, MediaConstraints())
}

fun createPeer(
    peerConnectionFactory: PeerConnectionFactory,
    iceServerList: List<PeerConnection.IceServer>,
): PeerConnection {
    val newPeer = peerConnectionFactory.createPeerConnection(
        iceServerList,
        object : PeerConnection.Observer {
            override fun onIceCandidate(iceCandidate: IceCandidate?) {
                Log.i("onIceCandidate", "onIceCandidate");
                /**
                 * 생성된 iceCandidate(파라미터 값)을 서버로 전송
                 */
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

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                TODO("Not yet implemented")
            }

            override fun onAddStream(p0: MediaStream?) {
                gotRemoteStream(mediaStream)
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
        }
    )
    newPeer?.addStream(stream)
    return newPeer!!
}
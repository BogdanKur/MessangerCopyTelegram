package com.example.messanger

import android.webkit.JavascriptInterface

class JavascriptInterfaceAudio(val callActivity: AudioCallFragment) {
    @JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}
package com.example.messanger

import android.webkit.JavascriptInterface

class JavascriptInterface(val callActivity: CallFragment) {

    @JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }

}
package com.example.messanger

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.identity.util.UUID
import com.example.messanger.ChatUserFragment.Companion.ids
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentAudioCallBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class AudioCallFragment : Fragment() {
    private var _binding: FragmentAudioCallBinding? = null
    private val binding get() = _binding!!
    var username = currentUserId.toString()
    var friendsUsername = ids.toString()

    var isPeerConnected = false

    var firebaseRef = Firebase.database.getReference("user")

    var isAudio = true
    var isVideo = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudioCallBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            binding.toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24 )
        }
        binding.rejectBtn.setOnClickListener {
            firebaseRef.child(username).child("incoming").setValue(null)
            firebaseRef.child(username).child("isAvailable").setValue(false)
            firebaseRef.child(username).child("connId").setValue(null)
            firebaseRef.child(friendsUsername).child("connId").setValue(null)
            firebaseRef.child(friendsUsername).child("isAvailable").setValue(false)
            firebaseRef.child(friendsUsername).child("incoming").setValue(null)
            navController.navigate(R.id.action_audioCallFragment_to_chatUserFragment)
        }

        setupWebView()

        Handler(Looper.getMainLooper()).postDelayed({
            sendCallRequest()
            Toast.makeText(requireContext(), "Ждём ответа от вашего собеседника", Toast.LENGTH_LONG).show()
        }, 3000)

        return view
    }

    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(requireContext(), "You're not connected. Check your internet", Toast.LENGTH_LONG).show()
            return
        }

        firebaseRef.child(friendsUsername).child("incoming").setValue(username)
        firebaseRef.child(friendsUsername).child("isAvailable").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }

            }

        })

    }

    private fun listenForConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
            }

        })
    }

    private fun setupWebView() {

        binding.webView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        binding.webView.addJavascriptInterface(JavascriptInterfaceAudio(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        binding.webView.loadUrl(filePath)

        binding.webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }

    var uniqueId = ""

    private fun initializePeer() {

        uniqueId = getUniqueID()

        callJavascriptFunction("javascript:init(\"${uniqueId}\")")
        firebaseRef.child(username).child("incoming").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                onCallRequest(snapshot.value as? String)
            }

        })

    }

    private fun onCallRequest(caller: String?) {
        if (caller == null) return

        firebaseRef.child(username).child("connId").setValue(uniqueId)
        firebaseRef.child(username).child("isAvailable").setValue(true)
        switchToControls()

    }

    private fun switchToControls() {
        binding.callControlLayout.visibility = View.VISIBLE
    }


    private fun getUniqueID(): String {
        return UUID.randomUUID().toString()
    }

    private fun callJavascriptFunction(functionString: String) {
        binding.webView.post { binding.webView.evaluateJavascript(functionString, null) }
    }


    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onDestroy() {
        binding.webView.loadUrl("about:blank")
        super.onDestroy()
    }

}
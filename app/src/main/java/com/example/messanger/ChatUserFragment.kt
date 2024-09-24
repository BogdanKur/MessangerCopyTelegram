package com.example.messanger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentChatUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatUserFragment : Fragment() {
    private var _binding: FragmentChatUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var usersRef:DatabaseReference
    private lateinit var job: Job
    private var phoneNumber =""
    var chatTextSize: Int? = null
    private var id = 0
    var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatUserBinding.inflate(inflater,container,false)
        val view = binding.root
        val navController = findNavController()
        usersRef = FirebaseDatabase.getInstance().reference
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        arguments?.let { bundle ->
            imageUrl = bundle.getString("IMAGE_URL").toString()
            val name = bundle.getString("NAMEOFUSER")
            phoneNumber = bundle.getString("NUMBER").toString()
            chatTextSize = bundle.getString("TEXTSIZE")?.toInt()
            id = bundle.getString("ID")!!.toInt()
            Glide.with(binding.imgBtnProfilePicture.context)
                .asBitmap()
                .load(imageUrl)
                .into(binding.imgBtnProfilePicture)
            binding.tvName.text = name
        }
        binding.imgBtnSendMessage.setOnClickListener {
            sendMessage(currentUserId.toString(), id.toString(), binding.etTextMessage.text.toString())
        }

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                listenForMessages(currentUserId.toString(), id.toString()){ messages ->
                    binding.rvMessage.adapter = MessageAdapter(currentUserId.toString(),messages, chatTextSize!!)
                }
                delay(1000)
            }
        }

        return view
    }

    fun listenForMessages(senderId: String, receiverId: String, onMessageReceived: (List<Message>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val messagesRef = database.child("user").child(senderId).child("messages").child(receiverId)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                onMessageReceived(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", error.message)
            }
        })
    }

    fun sendMessage(senderId: String, receiverId: String, messageText: String) {
        val database = FirebaseDatabase.getInstance().reference
        val messageId = database.child("user").child(senderId).child("messages").child(receiverId).push().key

        val message = Message(senderId, receiverId, messageText)

        if (messageId != null) {
            database.child("user").child(senderId).child("messages").child(receiverId).child(messageId).setValue(message)
            database.child("user").child(receiverId).child("messages").child(senderId).child(messageId).setValue(message)
        }
    }

}
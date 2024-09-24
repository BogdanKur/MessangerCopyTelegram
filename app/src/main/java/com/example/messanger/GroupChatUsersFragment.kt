package com.example.messanger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentGroupChatUsersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GroupChatUsersFragment : Fragment() {
    private var _binding: FragmentGroupChatUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var job: Job
    private var senderId = ""
    var textSize: Int? = null

    private var name = ""
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupChatUsersBinding.inflate(inflater, container, false )
        val view = binding.root
        val navController = findNavController()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Нельзя вернуться назад с этого экрана", Toast.LENGTH_SHORT).show()
        }
        binding.imgBtnBackToMessageMain.setOnClickListener {
            navController.navigate(R.id.action_groupChatUsersFragment_to_messageMainFragment)
        }

        arguments.let { bundle->
            name = bundle?.getString("name").toString()
            imageUrl = bundle?.getString("IMAGE").toString()
            textSize = bundle?.getString("TEXTSIZE")?.toInt()
            Glide.with(binding.imgBtnProfilePicture.context)
                .asBitmap()
                .load(imageUrl)
                .into(binding.imgBtnProfilePicture)
            binding.tvName.text = name
        }

        binding.imgBtnSendMessage.setOnClickListener {
            sendMessage(senderId, binding.etTextMessage.text.toString())
        }
        senderIdGet()

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                listenForMessages(senderId){ messages ->
                    binding.rvMessage.adapter = MessageGroupAdapter (senderId, messages, textSize!!)
                }
                delay(1000)
            }
        }

        return view
    }

    fun listenForMessages(senderId: String, onMessageReceived: (List<MessageGroup>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val messagesRef = database.child("group").child(senderId).child("messages")

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<MessageGroup>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(MessageGroup::class.java)
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

    fun sendMessage(senderId: String, messageText: String) {
        val database = FirebaseDatabase.getInstance().reference
        val messageId = database.child("group").child(senderId).child("messages").push().key

        val message = MessageGroup(senderId, messageText)

        if (messageId != null) {
            database.child("group").child(senderId).child("messages").child(messageId).setValue(message)
        }
    }

    fun senderIdGet() {
        val database = FirebaseDatabase.getInstance().reference
        val groupRef = database.child("group")
        groupRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children) {
                    for(userSnap in snap.child("users").children) {
                        Log.e("sendId", userSnap.child("userId").value.toString())
                        if(userSnap.child("userId").value.toString() == currentUserId.toString()) {
                            senderId = userSnap.child("userId").value.toString()
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}
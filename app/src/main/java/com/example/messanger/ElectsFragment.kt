package com.example.messanger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentElectsBinding
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

class ElectsFragment : Fragment() {
    private var _binding: FragmentElectsBinding? = null
    private val binding get() = _binding!!
    var usersRef = FirebaseDatabase.getInstance().reference
    lateinit var job: Job
    var textSize: Int? = 16

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentElectsBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_, destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        binding.imgBtnSendMessage.setOnClickListener {
            sendMessage(binding.etTextMessage.text.toString())
        }
        arguments.let { bundle ->
            textSize = bundle?.getString("TEXTSIZE").toString().toInt()
        }

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                listenYourMessages { messages ->
                    Log.e("xui", messages.toString())
                    binding.rvMessage.adapter = MessageElectAdapter(messages, textSize!!)
                }
                delay(1000)
            }
        }

        return view
    }

    fun listenYourMessages(onMessageReceived: (List<ElectMessages>) -> Unit) {
        val messagesRef = usersRef.child("user").child(currentUserId.toString()).child("messagesElect")

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ElectMessages>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ElectMessages::class.java)
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

    fun sendMessage(messageText: String) {
        val messageId = usersRef.child("user").child(currentUserId.toString()).child("messagesElect").push().key
        val message = ElectMessages(messageText)
        if(messageId != null) {
            usersRef.child("user").child(currentUserId.toString()).child("messagesElect").child(messageId).setValue(message)
        }
    }

}
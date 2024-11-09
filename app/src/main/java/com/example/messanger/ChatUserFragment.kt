package com.example.messanger

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentChatUserBinding
import com.google.android.material.internal.ViewUtils.hideKeyboard
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
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.util.UUID

class ChatUserFragment : Fragment() {
    private var _binding: FragmentChatUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var usersRef:DatabaseReference
    private lateinit var job: Job
    private var phoneNumber =""
    var chatTextSize: Int? = null
    companion object{
        var ids = 0
    }
    var imageUrl = ""
    var countHeight = 0

    private lateinit var mediaPlayer: MediaPlayer
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private val storage = FirebaseStorage.getInstance()
    private var messageList: List<Message> = listOf()
    @SuppressLint("SetTextI18n", "RestrictedApi")
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

        binding.imgBtnEmodji.setOnClickListener {
            hideKeyboard(binding.etTextMessage)
            binding.frameLayout.visibility = View.VISIBLE
            binding.imgBtnEmodjiClose.visibility = View.VISIBLE
            binding.imgBtnEmodji.visibility = View.GONE
        }
        binding.imgBtnEmodjiClose.setOnClickListener {
            binding.frameLayout.visibility = View.GONE
            binding.imgBtnEmodjiClose.visibility = View.GONE
            binding.imgBtnEmodji.visibility = View.VISIBLE
        }
        arguments?.let { bundle ->
            imageUrl = bundle.getString("IMAGE_URL").toString()
            val name = bundle.getString("NAMEOFUSER")
            phoneNumber = bundle.getString("NUMBER").toString()
            chatTextSize = bundle.getString("TEXTSIZE")?.toInt()
            ids = bundle.getString("ID")!!.toInt()
            Log.e("sd", ids.toString())
            Glide.with(binding.imgBtnProfilePicture.context)
                .asBitmap()
                .load(imageUrl)
                .into(binding.imgBtnProfilePicture)
            binding.tvName.text = name
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            binding.frameLayout.visibility = View.GONE
            binding.etTextMessage.clearFocus()
        }

        binding.imgBtnSendMessage.setOnClickListener {
            sendMessage(currentUserId.toString(), ids.toString(), binding.etTextMessage.text.toString())
            binding.etTextMessage.setText("")
            binding.etTextMessage.clearFocus()
            hideKeyboard(binding.etTextMessage)
            binding.imgBtnSendMessage.visibility = View.GONE
            binding.imgBtnRecordAudio.visibility = View.VISIBLE

            countHeight = 0
            listenForMessages(currentUserId.toString(), ids.toString()){ messages ->
                binding.rvMessage.adapter = MessageAdapter(currentUserId.toString(),messages, chatTextSize!!)
            }

        }

        binding.imbBtnSmileFace.setOnClickListener {
            val stringEtTextMessage = binding.etTextMessage.text.toString()
            binding.etTextMessage.setText(stringEtTextMessage + "\uD83D\uDE02")
        }
        binding.imbBtnKiss.setOnClickListener {
            val stringEtTextMessage = binding.etTextMessage.text.toString()
            binding.etTextMessage.setText(stringEtTextMessage + "\uD83D\uDE18")
        }
        binding.imbBtnFaceWithWordsInMounth.setOnClickListener {
            val stringEtTextMessage = binding.etTextMessage.text.toString()
            binding.etTextMessage.setText(stringEtTextMessage + "\uD83E\uDD2C")
        }
        binding.imbBtnFaceWithHearts.setOnClickListener {
            val stringEtTextMessage = binding.etTextMessage.text.toString()
            binding.etTextMessage.setText(stringEtTextMessage + "\uD83D\uDE0D")
        }
        binding.imbBtnFaceClown.setOnClickListener {
            val stringEtTextMessage = binding.etTextMessage.text.toString()
            binding.etTextMessage.setText(stringEtTextMessage + "\uD83E\uDD21")
        }
        binding.imbBtnFaceWithHalo.setOnClickListener {
            val stringEtTextMessage = binding.etTextMessage.text.toString()
            binding.etTextMessage.setText(stringEtTextMessage + "\uD83D\uDE07")
        }


        binding.etTextMessage.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.frameLayout.visibility = View.GONE
                binding.imgBtnSendMessage.visibility = View.VISIBLE
                binding.imgBtnRecordAudio.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        job = CoroutineScope(Dispatchers.Main).launch {
           while (isActive) {
                listenForMessages(currentUserId.toString(), ids.toString()){ messages ->
                    if(messageList != messages) {
                        messageList = messages
                        if(chatTextSize != null) binding.rvMessage.adapter = MessageAdapter(currentUserId.toString(),messages, chatTextSize!!)
                        else binding.rvMessage.adapter = MessageAdapter(currentUserId.toString(),messages, 16)
                    }
                    if(countHeight == 0) countHeight = 1
                }
               if(countHeight == 1) {
                   countHeight = 2
                   binding.rvMessage.post{
                       val lastPosition = messageList.size-1
                       binding.rvMessage.scrollToPosition(lastPosition)
                   }
               }
               if(binding.etTextMessage.text.toString() == "") {
                   if(binding.imgBtnStopAudio.visibility == View.GONE) {
                       binding.imgBtnSendMessage.visibility = View.GONE
                       binding.imgBtnRecordAudio.visibility = View.VISIBLE
                   } else {
                       binding.imgBtnSendMessage.visibility = View.GONE
                       binding.imgBtnRecordAudio.visibility = View.GONE
                   }
               }
               delay(100)
            }
        }

        binding.imgBtnVideoCallWithUser.setOnClickListener {
            val action = ChatUserFragmentDirections.actionChatUserFragmentToCallFragment()
            navController.navigate(action)
        }
        binding.imgBtncallWithUser.setOnClickListener {
            navController.navigate(R.id.action_chatUserFragment_to_audioCallFragment)
        }

        binding.imgBtnRecordAudio.setOnClickListener {
            binding.imgBtnRecordAudio.visibility = View.GONE
            binding.imgBtnSendMessage.visibility = View.GONE
            binding.imgBtnStopAudio.visibility = View.VISIBLE
            startRecording()
        }

        return view
    }

    private fun startRecording() {
        audioFilePath = "${requireActivity().externalCacheDir?.absolutePath}/${UUID.randomUUID()}.3gp"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }

        binding.imgBtnStopAudio.setOnClickListener {
            binding.imgBtnRecordAudio.visibility = View.VISIBLE
            binding.imgBtnSendMessage.visibility = View.GONE
            binding.imgBtnStopAudio.visibility = View.GONE
            stopRecording()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        uploadAudioToFirebase(audioFilePath!!)
    }
    private fun uploadAudioToFirebase(filePath: String) {
        val fileUri = Uri.fromFile(File(filePath))
        val storageRef = storage.reference.child("audio/${fileUri.lastPathSegment}")
        storageRef.putFile(fileUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.e("STOOPsp", uri.toString())
                sendMessage(currentUserId.toString(), ids.toString(), uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }


    private fun convertDpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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
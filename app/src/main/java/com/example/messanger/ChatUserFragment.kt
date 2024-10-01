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
    private var id = 0
    var imageUrl = ""
    var countHeight = 0

   // private val REQUEST_VIDEO_CAPTURE = 1
   // private lateinit var videoUri: Uri

    private lateinit var mediaPlayer: MediaPlayer
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
   // private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if(countHeight == 1) {
                binding.frameLayout.visibility = View.GONE
                binding.etTextMessage.clearFocus()
                val params = binding.rootLayout.layoutParams
                params.height = (params.height + convertDpToPx(370))
                binding.rootLayout.layoutParams = params
                binding.rootLayout.requestLayout()
                countHeight = 0
            }
        }

        binding.imgBtnSendMessage.setOnClickListener {
            sendMessage(currentUserId.toString(), id.toString(), binding.etTextMessage.text.toString())
            binding.etTextMessage.setText("")
            binding.etTextMessage.clearFocus()
            hideKeyboard(binding.etTextMessage)
            binding.imgBtnSendMessage.visibility = View.GONE
            binding.imgBtnRecordAudio.visibility = View.VISIBLE

            listenForMessages(currentUserId.toString(), id.toString()){ messages ->
                binding.rvMessage.adapter = MessageAdapter(currentUserId.toString(),messages, chatTextSize!!)
            }
        }

        binding.etTextMessage.setOnFocusChangeListener{ v, hasFocus ->
            if(hasFocus) {
                if(countHeight == 0) {
                    val params = binding.rootLayout.layoutParams
                    params.height = (params.height - convertDpToPx(370))
                    binding.rootLayout.layoutParams = params
                    binding.rootLayout.requestLayout()
                    countHeight = 1
                }
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
                listenForMessages(currentUserId.toString(), id.toString()){ messages ->
                   binding.rvMessage.adapter = MessageAdapter(currentUserId.toString(),messages, chatTextSize!!)
                }
               delay(10000)
            }
        }


        binding.imgBtnRecordAudio.setOnClickListener {
            binding.imgBtnRecordAudio.visibility = View.GONE
            binding.imgBtnSendMessage.visibility = View.GONE
            binding.imgBtnStopAudio.visibility = View.VISIBLE
            startRecording()

        }

//        binding.imgBtnRecordVideo.setOnClickListener {
//            binding.imgBtnRecordVideo.visibility = View.GONE
//            binding.imgBtnSendMessage.visibility = View.GONE
//            binding.imgBtnStopVideo.visibility = View.VISIBLE
//            dispatchTakeVideoIntent()
//        }
//        binding.imgBtnStopVideo.setOnClickListener {
//            uploadVideoToFirebase()
//        }
        return view
    }

//    private fun uploadVideoToFirebase() {
//        val storage = FirebaseStorage.getInstance()
//        val storageRef = storage.reference
//        val videoRef = storageRef.child("videos/${UUID.randomUUID()}.mp4")
//
//        val uploadTask = videoRef.putFile(videoUri)
//
//        uploadTask.addOnSuccessListener {
//            videoRef.downloadUrl.addOnSuccessListener { uri ->
//                sendMessage(currentUserId.toString(), id.toString(), uri.toString())
//            }
//        }.addOnFailureListener {
//        }
//    }
//
//    private fun dispatchTakeVideoIntent() {
//        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
//            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
//            videoUri = data?.data!!
//            uploadVideoToFirebase()
//        }
//    }

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
                sendMessage(currentUserId.toString(), id.toString(), uri.toString())
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

    private fun playAudio() {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        val audioRef = storageRef.child("path_to_your_audio_file.mp3") // Укажите путь к вашему аудиофайлу в Firebase Storage

        audioRef.downloadUrl.addOnSuccessListener { uri ->
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(uri.toString())
                mediaPlayer.prepare()
                mediaPlayer.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.addOnFailureListener {
            // Обработка ошибок
        }
    }

}
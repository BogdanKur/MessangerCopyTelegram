package com.example.messanger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.messanger.YourProfileFragment.Companion.aboutYourSelves
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.YourProfileFragment.Companion.name
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.databinding.FragmentSettingsBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var job: Job
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    uploadImageToFirebase(it)
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().reference
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                binding.tvPhoneUser.text = phoneNumber
                binding.tvNameUser.text = name
                binding.tvNameUser1.text = name
                binding.tvAboutYours.text = aboutYourSelves
                delay(1000)
            }
        }
        loadPicture()

        binding.fab.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(pickImageIntent)
            loadPicture()
        }

        binding.btnSettingsOfChats.setOnClickListener {
            navController.navigate(R.id.action_settingsFragment_to_settingsOfChatsFragment)
        }
        binding.btnFoldersWithChat.setOnClickListener {
            navController.navigate(R.id.action_settingsFragment_to_foldersWithChatsFragment)
        }

        return view
    }

    fun loadPicture() {
        val userRef = databaseRef.child("user").child(currentUserId.toString()).child("profilePicture")
        userRef.get().addOnSuccessListener { snap->
            if(snap.exists()) {
                val profilePicture = snap.value.toString()
                Glide.with(this)
                    .load(profilePicture)
                    .into(binding.imgBtnProfilePicture)
                Glide.with(this)
                    .load(profilePicture)
                    .into(binding.ivAppBar)

            }
        }
    }

    fun saveImageToUrl(imageUrl: String) {
        val userRef = databaseRef.child("user").child(currentUserId.toString())
        userRef.child("profilePicture").setValue(imageUrl)
    }

    fun uploadImageToFirebase(imageUri: Uri) {
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageToUrl(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Ошибка загрузки изображения: ${exception.message}")
        }
    }

}
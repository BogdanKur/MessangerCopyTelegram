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
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.messanger.databinding.FragmentYourProfileBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class YourProfileFragment : Fragment() {

    private var _binding: FragmentYourProfileBinding? = null
    private val binding get() = _binding!!
    private var job: Job? = null
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    companion object{
        var currentUserId by Delegates.notNull<Int>()
        var phoneNumber: String? = ""
        var name: String? = ""
        var surname: String? = ""
        var aboutYourSelves: String? = ""
    }

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
        _binding = FragmentYourProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().reference
        val navController = findNavController()
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.action_yourProfileFragment_to_messageMainFragment)
        }

        binding.btnBackProfile.setOnClickListener {
            navController.navigate(R.id.action_yourProfileFragment_to_messageMainFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            navController.navigate(R.id.action_yourProfileFragment_to_editYourProfileFragment)
        }

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                binding.tvPhoneUser.text = phoneNumber
                binding.tvNameUser.text = name
                binding.tvNameUser1.text = name
                binding.tvAboutYours.text = aboutYourSelves
                binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    binding.linearLayout.visibility = if (verticalOffset == 0) View.GONE else View.VISIBLE
                })
                delay(1000)
            }
        }
        loadProfilePicture()

        binding.fab.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(pickImageIntent)
            loadProfilePicture()
        }

        return view
    }

    private fun loadProfilePicture() {
        val usersRef = databaseRef.child("user").child(currentUserId.toString()).child("profilePicture")
        usersRef.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.exists()) {
                val profilePictureUrl = querySnapshot.value.toString()
                Glide.with(this)
                    .load(profilePictureUrl)
                    .into(binding.imgBtnProfilePicture)
                Glide.with(this)
                    .load(profilePictureUrl)
                    .into(binding.ivAppBar)

            }
        }.addOnFailureListener { exception ->
            Log.e("LoadProfilePicture", "Error getting profile picture: ", exception)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToDatabase(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Ошибка загрузки изображения: ${exception.message}")
            }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userRef = databaseRef.child("user").child(currentUserId.toString())
        userRef.child("profilePicture").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("Firebase", "URL изображения успешно сохранен в базе данных.")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Ошибка сохранения URL в базе данных: ${exception.message}")
            }
    }
}

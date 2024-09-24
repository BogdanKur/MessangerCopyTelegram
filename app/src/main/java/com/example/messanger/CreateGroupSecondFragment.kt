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
import com.bumptech.glide.Glide
import com.example.messanger.CreateGroupFragment.Companion.listOfUsers1
import com.example.messanger.YourProfileFragment.Companion
import com.example.messanger.YourProfileFragment.Companion.aboutYourSelves
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.YourProfileFragment.Companion.surname
import com.example.messanger.databinding.FragmentCreateGroupSecondBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.properties.Delegates

class CreateGroupSecondFragment : Fragment() {
    private var _binding: FragmentCreateGroupSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null
    companion object{
        var currentGroupId by Delegates.notNull<String>()
        var name: String? = ""
        var numberOfuser: UserOfMessanger? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                imageUri?.let {
                    Glide.with(this)
                        .load(imageUri)
                        .into(binding.imgBtnProfilePicture)
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateGroupSecondBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().reference

        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Создать группу"
            binding.toolbar.title = destination.label
        }

        binding.fab.setOnClickListener {
            name = binding.etGroupName.text.toString()
            if (imageUri != null) {
                uploadImageToFirebase(imageUri!!)
            } else {
                Log.e("ImageUri", "Изображение не выбрано!")
            }
            val bundle = Bundle().apply {
                putString("IMAGE", imageUri.toString())
                putString("users", numberOfuser.toString())
                putString("name", name.toString())
            }
            navController.navigate(R.id.action_createGroupSecondFragment_to_groupChatUsersfragment, bundle)
        }

        binding.imgBtnProfilePicture.setOnClickListener {
            name = binding.etGroupName.text.toString()
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(pickImageIntent)
        }
        return view
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
        val userRef = databaseRef.child("user")
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userRef.orderByChild("number").equalTo(currentUser?.phoneNumber).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        numberOfuser = userSnapshot.getValue(UserOfMessanger::class.java)
                    }
                } else {
                    Log.e("userId", "Пользователь не найден")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        val groupRef = databaseRef.child("group").child(currentUserId.toString())

        groupRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren()) {
                    for(snap in snapshot.children) {
                        if(snap.child("name").value.toString() != name) {
                            groupRef.child("name").setValue(name)
                            groupRef.child("users").child(numberOfuser!!.name.toString()).setValue(numberOfuser)
                            Log.e("listusers", listOfUsers1!!.get(0).name.toString())
                            groupRef.child("users").child(listOfUsers1!!.get(0).name.toString()).setValue(listOfUsers1!!.get(0))
                            groupRef.child("profilePicture").setValue(imageUrl)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "URL изображения успешно сохранен в базе данных.")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firebase", "Ошибка сохранения URL в базе данных: ${exception.message}")
                                }
                        }
                    }
                } else {
                    Log.e("listusers1", listOfUsers1.toString())
                    groupRef.child("name").setValue(name)
                    groupRef.child("users").child(numberOfuser!!.name.toString()).setValue(numberOfuser)
                    groupRef.child("users").child(listOfUsers1!!.get(0).name.toString()).setValue(listOfUsers1!!.get(0))
                    groupRef.child("profilePicture").setValue(imageUrl)
                        .addOnSuccessListener {
                            Log.d("Firebase", "URL изображения успешно сохранен в базе данных.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Ошибка сохранения URL в базе данных: ${exception.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

}
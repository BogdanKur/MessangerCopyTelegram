package com.example.messanger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.ChatFromFolderFragment.Companion.listOfUserForFolder
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentNewFolderBinding
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

class NewFolderFragment : Fragment() {
    private var _binding: FragmentNewFolderBinding? = null
    private val binding get() = _binding!!
    private lateinit var job: Job
    val databaseRef = FirebaseDatabase.getInstance().reference
    val usersRef = databaseRef.child("user")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewFolderBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Новая папка"
            binding.toolbar.title = destination.label
        }
        binding.btnCreateNewFolder.setOnClickListener {
            navController.navigate(R.id.action_newFolderFragment_to_chatFromFolderFragment)
        }
        binding.btnSaveNewFolder.setOnClickListener {
            val folder = FolderClass(binding.etNameOfFolder.text.toString(), listOfUserForFolder)
            usersRef.child(currentUserId.toString()).child("folders").child(binding.etNameOfFolder.text.toString()).setValue(folder)
            navController.navigate(R.id.action_newFolderFragment_to_foldersWithChatsFragment)
        }

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val adapter = NewFolderAdapter(listOfUserForFolder)
                binding.rvChatsFolders.adapter = adapter
                delay(3000)
            }
        }

        return view
    }

}
package com.example.messanger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentFoldersWithChatsBinding
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


class FoldersWithChatsFragment : Fragment() {
    private var _binding: FragmentFoldersWithChatsBinding? = null
    private val binding get() = _binding!!
    val databaseRef = FirebaseDatabase.getInstance().reference
    val userRef = databaseRef.child("user")
    var listOfNames = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoldersWithChatsBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Папки с чатами"
            binding.toolbar.title = destination.label
        }
        binding.btnCreateNewFolder.setOnClickListener {
            navController.navigate(R.id.action_foldersWithChatsFragment_to_newFolderFragment)
        }

        userRef.child(currentUserId.toString()).child("folders").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children) {
                    listOfNames.add(snap.child("name").value.toString())
                }
                val adapter = FoldersWithChatsAdapter(listOfNames)
                binding.rvChatsFolders.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        return view
    }

}
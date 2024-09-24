package com.example.messanger

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.databinding.FragmentChatFromFolderBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFromFolderFragment : Fragment(), AddNewChatsInterface {
    private var _binding: FragmentChatFromFolderBinding? = null
    private val binding get() = _binding!!
    val databaseRef = FirebaseDatabase.getInstance().reference
    companion object {
        val listOfUserForFolder = mutableListOf<MessageTypeClass>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatFromFolderBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Чаты в папке"
            binding.toolbar.title = destination.label
        }
        binding.etSearchUsersAndGroup.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val usersRef = databaseRef.child("user")
                val groupsRef = databaseRef.child("group")
                val listOfChatsUser = mutableListOf<MessageTypeClass>()
                usersRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(child in snapshot.children) {
                            if(child.child("name").value.toString().contains(s.toString(), ignoreCase = true)) {
                                val user= MessageTypeClass(
                                    child.child("name").value.toString(),
                                    child.child("profilePicture").value.toString(),
                                    child.child("number").value.toString(),
                                    child.child("userId").value.toString())
                                listOfChatsUser.add(user)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

                groupsRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(child in snapshot.children) {
                            if(child.child("name").value.toString().contains(s.toString(), ignoreCase = true)) {
                                val user= MessageTypeClass(
                                    child.child("name").value.toString(),
                                    child.child("profilePicture").value.toString(),
                                    child.child("number").value.toString(),
                                    child.key.toString())
                                listOfChatsUser.add(user)
                            }
                        }
                        val adapter = ChatFromFolderAdapter(listOfChatsUser, this@ChatFromFolderFragment)
                        binding.rvChatsFolders.adapter = adapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        return view
    }

    override fun onButtonClickForChatFromFolderUsers(user: MessageTypeClass) {
        listOfUserForFolder.add(user)
    }

}
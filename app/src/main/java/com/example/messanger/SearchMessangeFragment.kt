package com.example.messanger

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentSearchMessangeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchMessangeFragment : Fragment() {
    private var _binding: FragmentSearchMessangeBinding? = null
    private val binding get() = _binding!!
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val listOfUsers = mutableListOf<MessageTypeClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentSearchMessangeBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = ""
            binding.toolbar.title = destination.label
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.action_searchMessageFragment_to_messageMainFragment)
        }
        binding.imgBtnBackToMessageMain.setOnClickListener {
            navController.navigate(R.id.action_searchMessageFragment_to_messageMainFragment)
        }
        binding.etSearchUsers.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString() != "") {
                    val userRef = databaseRef.child("user")
                    val groupRef = databaseRef.child("group")
                    var userTextSize = 16
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (snap in snapshot.children) {
                                if (snap.child("userId").value.toString() != currentUserId.toString()
                                    && snap.child("name").value.toString().contains(s.toString(), ignoreCase = true)) {
                                    val name = snap.child("name").value.toString()
                                    val image = snap.child("profilePicture").value.toString()
                                    val number = snap.child("number").value.toString()
                                    val ids = snap.child("userId").value.toString()
                                    userTextSize = snap.child("textSize").value.toString().toInt()

                                    // Добавляем пользователя только если его нет в списке
                                    if (listOfUsers.none { it.id == ids }) {
                                        listOfUsers.add(MessageTypeClass(name, image, number, ids))
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                    groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                if (child.child("name").value.toString().contains(s.toString(), ignoreCase = true)) {
                                    val name = child.child("name").value.toString()
                                    val image = child.child("profilePicture").value.toString()
                                    val number = child.child("number").value.toString()
                                    val ids = child.key.toString()

                                    // Добавляем группу только если её нет в списке
                                    if (listOfUsers.none { it.id == ids }) {
                                        listOfUsers.add(MessageTypeClass(name, image, number, ids))
                                    }
                                }
                            }

                            val adapter = SearchMessageAdapter(listOfUsers, navController, userTextSize)
                            binding.rvSearchesUsers.adapter = adapter
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

         return  view
    }


}
package com.example.messanger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.databinding.FragmentNewMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlin.math.log

class NewMessageFragment : Fragment() {
    private var _binding: FragmentNewMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewMessageBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()

        fetchUserNumbers { userNumbers->
            val adapter = ListContactsAdapter(userNumbers, navController)
            binding.rvListContacts.adapter = adapter
        }

        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            destination.label = "Новое сообщение"
            binding.toolbar.title = destination.label
        }

        return view
    }

    fun fetchUserNumbers(callback: (List<UserOfMessanger>) -> Unit) {
        val listOfUserNumbers = mutableListOf<UserOfMessanger>()
        AuthAndRegMenuFragment.user.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue<UserOfMessanger>()
                    if (user != null) {
                        if(user.number != phoneNumber) {
                            user?.let {
                                Log.e("fs", listOfUserNumbers.toString())
                                listOfUserNumbers.add(it)
                            }
                        }
                    }
                }
                callback(listOfUserNumbers)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

}
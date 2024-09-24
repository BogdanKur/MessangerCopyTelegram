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
import com.example.messanger.CreateGroupSecondFragment.Companion.currentGroupId
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.databinding.FragmentCreateGroupBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateGroupFragment : Fragment(), CreateGroupInterface {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: CreateGroupAdapter
    lateinit var job: Job
    var listOfUsers: MutableList<UserOfMessanger>? = null
    companion object{
        var listOfUsers1: MutableList<UserOfMessanger>? = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()

        navController.addOnDestinationChangedListener{_, destination, _ ->
            destination.label = "Создать группу"
            binding.toolbar.title = destination.label
        }

        binding.toolbar.setupWithNavController(navController)

        addPeopleToGroup { list->
            adapter = CreateGroupAdapter(list, this)
            binding.rvCreateGroupFutureMembers.adapter = adapter
        }

        binding.etnameOfUser.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
                binding.rvCreateGroupFutureMembers.adapter = adapter
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.fab.setOnClickListener{
            navController.navigate(R.id.action_createGroupFragment_to_createGroupSecondFragment)
            currentGroupId = binding.etnameOfUser.text.toString()
        }


        return view
    }

    fun addPeopleToGroup(callback: (List<UserOfMessanger>) -> Unit) {
        listOfUsers = mutableListOf<UserOfMessanger>()
        AuthAndRegMenuFragment.user.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue<UserOfMessanger>()
                    if (user != null) {
                        if(user.number != phoneNumber) {
                            user?.let {
                                listOfUsers!!.add(it)
                            }
                        }
                    }
                }
                callback(listOfUsers!!)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    override fun onButtonClick(string: String) {
        if (listOfUsers1 == null) {
            listOfUsers1 = mutableListOf()
        }
        binding.etnameOfUser.setText(string)
        listOfUsers?.forEach { child ->
            if (string == child.name) {
                listOfUsers1?.add(child)
            }
        } ?: Log.e("srrk", "listOfUsers is null")
    }

}
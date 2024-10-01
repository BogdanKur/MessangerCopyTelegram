package com.example.messanger

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.databinding.FragmentNewMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.Manifest

class NewMessageFragment : Fragment() {
    private var _binding: FragmentNewMessageBinding? = null
    private val binding get() = _binding!!
    private var contactsList = mutableListOf<String>()

    companion object {
        private const val REQUEST_CODE_READ_CONTACTS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewMessageBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
        } else {
            getContacts()
        }

        fetchUserNumbers { userNumbers->
            val adapter = ListContactsAdapter(userNumbers, navController)
            binding.rvListContacts.adapter = adapter
        }

        binding.btnAddGroup.setOnClickListener {
            navController.navigate(R.id.action_newMessageFragment_to_createGroupFragment)
        }



        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            destination.label = "Новое сообщение"
            binding.toolbar.title = destination.label
        }

        return view
    }

    private fun getContacts() {
        val contentResolver: ContentResolver = requireContext().contentResolver

        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            val indexNumber = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val phoneNumber = it.getString(indexNumber)
                contactsList.add(phoneNumber)
            }
        }

        contactsList.forEach { contact ->
            Log.e("CONTACTSOSI", contact)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getContacts()
            } else {
                Toast.makeText(requireContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun fetchUserNumbers(callback: (List<UserOfMessanger>) -> Unit) {
        val listOfUserNumbers = mutableListOf<UserOfMessanger>()
        AuthAndRegMenuFragment.user.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue<UserOfMessanger>()
                    if (user != null) {
                        for(numb in contactsList) {
                            var contacts = numb
                            if (contacts.startsWith("8")) {
                                contacts = contacts.replaceFirst("8", "+7")
                            }
                            if(user.number ==  contacts && user.number != phoneNumber) {
                                user?.let {
                                    Log.e("fs", listOfUserNumbers.toString())
                                    if(listOfUserNumbers.size != 0) {
                                        for(i in listOfUserNumbers.indices) {
                                            if(!listOfUserNumbers.contains(it)) {
                                                listOfUserNumbers.add(it)
                                            }
                                        }
                                    } else {
                                        listOfUserNumbers.add(it)
                                    }
                                }
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
package com.example.messanger

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.aboutYourSelves
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.YourProfileFragment.Companion.name
import com.example.messanger.YourProfileFragment.Companion.surname
import com.example.messanger.databinding.FragmentEditYourProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EditYourProfileFragment : Fragment() {
    private var _binding: FragmentEditYourProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var job: Job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditYourProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()

        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Профиль"
            binding.toolbar.title = destination.label
        }
        binding.btnSaveEdit.setOnClickListener {
            val usersRef = FirebaseDatabase.getInstance().reference
            if(binding.etNameEdit.text.toString() != ""){
                usersRef.child("user").child(currentUserId.toString()).child("name").setValue(binding.etNameEdit.text.toString())
                name = binding.etNameEdit.text.toString()
            }
            if(binding.etSurnameEdit.text.toString() != "") {
                usersRef.child("user").child(currentUserId.toString()).child("surname").setValue(binding.etSurnameEdit.text.toString())
                surname = binding.etSurnameEdit.text.toString()
            }
            if(binding.etAboutYourSelvesEdit.text.toString() != "") {
                usersRef.child("user").child(currentUserId.toString()).child("aboutYourSelves").setValue(binding.etAboutYourSelvesEdit.text.toString())
                aboutYourSelves = binding.etAboutYourSelvesEdit.text.toString()
            }
            navController.navigate(R.id.action_editYourProfileFragment_to_yourProfileFragment)
        }
        binding.etNameEdit.setText(name)
        binding.etSurnameEdit.setText(surname)
        binding.etAboutYourSelvesEdit.setText(aboutYourSelves)
        binding.etNameEdit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSaveEdit.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.etSurnameEdit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSaveEdit.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.etAboutYourSelvesEdit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSaveEdit.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        job = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {

                    delay(1000)
                }
        }

        return view
    }

}
package com.example.messanger

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.messanger.AuthAndRegMenuFragment.Companion.auth
import com.example.messanger.AuthAndRegMenuFragment.Companion.enterCode
import com.example.messanger.AuthAndRegMenuFragment.Companion.verId
import com.example.messanger.YourProfileFragment.Companion.aboutYourSelves
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.YourProfileFragment.Companion.name
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.YourProfileFragment.Companion.surname
import com.example.messanger.databinding.FragmentBottomShitAuthVerificationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class BottomShitAuthVerificationFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentBottomShitAuthVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomShitAuthVerificationBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    fun verifyCode() {
        val code = enterCode.trim()
        val credential = PhoneAuthProvider.getCredential(verId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnEtCodeEnter.setOnClickListener {
            enterCode = binding.etCode.text.toString()
            if(enterCode != "") {
                verifyCode()
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                Log.e("fdas12daasasasd", currentUser.toString())
                if (currentUser != null) {
                    phoneNumber = currentUser.phoneNumber
                    if (phoneNumber != null) {
                        val  databaseRef = FirebaseDatabase.getInstance().reference
                        val userRef = databaseRef.child("user")

                        userRef.orderByChild("number").equalTo(phoneNumber)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (userSnapshot in snapshot.children) {
                                            currentUserId = userSnapshot.key!!.toInt()
                                            Log.e("fdas12daasasasd", currentUserId.toString())
                                            name = userSnapshot.child("name").getValue<String?>().toString()
                                            surname = userSnapshot.child("surname").getValue<String?>().toString()
                                            aboutYourSelves = userSnapshot.child("aboutYourSelves").getValue<String?>().toString()
                                        }
                                    } else {
                                        Log.e("userId", "Пользователь не найден")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("DatabaseError", error.message)
                                }
                            })
                    }
                }
                findNavController().navigate(R.id.action_authAndRegFragment_to_messageMainFragment)
                dismiss()
            }
        }
    }

}
package com.example.messanger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.messanger.AuthAndRegMenuFragment.Companion.auth
import com.example.messanger.AuthAndRegMenuFragment.Companion.enterCode
import com.example.messanger.AuthAndRegMenuFragment.Companion.verId
import com.example.messanger.databinding.FragmentBottomShitAuthVerificationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

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
                findNavController().navigate(R.id.action_authAndRegFragment_to_messageMainFragment)
                dismiss()
            }
        }
    }

}
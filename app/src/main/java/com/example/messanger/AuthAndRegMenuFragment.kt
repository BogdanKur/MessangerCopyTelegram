package com.example.messanger

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentAuthAndRegMenuBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthAndRegMenuFragment : Fragment() {
    private var _binding: FragmentAuthAndRegMenuBinding? = null
    private val binding get() = _binding!!
    private var job: Job? = null
    private val intent = Intent()
    lateinit var viewModel: AuthAndRegMenuViewModel
    lateinit var numb: String
    lateinit var numb1: String
    private lateinit var verificationId: String

    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val db:  FirebaseDatabase = FirebaseDatabase.getInstance()
        val user: DatabaseReference = db.getReference("user")
        var numberOfAuthUser : String = ""
        var enterCode = ""
        var verId = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthAndRegMenuBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        viewModel = ViewModelProvider(this).get(AuthAndRegMenuViewModel::class.java)

        binding.editText1.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val drawableRussiaIcon = resources.getDrawable(R.drawable.icon_russia)
                val drawableBelorussiaIcon = resources.getDrawable(R.drawable.icon_belorussia)
                val drawableAustralia = resources.getDrawable(R.drawable.icon_australia)
                val drawableIconRight = resources.getDrawable(R.drawable.icon_right)
                Log.e("s", s.toString())
                when (s.toString()) {
                    "61" -> {
                        binding.btnCountryChooseAuthAndRegFragment.text = "Австралия"
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(drawableAustralia, null, drawableIconRight, null)
                    }
                    "375" -> {
                        binding.btnCountryChooseAuthAndRegFragment.text = "Беларусь"
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(drawableBelorussiaIcon, null, drawableIconRight, null)
                    }
                    "7" -> {
                        binding.btnCountryChooseAuthAndRegFragment.text = "Россия"
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(drawableRussiaIcon, null, drawableIconRight, null)
                    }
                    else -> {
                        binding.btnCountryChooseAuthAndRegFragment.text = ""
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableIconRight, null)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.flBtnRegOrAuth.setOnClickListener{
            numb1 = "${binding.tvPlus.text} ${binding.editText1.text} ${binding.editText2.text}" ?: ""
            numb = numb1.replace("\\s+".toRegex(), "") ?: ""

            if(numb != "" ) {
                user.orderByChild("number").equalTo(numb).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Log.e("fuckYesNumber", numb)
                            sendVerificationCode()
                            val bottomShitAuthVerificationFragment = BottomShitAuthVerificationFragment()
                            bottomShitAuthVerificationFragment.show(parentFragmentManager, bottomShitAuthVerificationFragment.tag)
                            Toast.makeText(context, "Пользователь с таким номером уже существует", Toast.LENGTH_SHORT)
                        } else {
                            viewModel.addNewUser(numb)
                            navController.navigate(R.id.action_authAndRegFragment_to_messageMainFragment)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Ошибка при проверке пользователя", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        startFlow()


        binding.btnCountryChooseAuthAndRegFragment.setOnClickListener {
            navController.navigate(R.id.action_authAndRegFragment_to_countryChooseFromAuthAndRegFragment)
        }
        return view
    }

    private fun sendVerificationCode() {
        if (!numb.toString().contains("@", ignoreCase = true)) {
            Log.e("numberToAuth", numb)
            val usersRef = user

            usersRef.orderByChild("number").equalTo(numb)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            val userId = userSnapshot.key
                            numberOfAuthUser = userId!!
                            currentUserId = numberOfAuthUser.toInt()
                            Log.e("numbv", numberOfAuthUser)
                            Log.e("User ID:", userId!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Error getting user ID: ", error.message)
                    }
                })

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(numb)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(context, "Ошибка: неверный номер телефона", Toast.LENGTH_LONG).show()
                        Log.e("Error404", e.message ?: "Неизвестная ошибка")
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        this@AuthAndRegMenuFragment.verificationId = verificationId
                        verId = verificationId
                        Toast.makeText(context, "Код отправлен", Toast.LENGTH_SHORT).show()
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(context, "Пожалуйста, введите корректный номер телефона", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Успешно авторизован", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                }
            }
    }



    @SuppressLint("SetTextI18n")
    private fun startFlow() {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                var countryName = ""
                arguments?.let { bundle ->
                    countryName = bundle.getString("countryName").toString()
                }
                val drawableRussiaIcon = resources.getDrawable(R.drawable.icon_russia)
                val drawableBelorussiaIcon = resources.getDrawable(R.drawable.icon_belorussia)
                val drawableAustralia = resources.getDrawable(R.drawable.icon_australia)
                val drawableIconRight = resources.getDrawable(R.drawable.icon_right)
                if(binding.editText1.text.toString() == "") {
                    binding.btnCountryChooseAuthAndRegFragment.text = countryName
                }
                when (countryName) {
                    "Австралия" -> {
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(drawableAustralia, null, drawableIconRight, null)
                        binding.editText1.setText("61")
                        job?.cancel()
                    }
                    "Беларусь" -> {
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(drawableBelorussiaIcon, null, drawableIconRight, null)
                        binding.editText1.setText("375")
                        job?.cancel()
                    }
                    "Россия" -> {
                        binding.btnCountryChooseAuthAndRegFragment.setCompoundDrawablesWithIntrinsicBounds(drawableRussiaIcon, null, drawableIconRight, null)
                        binding.editText1.setText("7")
                        job?.cancel()
                    }
                }

                delay(1000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

}
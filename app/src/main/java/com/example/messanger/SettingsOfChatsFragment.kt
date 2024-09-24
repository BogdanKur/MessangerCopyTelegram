package com.example.messanger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.databinding.FragmentSettingsOfChatsBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.properties.Delegates


class SettingsOfChatsFragment : Fragment() {
    private var _binding: FragmentSettingsOfChatsBinding? = null
    private val binding get() = _binding!!
    var textSize by Delegates.notNull<Int>()
    lateinit var db: DatabaseReference
    lateinit var usersRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsOfChatsBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        db = FirebaseDatabase.getInstance().reference
        usersRef = db.child("user").child(currentUserId.toString())
        usersRef.child("textSize").get().addOnSuccessListener { snapy ->
            binding.seekBarTextOfMessangesSize.progress = snapy.value.toString().toInt()
        }
        navController.addOnDestinationChangedListener{_,destiantion,_ ->
            destiantion.label = "Настройки чатов"
            binding.toolbar.title = destiantion.label
        }
        binding.toolbar.setupWithNavController(navController)
        binding.tvSizeOfText.text = binding.seekBarTextOfMessangesSize.progress.toString()
        binding.seekBarTextOfMessangesSize.setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSizeOfText.text = progress.toString()
                textSize = progress
                usersRef.child("textSize").setValue(textSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        return view
    }

}
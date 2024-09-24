package com.example.messanger

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.messanger.databinding.FragmentStartMenuAuthAndRegBinding
import android.Manifest

class StartMenuAuthAndRegFragment : Fragment() {
    private var _binding: FragmentStartMenuAuthAndRegBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CALL_PHONE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartMenuAuthAndRegBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()

        binding.btnStartMessaging.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            }
            navController.navigate(R.id.action_startMenuAuthAndRegFragment_to_authAndRegFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.example.messanger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.messanger.databinding.FragmentSearchMessangeBinding

class SearchMessangeFragment : Fragment() {
    private var _binding: FragmentSearchMessangeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentSearchMessangeBinding.inflate(inflater, container, false)
        val view = binding.root

         return  view
    }


}
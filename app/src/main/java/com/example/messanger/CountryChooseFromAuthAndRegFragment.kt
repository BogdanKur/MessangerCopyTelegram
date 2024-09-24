package com.example.messanger

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.databinding.FragmentCountryChooseFromAuthAndRegBinding
import com.google.android.material.internal.ViewUtils.hideKeyboard

class CountryChooseFromAuthAndRegFragment : Fragment() {
    private var _binding: FragmentCountryChooseFromAuthAndRegBinding? = null
    private val binding get() = _binding!!



    @SuppressLint("RestrictedApi", "ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCountryChooseFromAuthAndRegBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_, destination, _ ->
            destination.label = "Выберите страну"
            binding.toolbar.title = destination.label
        }
        binding.btnSearchCountryChoose.setOnClickListener{
            binding.frametLayoutInToolbar.visibility = View.VISIBLE
            binding.etCountrySearch.visibility = View.VISIBLE
            binding.imgBtnBackToToolbar.visibility = View.VISIBLE
            binding.etCountrySearch.setFocusableInTouchMode(true);
            binding.etCountrySearch.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etCountrySearch, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.imgBtnClearEditText.setOnClickListener{
            binding.etCountrySearch.setText("")
            binding.imgBtnClearEditText.visibility = View.GONE
        }
        binding.imgBtnBackToToolbar.setOnClickListener {
            binding.imgBtnBackToToolbar.visibility = View.GONE
            binding.frametLayoutInToolbar.visibility = View.GONE
            binding.etCountrySearch.visibility = View.GONE
            binding.etCountrySearch.setText("")
            hideKeyboard(binding.etCountrySearch)
        }

        val drawableRussiaIcon = resources.getDrawable(R.drawable.icon_russia)
        val drawableBelorussiaIcon = resources.getDrawable(R.drawable.icon_belorussia)
        val drawableAustralia = resources.getDrawable(R.drawable.icon_australia)

        val countryChooseClassList = listOf(countryChooseClass("Австралия", "+61", drawableAustralia),
            countryChooseClass("Беларусь", "+375", drawableBelorussiaIcon),
            countryChooseClass("Россия", "+7", drawableRussiaIcon)
        )
        val adapter = context?.let { CountryChooseAdapter(countryChooseClassList, it, navController) }
        binding.rvCountryChoose.adapter = adapter
        binding.etCountrySearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter(s.toString())
                binding.imgBtnClearEditText.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

}
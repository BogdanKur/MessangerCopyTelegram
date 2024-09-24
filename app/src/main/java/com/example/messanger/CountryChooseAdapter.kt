package com.example.messanger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView

class CountryChooseAdapter(val countryChooseClass: List<countryChooseClass>, val context: Context, val navController: NavController): RecyclerView.Adapter<CountryChooseAdapter.CountryChooseViewHolder>() {
    private var filterCountryChooseClass = countryChooseClass

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filterCountryChooseClass = if(query.isEmpty()) {
            countryChooseClass
        } else {
            countryChooseClass.filter { it.countryName.contains(query, true) }
        }
        notifyDataSetChanged()
    }

    class CountryChooseViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.btnCountryChooseRV)
        val textView = view.findViewById<TextView>(R.id.tvCountryCodeRV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryChooseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_country_choose, parent, false)
        return CountryChooseViewHolder(view)
    }

    override fun getItemCount(): Int = filterCountryChooseClass.size

    override fun onBindViewHolder(holder: CountryChooseViewHolder, position: Int) {
        holder.button.text = filterCountryChooseClass[position].countryName
        holder.button.setCompoundDrawablesWithIntrinsicBounds(filterCountryChooseClass[position].drawableCountry, null, null, null)
        holder.textView.text = filterCountryChooseClass[position].countryCode
        holder.button.setOnClickListener {
            val intent = Intent(context, AuthAndRegMenuFragment::class.java)
            val bundle = Bundle().apply {
                putString("countryName", filterCountryChooseClass[position].countryName)
            }
            intent.putExtra("countryName", filterCountryChooseClass[position].countryName)
            navController.navigate(R.id.action_countryChooseFromAuthAndRegFragment_to_authAndRegFragment, bundle)
        }
    }
}
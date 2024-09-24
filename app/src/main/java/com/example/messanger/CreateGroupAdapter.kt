package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CreateGroupAdapter(var list: List<UserOfMessanger>, private val listener: CreateGroupInterface): RecyclerView.Adapter<CreateGroupAdapter.CreateGroupViewHolder>() {
    var notSortedList: List<UserOfMessanger> = list
    init {
        notSortedList = list
    }
    class CreateGroupViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val imageView = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }



    fun filter(query: String) {
        list = if(query.isEmpty()) {
            notSortedList
        } else {
            notSortedList.filter { it.name!!.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateGroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_list_contacts, parent, false)
        return CreateGroupViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CreateGroupViewHolder, position: Int) {
        holder.button.setOnClickListener {
            listener.onButtonClick(list[position].name.toString())
        }
        holder.button.text = list[position].name
        Glide.with(holder.imageView.context)
            .asBitmap()
            .load(list[position].profilePicture)
            .into(holder.imageView)
    }
}
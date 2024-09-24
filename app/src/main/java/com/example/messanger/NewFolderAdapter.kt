package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewFolderAdapter(val list: List<MessageTypeClass>): RecyclerView.Adapter<NewFolderAdapter.NewFolderViewHolder>() {
    class NewFolderViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val button = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val imageView = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewFolderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_tab_of_messanges, parent, false)
        return NewFolderViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: NewFolderViewHolder, position: Int) {
        holder.button.text = list[position].nameOfChat
        Glide.with(holder.imageView.context)
            .load(list[position].imgAvaOfChatURL)
            .into(holder.imageView)
    }
}
package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatFromFolderAdapter(val list: List<MessageTypeClass>, val listener: AddNewChatsInterface):RecyclerView.Adapter<ChatFromFolderAdapter.ChatFromFolderViewHolder>() {
    class ChatFromFolderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val imageView = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatFromFolderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_tab_of_messanges, parent, false)
        return ChatFromFolderViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: ChatFromFolderViewHolder, position: Int) {
        holder.button.text = list[position].nameOfChat
        Glide.with(holder.imageView.context)
            .load(list[position].imgAvaOfChatURL)
            .into(holder.imageView)

        holder.button.setOnClickListener {
            listener.onButtonClickForChatFromFolderUsers(list[position])
        }
    }

    override fun getItemCount(): Int = list.size
}
package com.example.messanger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SearchMessageAdapter(val list: List<MessageTypeClass>, val navController: NavController, val textSize: Int): RecyclerView.Adapter<SearchMessageAdapter.SearchMessageViewHolder>() {
    class SearchMessageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val buttonOpenMessage = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val ImageOpenMessage = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchMessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_tab_of_messanges, parent, false)
        return SearchMessageViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchMessageViewHolder, position: Int) {
        holder.buttonOpenMessage.text = list[position].nameOfChat
        Glide.with(holder.ImageOpenMessage.context)
            .load(list[position].imgAvaOfChatURL)
            .into(holder.ImageOpenMessage)

        holder.buttonOpenMessage.setOnClickListener {
            val bundle = Bundle().apply {
                putString("IMAGE_URL", list[position].imgAvaOfChatURL)
                putString("NAMEOFUSER", list[position].nameOfChat)
                putString("NUMBER", list[position].number)
                putString("ID", list[position].id.toString())
                putString("TEXTSIZE", textSize.toString())
            }
            navController.navigate(R.id.action_searchMessageFragment_to_chatUserFragment, bundle)
        }
    }
}
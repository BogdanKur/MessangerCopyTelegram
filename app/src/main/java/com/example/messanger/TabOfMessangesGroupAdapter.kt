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

class TabOfMessangesGroupAdapter(val list: List<MessageTypeClassGroup>, val navController: NavController, val textSize: Int): RecyclerView.Adapter<TabOfMessangesGroupAdapter.TabOfMessangesGroupViewHolder>() {
    class TabOfMessangesGroupViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val buttonOpenMessage = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val ImageOpenMessage = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabOfMessangesGroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_tab_of_messanges, parent, false)
        return TabOfMessangesGroupViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: TabOfMessangesGroupViewHolder, position: Int) {
        holder.buttonOpenMessage.text = list[position].nameOfChat
        Glide.with(holder.ImageOpenMessage.context)
            .asBitmap()
            .load(list[position].imgAvaOfChatURL)
            .into(holder.ImageOpenMessage)

        holder.buttonOpenMessage.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name", list[position].nameOfChat)
                putString("IMAGE", list[position].imgAvaOfChatURL)
                putString("TEXTSIZE", textSize.toString())
            }
            navController.navigate(R.id.action_messageMainFragment_to_groupChatUsersFragment, bundle)
        }
    }
}
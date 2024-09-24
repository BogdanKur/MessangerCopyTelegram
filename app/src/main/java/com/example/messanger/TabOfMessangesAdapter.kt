package com.example.messanger

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class TabOfMessangesAdapter(val list: List<MessageTypeClass>, val navController: NavController, val textSize: Int): RecyclerView.Adapter<TabOfMessangesAdapter.TabOfMessangesViewHolder>() {
    class TabOfMessangesViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val buttonOpenMessage = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val ImageOpenMessage = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabOfMessangesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_tab_of_messanges, parent, false)
        return TabOfMessangesViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: TabOfMessangesViewHolder, position: Int) {
        holder.buttonOpenMessage.text = list[position].nameOfChat
        Glide.with(holder.ImageOpenMessage.context)
            .asBitmap()
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
            navController.navigate(R.id.action_messageMainFragment_to_chatUserFragment, bundle)
        }
    }
}
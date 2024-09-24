package com.example.messanger

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

class ListContactsAdapter(val list: List<UserOfMessanger>, val navController: NavController): RecyclerView.Adapter<ListContactsAdapter.ListContactsViewHolder>() {
    class ListContactsViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.btnRvTabOfMessangesOpenMessage)
        val imageView = view.findViewById<ImageView>(R.id.ivProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListContactsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_list_contacts, parent, false)
        return ListContactsViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ListContactsViewHolder, position: Int) {
        holder.button.setOnClickListener {
            val bundle = Bundle().apply {
                putString("IMAGE_URL", list[position].profilePicture)
                putString("NAMEOFUSER", list[position].name)
                putString("NUMBER", list[position].number)
                putString("ID", list[position].userId.toString())
            }
            navController.navigate(R.id.action_newMessagefragment_to_chatUserFragment, bundle)
        }
        holder.button.text = list[position].name
        Glide.with(holder.imageView.context)
            .asBitmap()
            .load(list[position].profilePicture)
            .into(holder.imageView)

    }

}
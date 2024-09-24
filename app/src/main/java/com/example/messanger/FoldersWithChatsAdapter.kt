package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoldersWithChatsAdapter(val listOfNames: List<String>): RecyclerView.Adapter<FoldersWithChatsAdapter.FoldersWithChatsViewHolder>() {
    class FoldersWithChatsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textView = view.findViewById<TextView>(R.id.tvFoldersName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersWithChatsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_folders_with_chats, parent, false)
        return FoldersWithChatsViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = listOfNames.size

    override fun onBindViewHolder(holder: FoldersWithChatsViewHolder, position: Int) {
        holder.textView.text = listOfNames[position]
    }
}
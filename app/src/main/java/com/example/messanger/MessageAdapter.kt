package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val currentUserId: String, private val messages: List<Message>, val textSize: Int) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, textSize: Int) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = message.text
            itemView.findViewById<TextView>(R.id.tvMessage).textSize = textSize.toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = when (viewType) {
            R.layout.rv_message -> LayoutInflater.from(parent.context).inflate(R.layout.rv_message, parent, false)
            R.layout.rv_reciever_message -> LayoutInflater.from(parent.context).inflate(R.layout.rv_reciever_message, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message, textSize)
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            R.layout.rv_message
        } else {
            R.layout.rv_reciever_message
        }
    }
}


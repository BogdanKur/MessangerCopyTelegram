package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageElectAdapter(private val messages: List<ElectMessages>, val textSize: Int): RecyclerView.Adapter<MessageElectAdapter.MessageElectViewHolder>() {
    class MessageElectViewHolder(view: View): RecyclerView.ViewHolder(view) {
           val textView = view.findViewById<TextView>(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageElectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.rv_message, parent, false)
        return MessageElectViewHolder(layoutInflater)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageElectViewHolder, position: Int) {
        holder.textView.text = messages[position].text
        holder.textView.textSize = textSize.toFloat()
    }
}
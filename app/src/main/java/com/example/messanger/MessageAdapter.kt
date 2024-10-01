package com.example.messanger

import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore.Video
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val currentUserId: String, private val messages: List<Message>, val textSize: Int) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    companion object {
        var mediaPlayer: MediaPlayer? = null
    }
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var isPlaying = false
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvMessage1: TextView = itemView.findViewById(R.id.tvMessage1)

        fun bind(message: Message, textSize: Int, onMessageClick: (String) -> Unit) {
            if(!message.text.contains("audio") && !message.text.contains("videos")) {
                tvMessage.apply {
                    tvMessage.text = message.text
                    tvMessage.textSize = textSize.toFloat()
                }
            } else if(message.text.contains("audio")) {
                if(!isPlaying) {
                    tvMessage.apply {
                        setOnClickListener {
                            visibility = View.GONE
                            tvMessage1.visibility = View.VISIBLE
                            isPlaying = true
                            onMessageClick(message.text)
                        }
                    }
                } else {
                    tvMessage1.setOnClickListener {
                        tvMessage.visibility = View.GONE
                        tvMessage1.visibility = View.VISIBLE
                        isPlaying = false
                        mediaPlayer?.pause()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = when (viewType) {
            R.layout.rv_message -> LayoutInflater.from(parent.context).inflate(R.layout.rv_message, parent, false)
            R.layout.rv_reciever_message -> LayoutInflater.from(parent.context).inflate(R.layout.rv_reciever_message, parent, false)
            R.layout.rv_message_audio -> LayoutInflater.from(parent.context).inflate(R.layout.rv_message_audio, parent, false)
            R.layout.rv_reciever_message_audio -> LayoutInflater.from(parent.context).inflate(R.layout.rv_reciever_message_audio, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return MessageViewHolder(view)
    }



    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message, textSize) { audioUrl ->
            playAudio(audioUrl)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId ) {
            if(messages[position].text.contains("audio")) {
                R.layout.rv_message_audio
            }else {
                R.layout.rv_message
            }
        } else {
            if(messages[position].text.contains("audio")) {
                R.layout.rv_reciever_message_audio
            }else {
                R.layout.rv_reciever_message
            }
        }
    }

    private fun playAudio(audioUrl: String) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(audioUrl)
        mediaPlayer.setOnPreparedListener {
            it.start()
        }
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Log.e("MediaPlayerError", "Error code: $what, extra code: $extra")
            true
        }
    }
}


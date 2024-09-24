package com.example.messanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView

class TabsFoldersAdapter(val tabsFoldersList: List<String>, val navController: NavController, val listener: TabsFoldersInterface): RecyclerView.Adapter<TabsFoldersAdapter.TabsFoldersViewHolder>() {
    class TabsFoldersViewHolder(val view: View): RecyclerView.ViewHolder(view){
        val nameTabsFolders = view.findViewById<Button>(R.id.tvNameTabsFolders)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabsFoldersViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.rv_tabs_folders, parent, false )
        return TabsFoldersViewHolder(layout)
    }

    override fun getItemCount(): Int = tabsFoldersList.size

    override fun onBindViewHolder(holder: TabsFoldersViewHolder, position: Int) {
        holder.nameTabsFolders.text = tabsFoldersList[position]
        holder.nameTabsFolders.setOnClickListener {
            listener.onButtonClick(tabsFoldersList[position])
        }
    }
}
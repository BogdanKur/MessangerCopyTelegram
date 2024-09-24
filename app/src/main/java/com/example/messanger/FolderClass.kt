package com.example.messanger

data class FolderClass(
    var name: String? = "",
    var listsUser: MutableList<MessageTypeClass> = mutableListOf()
)

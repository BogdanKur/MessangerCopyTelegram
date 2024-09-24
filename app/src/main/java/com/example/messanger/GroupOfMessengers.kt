package com.example.messanger

data class GroupOfMessengers(
    val name: String? = "",
    val profilePicture: String? = "",
    val users: List<UserOfMessanger>? = null
)

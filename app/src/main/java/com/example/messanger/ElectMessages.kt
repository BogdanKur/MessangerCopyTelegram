package com.example.messanger

import java.sql.Time

data class ElectMessages(
    val text: String = "",
    val time: Long = System.currentTimeMillis()
)
package com.example.chatapplication.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val date = Date(timestamp)
        return if( now - timestamp > oneDayMillis) {
            val sdf = SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

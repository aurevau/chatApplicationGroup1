package com.example.chatapplication.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

package com.example.chatapplication.data

data class User(
    val id: String = "",
    val username: String = "",
    val name: String = ""
) {
    val initials: String
        get() = name
            .trim()
            .split("\\s+" .toRegex())
            .take(2)
            .joinToString ("") {it.first().uppercase()}
}
package com.example.chatapplication.data

data class User(
    val id: String = "",
    val name: String = ""
) {
    val initials: String
        get() {
            if (name.isBlank()) return "?"
            return name.trim()
                .split("\\s+".toRegex())
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
        }
}
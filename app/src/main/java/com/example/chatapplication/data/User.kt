package com.example.chatapplication.data

data class User(
    val id: String? = "",
    var fullName: String = "",
    var fullNameLower: String = fullName.lowercase()
) {
    constructor(): this("", "", "")
    val initials: String
        get() {
            if (fullName.isBlank()) return "?"
            return fullName.trim()
                .split("\\s+".toRegex())
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
        }
}
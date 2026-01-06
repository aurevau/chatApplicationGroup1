package com.example.chatapplication.data

data class User(
    val id: String? = "",
    var fullName: String = "",
    val email: String = "", // Added email field
    val profileImageUrl: String? = null, // Added profileImageUrl field
    var fullNameLower: String = fullName.lowercase()
) {
    constructor(): this("", "", "", null, "")
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

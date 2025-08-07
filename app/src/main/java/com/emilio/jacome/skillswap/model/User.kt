package com.emilio.jacome.skillswap.model

data class User(
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var bio: String = "",
    var university: String = "",
    var rating: Double = 0.0,
    var profileImageUrl: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0.0, "", 0L, 0L)

    fun isValid(): Boolean {
        return uid.isNotEmpty() && 
               email.isNotEmpty() && 
               name.isNotEmpty() &&
               name.length >= 2
    }

    fun getDisplayName(): String {
        return if (name.isNotEmpty()) name else email.substringBefore("@")
    }
}

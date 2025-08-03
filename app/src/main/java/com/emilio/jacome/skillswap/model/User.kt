package com.emilio.jacome.skillswap.model

/**
 * User data model for Firestore
 */
data class User(
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var bio: String = "",
    var skills: List<String> = emptyList(),
    var rating: Double = 0.0,
    var profileImageUrl: String = "",
    var university: String = "", // Add university field
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", "", "", emptyList(), 0.0, "", "", 0L, 0L)
    
    /**
     * Validate user data
     */
    fun isValid(): Boolean {
        return uid.isNotEmpty() && 
               email.isNotEmpty() && 
               name.isNotEmpty() &&
               name.length >= 2
    }
    
    /**
     * Get display name (fallback to email if name is empty)
     */
    fun getDisplayName(): String {
        return if (name.isNotEmpty()) name else email.substringBefore("@")
    }
}

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
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", "", "", emptyList(), 0.0, "", 0L, 0L)
}

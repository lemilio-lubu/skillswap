package com.emilio.jacome.skillswap.model

/**
 * Skill data model for Firestore
 */
data class Skill(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var price: Double = 0.0,
    var userId: String = "",
    var userName: String = "",
    var userAvatar: String = "",
    var rating: Double = 0.0,
    var reviewCount: Int = 0,
    var active: Boolean = true,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", "", "", 0.0, "", "", "", 0.0, 0, true, 0L, 0L)
    
    /**
     * Get formatted price string
     */
    fun getFormattedPrice(): String {
        return "$${price.toInt()}/hora"
    }
    
    /**
     * Get formatted rating string
     */
    fun getFormattedRating(): String {
        return if (reviewCount > 0) {
            "${String.format("%.1f", rating)} ($reviewCount reseñas)"
        } else {
            "Sin reseñas"
        }
    }
    
    /**
     * Generate skill ID from title and user ID
     */
    fun generateId(): String {
        return "${userId}_${title.replace(" ", "_").lowercase()}"
    }
}

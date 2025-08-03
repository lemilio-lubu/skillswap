package com.emilio.jacome.skillswap.utils

import android.content.Context
import android.widget.Toast

/**
 * UIHelper - Utility functions for common UI operations
 */
object UIHelper {
    
    /**
     * Show a short toast message
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show a long toast message
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Generate user initials from full name
     */
    fun getUserInitials(name: String): String {
        if (name.isEmpty()) return "?"
        
        val parts = name.trim().split(" ")
        return when (parts.size) {
            1 -> parts[0].first().uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }
    
    /**
     * Format rating display text
     */
    fun formatRating(rating: Double, reviewCount: Int): String {
        return if (reviewCount > 0) {
            "${String.format("%.1f", rating)} ($reviewCount reseña${if (reviewCount != 1) "s" else ""})"
        } else {
            "Sin reseñas aún"
        }
    }
    
    /**
     * Format price display text
     */
    fun formatPrice(price: Double): String {
        return "$${price.toInt()}/hora"
    }
    
    /**
     * Get skill category emoji
     */
    fun getCategoryEmoji(category: String): String {
        return when (category.lowercase()) {
            "programación", "programming" -> "💻"
            "diseño", "design" -> "🎨"
            "matemáticas", "mathematics" -> "📊"
            "idiomas", "languages" -> "🗣️"
            "música", "music" -> "🎵"
            "deportes", "sports" -> "⚽"
            "cocina", "cooking" -> "👨‍🍳"
            "fotografía", "photography" -> "📸"
            else -> "📚"
        }
    }
    
    /**
     * Truncate text to specified length
     */
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            "${text.substring(0, maxLength - 3)}..."
        }
    }
    
    /**
     * Validate input fields
     */
    fun isValidInput(vararg inputs: String): Boolean {
        return inputs.all { it.isNotBlank() }
    }
    
    /**
     * Get time ago format
     */
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "Hace $days día${if (days != 1L) "s" else ""}"
            hours > 0 -> "Hace $hours hora${if (hours != 1L) "s" else ""}"
            minutes > 0 -> "Hace $minutes minuto${if (minutes != 1L) "s" else ""}"
            else -> "Hace un momento"
        }
    }
}

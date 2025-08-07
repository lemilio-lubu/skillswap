package com.emilio.jacome.skillswap.utils

import android.content.Context
import android.widget.Toast

object UIHelper {

    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun getUserInitials(name: String): String {
        if (name.isEmpty()) return "?"
        
        val parts = name.trim().split(" ")
        return when (parts.size) {
            1 -> parts[0].first().uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }

    fun formatRating(rating: Double, reviewCount: Int): String {
        return if (reviewCount > 0) {
            "${String.format("%.1f", rating)} ($reviewCount reseña${if (reviewCount != 1) "s" else ""})"
        } else {
            "Sin reseñas aún"
        }
    }
}

package com.emilio.jacome.skillswap.model

data class Skill(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var price: Double = 0.0,
    var modalidad: String = "", // Nuevo campo para modalidad
    var userId: String = "",
    var userName: String = "",
    var userAvatar: String = "",
    var rating: Double = 0.0,
    var reviewCount: Int = 0,
    var sesionesCompletadas: Int = 0, // Nuevo campo para sesiones completadas
    var active: Boolean = true,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", 0.0, "", "", "", "", 0.0, 0, 0, true, 0L, 0L)

    fun getFormattedPrice(): String {
        return "$${price.toInt()}/hora"
    }

    fun getFormattedRating(): String {
        return if (reviewCount > 0) {
            "${String.format("%.1f", rating)} ($reviewCount reseñas)"
        } else {
            "Sin reseñas"
        }
    }

    fun generateId(): String {
        return "${userId}_${title.replace(" ", "_").lowercase()}"
    }
}

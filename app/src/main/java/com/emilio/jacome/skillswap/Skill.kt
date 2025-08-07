package com.emilio.jacome.skillswap.model

data class Skill(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val modalidad: String = "",
    val userId: String = "",
    val userName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    constructor() : this("", "", "", "", 0.0, "", "", "", 0L, 0L, true)

    // Función auxiliar para obtener las iniciales del instructor
    fun getInstructorInitials(): String {
        return userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

    // Función para formatear el precio
    fun getFormattedPrice(): String {
        return if (price == price.toInt().toDouble()) {
            "$${price.toInt()}/hora"
        } else {
            "$${String.format("%.2f", price)}/hora"
        }
    }

    // Función para validar si la habilidad es válida
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                description.isNotBlank() &&
                category.isNotBlank() &&
                price > 0 &&
                modalidad.isNotBlank() &&
                userId.isNotBlank() &&
                userName.isNotBlank()
    }
}
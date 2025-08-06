package com.emilio.jacome.skillswap.utils

object Constants {
    /**
     * Categorías disponibles para las habilidades
     */
    object Categories {
        val LIST = listOf(
            "Matemáticas",
            "Programación",
            "Idiomas",
            "Diseño",
            "Música",
            "Deportes",
            "Otros"
        )
    }

    /**
     * Modalidades disponibles para las habilidades
     */
    object Modalities {
        const val PRESENCIAL = "Presencial"
        const val VIRTUAL = "Virtual"

        val LIST = listOf(PRESENCIAL, VIRTUAL)

        // Lista con opción por defecto para los spinners
        val LIST_WITH_DEFAULT = listOf("Selecciona una modalidad", PRESENCIAL, VIRTUAL)
    }
}

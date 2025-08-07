package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.emilio.jacome.skillswap.model.Skill
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions

object SkillRepository {
    
    private const val SKILLS_COLLECTION = "skills"
    private const val RATINGS_COLLECTION = "ratings"

    fun createSkill(skill: Skill): Task<Void> {
        val skillId = if (skill.id.isEmpty()) skill.generateId() else skill.id
        skill.id = skillId
        
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .set(skill)
    }

    fun getSkill(skillId: String): Task<DocumentSnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .get()
    }

    fun getUserSkills(userId: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("active", true)
            .get()
    }

    fun getAllActiveSkills(): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("active", true)
            .get()
    }

    fun updateSkill(skillId: String, updates: Map<String, Any>): Task<Void> {
        val updatedData = updates.toMutableMap()
        updatedData["updatedAt"] = System.currentTimeMillis()
        
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .update(updatedData)
    }

    fun deleteSkill(skillId: String): Task<Void> {
        return updateSkill(skillId, mapOf("active" to false))
    }

    fun rateSkill(skillId: String, userId: String, rating: Float): Task<Void> {
        val db = FirebaseManager.firestore
        val ratingRef = db.collection(SKILLS_COLLECTION)
                          .document(skillId)
                          .collection(RATINGS_COLLECTION)
                          .document(userId)

        val ratingData = hashMapOf(
            "userId" to userId,
            "rating" to rating,
            "timestamp" to System.currentTimeMillis()
        )

        return ratingRef.set(ratingData, SetOptions.merge())
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    // Si la calificación se guarda correctamente, actualiza el promedio en la habilidad
                    updateSkillRatingAverage(skillId)
                } else {
                    throw task.exception ?: Exception("Error al guardar calificación")
                }
            }
    }

    fun getUserRating(skillId: String, userId: String): Task<DocumentSnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .collection(RATINGS_COLLECTION)
            .document(userId)
            .get()
    }

    fun getSkillRatings(skillId: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .collection(RATINGS_COLLECTION)
            .get()
    }

    private fun updateSkillRatingAverage(skillId: String): Task<Void> {
        return getSkillRatings(skillId)
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    val ratings = task.result?.documents ?: emptyList()
                    var totalRating = 0f

                    // Calcular el promedio de calificaciones
                    ratings.forEach { doc ->
                        val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                        totalRating += rating
                    }

                    val averageRating = if (ratings.isNotEmpty()) {
                        totalRating / ratings.size
                    } else 0.0

                    // Actualizar la habilidad con el nuevo promedio y contador
                    val updates = mapOf(
                        "rating" to averageRating,
                        "reviewCount" to ratings.size,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    updateSkill(skillId, updates)
                } else {
                    throw task.exception ?: Exception("Error al obtener calificaciones")
                }
            }
    }

    fun getTutorRatingAverage(tutorId: String): Task<Double> {
        return getUserSkills(tutorId)
            .continueWith { task ->
                if (task.isSuccessful) {
                    val skills = task.result?.documents ?: emptyList()
                    var totalRating = 0.0
                    var ratedSkillsCount = 0

                    skills.forEach { doc ->
                        val skill = doc.toObject(Skill::class.java)
                        if (skill != null && skill.reviewCount > 0) {
                            totalRating += skill.rating
                            ratedSkillsCount++
                        }
                    }

                    if (ratedSkillsCount > 0) {
                        totalRating / ratedSkillsCount
                    } else 0.0
                } else {
                    throw task.exception ?: Exception("Error al obtener habilidades del tutor")
                }
            }
    }
}

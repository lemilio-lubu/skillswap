package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.emilio.jacome.skillswap.model.Skill
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions

/**
 * SkillRepository - Handles skill data operations with Firestore
 */
object SkillRepository {
    
    private const val SKILLS_COLLECTION = "skills"
    private const val RATINGS_COLLECTION = "ratings"

    /**
     * Create a new skill document in Firestore
     */
    fun createSkill(skill: Skill): Task<Void> {
        val skillId = if (skill.id.isEmpty()) skill.generateId() else skill.id
        skill.id = skillId
        
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .set(skill)
    }
    
    /**
     * Get skill document by ID
     */
    fun getSkill(skillId: String): Task<DocumentSnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .get()
    }
    
    /**
     * Get all skills for a specific user
     */
    fun getUserSkills(userId: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("active", true)
            .get()
    }
    
    /**
     * Get all active skills (for browsing) - filtering on client side
     */
    fun getAllActiveSkills(): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("active", true)
            .get()
    }
    
    /**
     * Search skills by category - filtering on client side
     */
    fun getSkillsByCategory(category: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("category", category)
            .whereEqualTo("active", true)
            .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
    }
    
    /**
     * Update skill document
     */
    fun updateSkill(skillId: String, updates: Map<String, Any>): Task<Void> {
        val updatedData = updates.toMutableMap()
        updatedData["updatedAt"] = System.currentTimeMillis()
        
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .update(updatedData)
    }
    
    /**
     * Delete skill (set as inactive)
     */
    fun deleteSkill(skillId: String): Task<Void> {
        return updateSkill(skillId, mapOf("active" to false))
    }
    
    /**
     * Search skills by title or description - filtering on client side
     */
    fun searchSkills(query: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("active", true)
            .orderBy("title")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
    }

    /**
     * Get all active skills excluding those from a specific user
     */
    fun getActiveSkillsExcludingUser(excludeUserId: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("active", true)
            .whereNotEqualTo("userId", excludeUserId)
            .get()
    }

    /**
     * Rate a skill by a user
     */
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

    /**
     * Check if a user has already rated a skill
     */
    fun getUserRating(skillId: String, userId: String): Task<DocumentSnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .collection(RATINGS_COLLECTION)
            .document(userId)
            .get()
    }

    /**
     * Get all ratings for a skill
     */
    fun getSkillRatings(skillId: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .document(skillId)
            .collection(RATINGS_COLLECTION)
            .get()
    }

    /**
     * Update skill rating average based on all ratings
     */
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

    /**
     * Get average rating for all skills of a tutor
     */
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

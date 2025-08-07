package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.emilio.jacome.skillswap.model.Skill
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * SkillRepository - Handles skill data operations with Firestore
 */
object SkillRepository {
    
    private const val SKILLS_COLLECTION = "skills"
    
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
}

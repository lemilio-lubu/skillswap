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
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
    }
    
    /**
     * Get all active skills (for browsing)
     */
    fun getAllActiveSkills(): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
    }
    
    /**
     * Search skills by category
     */
    fun getSkillsByCategory(category: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("category", category)
            .whereEqualTo("isActive", true)
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
        return updateSkill(skillId, mapOf("isActive" to false))
    }
    
    /**
     * Search skills by title or description
     */
    fun searchSkills(query: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(SKILLS_COLLECTION)
            .whereEqualTo("isActive", true)
            .orderBy("title")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
    }
}

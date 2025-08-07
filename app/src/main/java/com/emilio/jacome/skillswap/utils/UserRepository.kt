package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.emilio.jacome.skillswap.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * UserRepository - Handles user data operations with Firestore
 */
object UserRepository {
    
    private const val USERS_COLLECTION = "users"
    
    /**
     * Create a new user document in Firestore
     */
    fun createUser(user: User): Task<Void> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user)
    }
    
    /**
     * Get user document by UID
     */
    fun getUser(uid: String): Task<DocumentSnapshot> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .get()
    }
    
    /**
     * Update user document
     */
    fun updateUser(uid: String, updates: Map<String, Any>): Task<Void> {
        val updatedData = updates.toMutableMap()
        updatedData["updatedAt"] = System.currentTimeMillis()
        
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .update(updatedData)
    }
    
    /**
     * Check if user document exists
     */
    fun userExists(uid: String, callback: (Boolean) -> Unit) {
        getUser(uid)
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }
    
    /**
     * Get current user data
     */
    fun getCurrentUser(callback: (User?) -> Unit) {
        val currentUserId = FirebaseManager.getCurrentUserId()
        if (currentUserId == null) {
            callback(null)
            return
        }
        
        getUser(currentUserId)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
    
    /**
     * Get all users (for search functionality) - filtering on client side
     */
    fun getAllUsers(): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .get()
    }
    
    /**
     * Search users by name - filtering on client side
     */
    fun searchUsersByName(query: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
    }
    
    /**
     * Search users by university
     */
    fun searchUsersByUniversity(university: String): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .whereEqualTo("university", university)
            .get()
    }
}

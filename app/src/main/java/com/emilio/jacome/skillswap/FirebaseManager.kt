package com.emilio.jacome.skillswap

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Firebase Manager - Centralized Firebase configuration and access
 */
object FirebaseManager {
    
    // Firebase Authentication instance
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    
    // Firestore database instance
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    /**
     * Initialize Firebase (call this in Application class or MainActivity)
     */
    fun initialize(context: android.content.Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    
    /**
     * Get current user display name
     */
    fun getCurrentUserDisplayName(): String? {
        return auth.currentUser?.displayName
    }
    
    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }
    
    /**
     * Send email verification to current user
     */
    fun sendEmailVerification(callback: (Boolean, String?) -> Unit) {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Add authentication state listener
     */
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }
    
    /**
     * Remove authentication state listener
     */
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }
}

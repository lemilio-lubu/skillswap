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
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }
}

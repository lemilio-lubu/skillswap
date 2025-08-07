package com.emilio.jacome.skillswap

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun initialize(context: android.content.Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserDisplayName(): String? {
        return auth.currentUser?.displayName
    }

    fun signOut() {
        auth.signOut()
    }
}

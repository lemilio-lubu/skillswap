package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.emilio.jacome.skillswap.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object UserRepository {
    
    private const val USERS_COLLECTION = "users"

    fun getUser(uid: String): Task<DocumentSnapshot> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .get()
    }

    fun getAllUsers(): Task<QuerySnapshot> {
        return FirebaseManager.firestore
            .collection(USERS_COLLECTION)
            .get()
    }
}

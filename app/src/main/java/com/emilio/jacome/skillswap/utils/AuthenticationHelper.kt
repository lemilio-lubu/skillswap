package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

object AuthenticationHelper {

    fun login(email: String, password: String): Task<AuthResult> {
        return FirebaseManager.auth.signInWithEmailAndPassword(email, password)
    }

    fun resetPassword(email: String): Task<Void> {
        return FirebaseManager.auth.sendPasswordResetEmail(email)
    }

    fun logout() {
        FirebaseManager.signOut()
    }

    fun getAuthErrorMessage(exception: Exception?): String {
        return when {
            exception?.message?.contains("user-not-found") == true -> 
                "No existe una cuenta con este email"
            exception?.message?.contains("wrong-password") == true -> 
                "Contraseña incorrecta"
            exception?.message?.contains("invalid-email") == true -> 
                "Email inválido"
            exception?.message?.contains("weak-password") == true -> 
                "La contraseña es muy débil"
            exception?.message?.contains("email-already-in-use") == true -> 
                "Este email ya está registrado"
            exception?.message?.contains("user-disabled") == true -> 
                "Esta cuenta ha sido deshabilitada"
            exception?.message?.contains("too-many-requests") == true -> 
                "Demasiados intentos. Intenta más tarde"
            exception?.message?.contains("network") == true -> 
                "Error de conexión"
            else -> exception?.message ?: "Error desconocido"
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

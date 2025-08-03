package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.FirebaseManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

/**
 * AuthenticationHelper - Utility functions for Firebase Authentication
 */
object AuthenticationHelper {
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String): Task<AuthResult> {
        return FirebaseManager.auth.signInWithEmailAndPassword(email, password)
    }
    
    /**
     * Register with email and password
     */
    fun register(email: String, password: String): Task<AuthResult> {
        return FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
    }
    
    /**
     * Send password reset email
     */
    fun resetPassword(email: String): Task<Void> {
        return FirebaseManager.auth.sendPasswordResetEmail(email)
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        FirebaseManager.signOut()
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return FirebaseManager.isUserLoggedIn()
    }
    
    /**
     * Get error message from Firebase Auth exception
     */
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
    
    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate password strength
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Get password strength message
     */
    fun getPasswordStrengthMessage(password: String): String {
        return when {
            password.length < 6 -> "Muy débil - Mínimo 6 caracteres"
            password.length < 8 -> "Débil - Recomendado 8+ caracteres"
            !password.any { it.isDigit() } -> "Buena - Agrega números para mayor seguridad"
            !password.any { it.isUpperCase() } -> "Buena - Agrega mayúsculas para mayor seguridad"
            else -> "Fuerte"
        }
    }
}

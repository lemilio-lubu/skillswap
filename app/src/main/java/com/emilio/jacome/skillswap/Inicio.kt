package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.utils.AuthenticationHelper

class Inicio : AppCompatActivity() {
    
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_inicio)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            loginUser()
        }
        
        tvRegister.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
        
        tvForgotPassword.setOnClickListener {
            resetPassword()
        }
    }
    
    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        // Validate input
        if (!validateInput(email, password)) {
            return
        }
        
        // Disable button to prevent multiple clicks
        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."
        
        // Login with Firebase Auth
        AuthenticationHelper.login(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val user = task.result?.user
                    if (user != null) {
                        showSuccess("¡Bienvenido!")
                        navigateToMainApp()
                    }
                } else {
                    // Login failed
                    val errorMessage = AuthenticationHelper.getAuthErrorMessage(task.exception)
                    showError(errorMessage)
                    resetButton()
                }
            }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showError("Por favor ingresa tu email")
                etEmail.requestFocus()
                false
            }
            !AuthenticationHelper.isValidEmail(email) -> {
                showError("Por favor ingresa un email válido")
                etEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                showError("Por favor ingresa tu contraseña")
                etPassword.requestFocus()
                false
            }
            else -> true
        }
    }
    
    private fun resetPassword() {
        val email = etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            showError("Por favor ingresa tu email para recuperar la contraseña")
            etEmail.requestFocus()
            return
        }
        
        if (!AuthenticationHelper.isValidEmail(email)) {
            showError("Por favor ingresa un email válido")
            etEmail.requestFocus()
            return
        }
        
        AuthenticationHelper.resetPassword(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSuccess("Se ha enviado un email para restablecer tu contraseña")
                } else {
                    val errorMessage = AuthenticationHelper.getAuthErrorMessage(task.exception)
                    showError("Error al enviar email: $errorMessage")
                }
            }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun resetButton() {
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar sesión"
    }
    
    private fun navigateToMainApp() {
        startActivity(Intent(this, Busqueda::class.java))
        finish()
    }
}
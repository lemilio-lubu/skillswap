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
        
        if (!validateInput(email, password)) {
            return
        }
        
        btnLogin.isEnabled = false
        btnLogin.text = getString(R.string.btn_login_progress)

        AuthenticationHelper.login(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        showSuccess(getString(R.string.welcome_message))
                        navigateToMainApp()
                    }
                } else {
                    val errorMessage = AuthenticationHelper.getAuthErrorMessage(task.exception)
                    showError(errorMessage)
                    resetButton()
                }
            }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showError(getString(R.string.error_empty_email))
                etEmail.requestFocus()
                false
            }
            !AuthenticationHelper.isValidEmail(email) -> {
                showError(getString(R.string.error_invalid_email))
                etEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                showError(getString(R.string.error_empty_password))
                etPassword.requestFocus()
                false
            }
            else -> true
        }
    }
    
    private fun resetPassword() {
        val email = etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            showError(getString(R.string.error_empty_email_reset))
            etEmail.requestFocus()
            return
        }
        
        if (!AuthenticationHelper.isValidEmail(email)) {
            showError(getString(R.string.error_invalid_email))
            etEmail.requestFocus()
            return
        }
        
        AuthenticationHelper.resetPassword(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSuccess(getString(R.string.reset_password_email_sent))
                } else {
                    val errorMessage = AuthenticationHelper.getAuthErrorMessage(task.exception)
                    showError(getString(R.string.error_reset_password, errorMessage))
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
        btnLogin.text = getString(R.string.btn_login)
    }
    
    private fun navigateToMainApp() {
        startActivity(Intent(this, Busqueda::class.java))
        finish()
    }
}
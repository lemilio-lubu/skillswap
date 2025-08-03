package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.model.User

class Registro : AppCompatActivity() {
    
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUniversidad: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCrearCuenta: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_registro)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        
        initializeViews()
        setupClickListeners()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private fun initializeViews() {
        btnCrearCuenta = findViewById(R.id.btn_crear_cuenta)
        etNombre = findViewById(R.id.et_nombre_completo)
        etEmail = findViewById(R.id.et_email)
        etUniversidad = findViewById(R.id.et_universidad)
        etPassword = findViewById(R.id.et_contrasena)
        etConfirmPassword = findViewById(R.id.et_confirmar_contrasena)
    }
    
    private fun setupClickListeners() {
        btnCrearCuenta.setOnClickListener {
            registerUser()
        }
    }
    
    private fun registerUser() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val universidad = etUniversidad.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        
        // Validate input
        if (!validateInput(nombre, email, universidad, password, confirmPassword)) {
            return
        }
        
        // Disable button to prevent multiple clicks
        btnCrearCuenta.isEnabled = false
        btnCrearCuenta.text = "Creando cuenta..."
        
        // Create user with Firebase Auth
        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, create user document
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        createUserDocument(firebaseUser.uid, nombre, email, universidad)
                    }
                } else {
                    // Registration failed
                    handleRegistrationError(task.exception)
                    resetButton()
                }
            }
    }
    
    private fun validateInput(
        nombre: String,
        email: String, 
        universidad: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            nombre.isEmpty() -> {
                showError("Por favor ingresa tu nombre completo")
                etNombre.requestFocus()
                false
            }
            nombre.length < 2 -> {
                showError("El nombre debe tener al menos 2 caracteres")
                etNombre.requestFocus()
                false
            }
            email.isEmpty() -> {
                showError("Por favor ingresa tu email")
                etEmail.requestFocus()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Por favor ingresa un email válido")
                etEmail.requestFocus()
                false
            }
            universidad.isEmpty() -> {
                showError("Por favor ingresa tu universidad/institución")
                etUniversidad.requestFocus()
                false
            }
            password.isEmpty() -> {
                showError("Por favor ingresa una contraseña")
                etPassword.requestFocus()
                false
            }
            password.length < 6 -> {
                showError("La contraseña debe tener al menos 6 caracteres")
                etPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                showError("Por favor confirma tu contraseña")
                etConfirmPassword.requestFocus()
                false
            }
            password != confirmPassword -> {
                showError("Las contraseñas no coinciden")
                etConfirmPassword.requestFocus()
                false
            }
            else -> true
        }
    }
    
    private fun createUserDocument(uid: String, nombre: String, email: String, universidad: String) {
        val user = User(
            uid = uid,
            email = email,
            name = nombre,
            bio = "Estudiante en $universidad",
            university = universidad, // Add university field
            skills = emptyList(),
            rating = 0.0,
            profileImageUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        FirebaseManager.firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                // User document created successfully
                showSuccess("¡Cuenta creada exitosamente!")
                navigateToMainApp()
            }
            .addOnFailureListener { exception ->
                showError("Error al crear perfil: ${exception.message}")
                resetButton()
            }
    }
    
    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("email address is already in use") == true -> 
                "Este email ya está registrado. Intenta iniciar sesión."
            exception?.message?.contains("weak-password") == true -> 
                "La contraseña es muy débil. Usa al menos 6 caracteres."
            exception?.message?.contains("invalid-email") == true -> 
                "El formato del email no es válido."
            exception?.message?.contains("network") == true -> 
                "Error de conexión. Verifica tu internet."
            else -> "Error al crear la cuenta: ${exception?.message ?: "Error desconocido"}"
        }
        showError(errorMessage)
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun resetButton() {
        btnCrearCuenta.isEnabled = true
        btnCrearCuenta.text = "Crear cuenta"
    }
    
    private fun navigateToMainApp() {
        startActivity(Intent(this, Busqueda::class.java))
        finish()
    }
}
package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.model.Skill
import com.emilio.jacome.skillswap.model.User
import com.emilio.jacome.skillswap.utils.AuthenticationHelper
import com.emilio.jacome.skillswap.utils.SkillRepository
import com.emilio.jacome.skillswap.utils.UIHelper
import com.emilio.jacome.skillswap.utils.UserRepository

class Perfil : AppCompatActivity() {
    
    private lateinit var avatar: TextView
    private lateinit var nombreUsuario: TextView
    private lateinit var universidad: TextView
    private lateinit var calificacion: TextView
    private lateinit var skillsContainer: ViewGroup
    private lateinit var btnAgregarHabilidad: Button
    
    private var currentUser: User? = null
    private var userSkills: List<Skill> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_perfil)
        
        initializeViews()
        setupClickListeners()
        loadUserData()
    }
    
    private fun initializeViews() {
        val btnBack = findViewById<TextView>(R.id.btn_back)
        val btnConfiguracion = findViewById<TextView>(R.id.btn_configuracion)
        btnAgregarHabilidad = findViewById<Button>(R.id.btn_agregar_habilidad)
        
        avatar = findViewById(R.id.avatar)
        nombreUsuario = findViewById(R.id.nombre_usuario)
        universidad = findViewById(R.id.universidad)
        calificacion = findViewById(R.id.calificacion)
        
        // For dynamic skills, we'll use the main container for now
        skillsContainer = findViewById(R.id.main)
        
        // Hide content initially to prevent data flicker
        hideProfileContent()
    }
    
    private fun setupClickListeners() {
        val btnBack = findViewById<TextView>(R.id.btn_back)
        val btnLogout = findViewById<TextView>(R.id.btn_configuracion) // Changed from config to logout
        
        btnBack.setOnClickListener {
            finish()
        }
        
        btnLogout.setOnClickListener {
            // Direct logout prompt - no config menu
            showLogoutConfirmation()
        }
        
        btnAgregarHabilidad.setOnClickListener {
            startActivity(Intent(this, CrearHabilidad::class.java))
        }
    }
    
    private fun loadUserData() {
        val userId = FirebaseManager.getCurrentUserId()
        
        if (userId == null) {
            showError("Error: Usuario no autenticado")
            return
        }
        
        // Show loading state
        showLoadingState(true)
        
        // Load user profile data
        UserRepository.getUser(userId)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    currentUser?.let { user ->
                        updateUserUI(user)
                        loadUserSkills(userId)
                        showProfileContent() // Show content when data loads
                    }
                } else {
                    showError("Perfil de usuario no encontrado")
                    showLoadingState(false)
                    showProfileContent() // Show content even on error
                }
            }
            .addOnFailureListener { exception ->
                showError("Error al cargar perfil: ${exception.message}")
                showLoadingState(false)
                showProfileContent() // Show content even on error
            }
    }
    
    private fun loadUserSkills(userId: String) {
        SkillRepository.getUserSkills(userId)
            .addOnSuccessListener { querySnapshot ->
                userSkills = querySnapshot.toObjects(Skill::class.java)
                    .sortedByDescending { it.createdAt }
                updateSkillsUI(userSkills)
                showLoadingState(false)
            }
            .addOnFailureListener { exception ->
                showError("Error al cargar habilidades: ${exception.message}")
                showLoadingState(false)
            }
    }
    
    private fun updateUserUI(user: User) {
        // Update avatar with user initials
        val initials = UIHelper.getUserInitials(user.name)
        avatar.text = initials
        
        // Update user name
        nombreUsuario.text = user.getDisplayName()
        
        // Update university
        universidad.text = if (user.university.isNotEmpty()) user.university else "Universidad no especificada"
        
        // Update rating (will be calculated from actual skills and reviews later)
        val totalReviews = userSkills.sumOf { it.reviewCount }
        val averageRating = if (totalReviews > 0) {
            userSkills.sumOf { it.rating * it.reviewCount } / totalReviews
        } else {
            0.0
        }
        val ratingText = UIHelper.formatRating(averageRating, totalReviews)
        calificacion.text = ratingText
    }
    
    private fun updateSkillsUI(skills: List<Skill>) {
        val cardJavascript = findViewById<LinearLayout>(R.id.card_javascript)
        val cardDiseno = findViewById<LinearLayout>(R.id.card_diseno)
        
        // Hide cards initially
        cardJavascript?.visibility = View.GONE
        cardDiseno?.visibility = View.GONE
        
        // If no skills, show the "add first skill" state
        if (skills.isEmpty()) {
            btnAgregarHabilidad.text = "Agregar mi primera habilidad"
            return
        }
        
        // Show cards based on user's actual skills
        skills.forEachIndexed { index, skill ->
            when (index) {
                0 -> {
                    cardJavascript?.let { card ->
                        updateSkillCard(card, skill)
                        card.visibility = View.VISIBLE
                        card.setOnClickListener {
                            editSkill(skill)
                        }
                    }
                }
                1 -> {
                    cardDiseno?.let { card ->
                        updateSkillCard(card, skill)
                        card.visibility = View.VISIBLE
                        card.setOnClickListener {
                            editSkill(skill)
                        }
                    }
                }
            }
        }
        
        // Update button text based on skills count
        val buttonText = if (skills.isEmpty()) {
            "Agregar mi primera habilidad"
        } else {
            "Agregar nueva habilidad"
        }
        btnAgregarHabilidad.text = buttonText
    }
    
    private fun updateSkillCard(card: LinearLayout, skill: Skill) {
        // Find TextViews within the card and update them by ID
        val titleView = card.findViewById<TextView>(R.id.titulo_javascript) ?: card.findViewById<TextView>(R.id.titulo_diseno)
        val descriptionView = card.findViewById<TextView>(R.id.descripcion_javascript) ?: card.findViewById<TextView>(R.id.descripcion_diseno)
        val priceView = card.findViewById<TextView>(R.id.precio_javascript) ?: card.findViewById<TextView>(R.id.precio_diseno)
        
        // Update the views with skill data
        titleView?.text = skill.title
        descriptionView?.text = skill.description
        priceView?.text = skill.getFormattedPrice()
        
        // Update students count if available
        val studentsView = card.findViewById<TextView>(R.id.estudiantes_javascript) ?: card.findViewById<TextView>(R.id.estudiantes_diseno)
        studentsView?.text = "0 estudiantes" // Default for now
    }
    
    private fun editSkill(skill: Skill) {
        val intent = Intent(this, EditarHabilidad::class.java)
        intent.putExtra("skill_id", skill.id)
        intent.putExtra("skill_title", skill.title)
        intent.putExtra("skill_description", skill.description)
        intent.putExtra("skill_category", skill.category)
        intent.putExtra("skill_price", skill.price.toString())
        startActivity(intent)
    }
    
    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            hideProfileContent()
            // Show loading indicators
            nombreUsuario.text = "Cargando..."
            nombreUsuario.visibility = View.VISIBLE
            universidad.text = "Cargando..."
            universidad.visibility = View.VISIBLE
            calificacion.text = "Cargando..."
            calificacion.visibility = View.VISIBLE
        }
    }
    
    private fun hideProfileContent() {
        // Hide profile elements to prevent flicker
        avatar.visibility = View.INVISIBLE
        nombreUsuario.visibility = View.INVISIBLE
        universidad.visibility = View.INVISIBLE
        calificacion.visibility = View.INVISIBLE
        
        // Hide skill cards
        findViewById<LinearLayout>(R.id.card_javascript)?.visibility = View.GONE
        findViewById<LinearLayout>(R.id.card_diseno)?.visibility = View.GONE
        
        btnAgregarHabilidad.visibility = View.INVISIBLE
    }
    
    private fun showProfileContent() {
        // Show all profile elements
        avatar.visibility = View.VISIBLE
        nombreUsuario.visibility = View.VISIBLE
        universidad.visibility = View.VISIBLE
        calificacion.visibility = View.VISIBLE
        btnAgregarHabilidad.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        UIHelper.showLongToast(this, message)
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun logout() {
        AuthenticationHelper.logout()
        
        // Navigate to login screen and clear the back stack
        val intent = Intent(this, Inicio::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload data when returning from other activities
        loadUserData()
    }
}
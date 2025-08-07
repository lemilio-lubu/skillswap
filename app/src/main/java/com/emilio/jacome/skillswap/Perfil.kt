package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.model.Skill
import com.emilio.jacome.skillswap.model.User
import com.emilio.jacome.skillswap.utils.AuthenticationHelper
import com.emilio.jacome.skillswap.utils.SkillRepository
import com.emilio.jacome.skillswap.utils.UIHelper
import com.emilio.jacome.skillswap.utils.UserRepository
import com.emilio.jacome.skillswap.utils.UserProfileCache

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
        btnAgregarHabilidad = findViewById<Button>(R.id.btn_agregar_habilidad)
        avatar = findViewById(R.id.avatar)
        nombreUsuario = findViewById(R.id.nombre_usuario)
        universidad = findViewById(R.id.universidad)
        calificacion = findViewById(R.id.calificacion)
        
        skillsContainer = findViewById(R.id.main)
        
        hideProfileContent()
    }
    
    private fun setupClickListeners() {
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnLogout = findViewById<ImageView>(R.id.btn_configuracion)
        
        btnBack.setOnClickListener {
            finish()
        }
        
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        
        btnAgregarHabilidad.setOnClickListener {
            startActivity(Intent(this, CrearHabilidad::class.java))
        }
    }
    
    private fun loadUserData() {
        val userId = FirebaseManager.getCurrentUserId()
        
        if (userId == null) {
            showError(getString(R.string.user_not_authenticated))
            return
        }
        
        // Intentar usar datos del caché primero
        val cachedUser = UserProfileCache.getCachedUser()
        val cachedSkills = UserProfileCache.getCachedSkills()
        
        if (cachedUser != null && cachedSkills != null) {
            // Usar datos del caché
            currentUser = cachedUser
            userSkills = cachedSkills
            updateBasicUserInfo(cachedUser)
            updateUserRating()
            updateSkillsUI(cachedSkills)
            showProfileContent()
            return
        }
        
        showLoadingState(true)
        
        // Solo cargar datos del usuario si no están en caché
        if (cachedUser == null) {
            loadUserFromFirebase(userId)
        } else {
            // Si tenemos usuario cacheado pero no skills, usar el usuario cacheado
            currentUser = cachedUser
            updateBasicUserInfo(cachedUser)
            loadUserSkills(userId)
        }
    }
    
    private fun loadUserFromFirebase(userId: String) {
        UserRepository.getUser(userId)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    currentUser?.let { user ->
                        UserProfileCache.setCachedUser(user)
                        updateBasicUserInfo(user)
                        loadUserSkills(userId)
                    }
                } else {
                    showError(getString(R.string.profile_not_found))
                    showLoadingState(false)
                    showProfileContent()
                }
            }
            .addOnFailureListener { exception ->
                showError(getString(R.string.error_loading_profile, exception.message))
                showLoadingState(false)
                showProfileContent()
            }
    }
    
    private fun loadUserSkills(userId: String) {
        // Verificar si tenemos skills cacheadas válidas
        val cachedSkills = UserProfileCache.getCachedSkills()
        if (cachedSkills != null) {
            userSkills = cachedSkills
            updateUserRating()
            updateSkillsUI(cachedSkills)
            showLoadingState(false)
            showProfileContent()
            return
        }
        
        // Si no hay caché, cargar desde Firebase
        SkillRepository.getUserSkills(userId)
            .addOnSuccessListener { querySnapshot ->
                userSkills = querySnapshot.toObjects(Skill::class.java)
                    .sortedByDescending { it.createdAt }

                // Guardar en caché
                UserProfileCache.setCachedSkills(userSkills)
                
                updateUserRating()
                updateSkillsUI(userSkills)
                showLoadingState(false)
                showProfileContent()
            }
            .addOnFailureListener { exception ->
                showError(getString(R.string.error_loading_skills_profile, exception.message))
                showLoadingState(false)
                showProfileContent()
            }
    }
    
    private fun updateBasicUserInfo(user: User) {
        val initials = UIHelper.getUserInitials(user.name)
        avatar.text = initials
        
        nombreUsuario.text = user.getDisplayName()
        
        universidad.text = if (user.university.isNotEmpty()) user.university else getString(R.string.university_not_specified)

        calificacion.text = getString(R.string.loading_text)
    }

    private fun updateUserRating() {
        if (currentUser == null || userSkills.isEmpty()) {
            calificacion.text = getString(R.string.rating_sin_reviews).substring(2)
        }

        val userId = currentUser?.uid ?: FirebaseManager.getCurrentUserId() ?: return

        var totalRating = 0.0
        var ratedSkillsCount = 0

        userSkills.forEach { skill ->
            if (skill.reviewCount > 0) {
                totalRating += skill.rating
                ratedSkillsCount++
            }
        }

        if (ratedSkillsCount > 0) {
            val averageRating = totalRating / ratedSkillsCount
            val totalReviews = userSkills.sumOf { it.reviewCount }
            calificacion.text = UIHelper.formatRating(averageRating, totalReviews).substring(2)
        } else {
            calificacion.text = getString(R.string.rating_sin_reviews).substring(2)
        }

        SkillRepository.getTutorRatingAverage(userId)
            .addOnSuccessListener { averageRating ->
                if (averageRating > 0) {
                    val totalReviews = userSkills.sumOf { it.reviewCount }
                    calificacion.text = String.format("%.1f (%d reseñas)", averageRating, totalReviews)
                } else {
                    calificacion.text = "Sin reseñas"
                }
            }
            .addOnFailureListener {

            }
    }
    
    private fun updateSkillsUI(skills: List<Skill>) {
        val cardJavascript = findViewById<LinearLayout>(R.id.card_javascript)
        val cardDiseno = findViewById<LinearLayout>(R.id.card_diseno)
        
        cardJavascript?.visibility = View.GONE
        cardDiseno?.visibility = View.GONE
        
        if (skills.isEmpty()) {
            btnAgregarHabilidad.text = getString(R.string.add_first_skill)
            return
        }
        
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
        
        val buttonText = if (skills.isEmpty()) {
            getString(R.string.add_first_skill)
        } else {
            getString(R.string.add_new_skill)
        }
        btnAgregarHabilidad.text = buttonText
    }
    
    private fun updateSkillCard(card: LinearLayout, skill: Skill) {
        val titleView = card.findViewById<TextView>(R.id.titulo_javascript) ?: card.findViewById<TextView>(R.id.titulo_diseno)
        val descriptionView = card.findViewById<TextView>(R.id.descripcion_javascript) ?: card.findViewById<TextView>(R.id.descripcion_diseno)
        val priceView = card.findViewById<TextView>(R.id.precio_javascript) ?: card.findViewById<TextView>(R.id.precio_diseno)
        
        titleView?.text = skill.title
        descriptionView?.text = skill.description
        priceView?.text = skill.getFormattedPrice()
        
        val studentsView = card.findViewById<TextView>(R.id.estudiantes_javascript) ?: card.findViewById<TextView>(R.id.estudiantes_diseno)
        studentsView?.text = getString(R.string.default_students)
    }
    
    private fun editSkill(skill: Skill) {
        val intent = Intent(this, EditarHabilidad::class.java)
        intent.putExtra("skill_id", skill.id)
        intent.putExtra("skill_title", skill.title)
        intent.putExtra("skill_description", skill.description)
        intent.putExtra("skill_category", skill.category)
        intent.putExtra("skill_price", skill.price.toString())
        intent.putExtra("skill_modalidad", skill.modalidad)
        intent.putExtra("skill_rating", skill.rating)
        intent.putExtra("skill_review_count", skill.reviewCount)
        intent.putExtra("skill_sesiones_completadas", skill.sesionesCompletadas)
        startActivity(intent)
    }
    
    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            hideProfileContent()
            nombreUsuario.text = getString(R.string.loading_text)
            nombreUsuario.visibility = View.VISIBLE
            universidad.text = getString(R.string.loading_text)
            universidad.visibility = View.VISIBLE
            calificacion.text = getString(R.string.loading_text)
            calificacion.visibility = View.VISIBLE
        }
    }
    
    private fun hideProfileContent() {
        avatar.visibility = View.INVISIBLE
        nombreUsuario.visibility = View.INVISIBLE
        universidad.visibility = View.INVISIBLE
        calificacion.visibility = View.INVISIBLE
        
        findViewById<LinearLayout>(R.id.card_javascript)?.visibility = View.GONE
        findViewById<LinearLayout>(R.id.card_diseno)?.visibility = View.GONE
        
        btnAgregarHabilidad.visibility = View.INVISIBLE
    }
    
    private fun showProfileContent() {
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
            .setTitle(getString(R.string.logout_confirmation_title))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun logout() {
        // Limpiar caché al hacer logout
        UserProfileCache.invalidateAll()
        AuthenticationHelper.logout()
        
        val intent = Intent(this, Inicio::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Solo recargar si no hay skills válidas en caché
        // Esto permite que se recarguen las skills después de crear/editar/eliminar una habilidad
        if (!UserProfileCache.hasValidSkillsCache()) {
            loadUserData()
        } else {
            // Si tenemos caché válido, solo mostrar los datos
            val cachedUser = UserProfileCache.getCachedUser()
            val cachedSkills = UserProfileCache.getCachedSkills()
            
            if (cachedUser != null && cachedSkills != null) {
                currentUser = cachedUser
                userSkills = cachedSkills
                updateBasicUserInfo(cachedUser)
                updateUserRating()
                updateSkillsUI(cachedSkills)
                showProfileContent()
            } else {
                loadUserData()
            }
        }
    }
}
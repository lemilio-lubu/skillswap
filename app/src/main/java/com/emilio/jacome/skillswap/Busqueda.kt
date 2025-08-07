package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.emilio.jacome.skillswap.utils.Constants
import com.emilio.jacome.skillswap.utils.SkillRepository
import com.emilio.jacome.skillswap.utils.UserRepository
import com.emilio.jacome.skillswap.model.Skill
import com.emilio.jacome.skillswap.model.User
import com.google.firebase.firestore.QuerySnapshot

class Busqueda : AppCompatActivity() {

    // Views principales
    private lateinit var etBuscar: EditText
    private lateinit var layoutSugerencias: LinearLayout
    private lateinit var scrollViewContent: ScrollView
    private lateinit var skillsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResultados: LinearLayout
    private lateinit var tvResultadosContador: TextView

    // Contenedor de categorías
    private lateinit var categoriesContainer: LinearLayout
    
    // Data
    private var allSkills = mutableListOf<Skill>()
    private var filteredSkills = mutableListOf<Skill>()
    private var allUsers = mutableListOf<User>()
    private var selectedCategory = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_busqueda)

        initViews()
        setupSearchFunctionality()
        setupCategoryFilters()
        setupNavigationButtons()
        loadUsersAndSkills()
    }

    private fun initViews() {
        // Views de búsqueda
        etBuscar = findViewById(R.id.et_buscar)
        layoutSugerencias = findViewById(R.id.layout_sugerencias)
        scrollViewContent = findViewById(R.id.scroll_view_content)
        skillsContainer = findViewById(R.id.skills_container)
        progressBar = findViewById(R.id.progress_bar)
        tvNoResultados = findViewById(R.id.tv_no_resultados)
        tvResultadosContador = findViewById(R.id.tv_resultados_contador)

        // Contenedor de categorías
        categoriesContainer = findViewById(R.id.categories_container)
    }

    private fun setupNavigationButtons() {
        // Botón de perfil en el header
        val btnPerfil = findViewById<TextView>(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }
    }

    private fun setupSearchFunctionality() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    filterSkills(query)
                    showSugerencias(query)
                } else {
                    hideSugerencias()
                    if (query.isEmpty()) {
                        filterSkillsByCategory()
                    }
                }
            }
        })

        etBuscar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && etBuscar.text.toString().length >= 2) {
                showSugerencias(etBuscar.text.toString())
            } else {
                hideSugerencias()
            }
        }
    }

    private fun setupCategoryFilters() {
        // Crear filtros dinámicamente basados en Constants
        val allCategories = listOf("Todas") + Constants.Categories.LIST
        
        allCategories.forEach { category ->
            val filterView = createCategoryFilterView(category)
            categoriesContainer.addView(filterView)
        }

        // Seleccionar "Todas" por defecto
        selectedCategory = "Todas"
        updateCategoryFilterSelection()
    }

    private fun createCategoryFilterView(category: String): TextView {
        return TextView(this).apply {
            text = category
            setPadding(48, 24, 48, 24)
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            isClickable = true
            isFocusable = true
            
            // Establecer márgenes
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(0, 0, 24, 0)
            layoutParams = params
            
            setOnClickListener {
                selectedCategory = category
                updateCategoryFilterSelection()
                filterSkillsByCategory()
            }
        }
    }

    private fun updateCategoryFilterSelection() {
        // Actualizar todos los filtros de categoría
        for (i in 0 until categoriesContainer.childCount) {
            val filterView = categoriesContainer.getChildAt(i) as TextView
            val isSelected = filterView.text.toString() == selectedCategory
            
            if (isSelected) {
                filterView.setBackgroundResource(R.drawable.filter_selected_background)
                filterView.setTextColor(getColor(android.R.color.white))
            } else {
                filterView.setBackgroundResource(R.drawable.filter_unselected_background)
                filterView.setTextColor(getColor(android.R.color.black))
            }
        }
    }

    private fun filterSkills(query: String) {
        val normalizedQuery = normalizeText(query)
        
        // Si la consulta es específica, usar búsqueda de Firebase
        if (query.length >= 3) {
            performFirebaseSearch(query)
        } else {
            // Para consultas cortas, filtrar localmente
            performLocalSearch(normalizedQuery)
        }
    }

    private fun performFirebaseSearch(query: String) {
        progressBar.visibility = View.VISIBLE
        
        // Realizar búsqueda en paralelo tanto en skills como en usuarios
        val skillSearchTask = SkillRepository.searchSkills(query)
        val userSearchTask = UserRepository.searchUsersByName(query)
        
        // Combinar resultados de ambas búsquedas
        skillSearchTask.addOnCompleteListener { skillTask ->
            userSearchTask.addOnCompleteListener { userTask ->
                progressBar.visibility = View.GONE
                
                val searchResults = mutableListOf<Skill>()
                
                // Agregar skills encontrados directamente
                if (skillTask.isSuccessful) {
                    for (document in skillTask.result.documents) {
                        val skill = document.toObject(Skill::class.java)
                        skill?.let { searchResults.add(it) }
                    }
                }
                
                // Agregar skills de usuarios encontrados
                if (userTask.isSuccessful) {
                    val foundUserIds = mutableSetOf<String>()
                    for (document in userTask.result.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let { foundUserIds.add(it.uid) }
                    }
                    
                    // Buscar skills de estos usuarios
                    searchResults.addAll(allSkills.filter { skill ->
                        foundUserIds.contains(skill.userId)
                    })
                }
                
                // Aplicar filtros adicionales y eliminar duplicados
                filteredSkills.clear()
                val uniqueSkills = searchResults.distinctBy { it.id }
                
                filteredSkills.addAll(uniqueSkills.filter { skill ->
                    val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory
                    matchesCategory
                })
                
                updateSkillsDisplay()
            }
        }
        
        // Manejo de errores
        skillSearchTask.addOnFailureListener {
            userSearchTask.addOnFailureListener {
                progressBar.visibility = View.GONE
                // En caso de error, usar búsqueda local
                performLocalSearch(normalizeText(query))
            }
        }
    }

    private fun performLocalSearch(normalizedQuery: String) {
        filteredSkills.clear()

        filteredSkills.addAll(allSkills.filter { skill ->
            // Buscar en título, descripción, categoría de la skill
            val matchesSkillData = normalizeText(skill.title).contains(normalizedQuery) ||
                    normalizeText(skill.description).contains(normalizedQuery) ||
                    normalizeText(skill.category).contains(normalizedQuery)
            
            // Buscar en datos del usuario
            val matchesUserData = normalizeText(skill.userName).contains(normalizedQuery) ||
                    // Buscar en datos adicionales del usuario si están disponibles
                    allUsers.find { it.uid == skill.userId }?.let { user ->
                        normalizeText(user.name).contains(normalizedQuery) ||
                        normalizeText(user.university).contains(normalizedQuery) ||
                        normalizeText(user.bio).contains(normalizedQuery)
                    } ?: false

            val matchesQuery = matchesSkillData || matchesUserData
            val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory

            matchesQuery && matchesCategory
        })

        updateSkillsDisplay()
    }

    private fun filterSkillsByCategory() {
        if (etBuscar.text.toString().trim().isNotEmpty()) {
            filterSkills(etBuscar.text.toString().trim())
        } else {
            // Si no hay texto de búsqueda, usar filtro de categoría de Firebase
            if (selectedCategory != "Todas") {
                loadSkillsByCategory(selectedCategory)
            } else {
                applyAllFilters()
            }
        }
    }

    private fun loadSkillsByCategory(category: String) {
        progressBar.visibility = View.VISIBLE
        
        SkillRepository.getSkillsByCategory(category)
            .addOnSuccessListener { querySnapshot ->
                progressBar.visibility = View.GONE
                filteredSkills.clear()
                
                for (document in querySnapshot.documents) {
                    val skill = document.toObject(Skill::class.java)
                    skill?.let { 
                        filteredSkills.add(it)
                    }
                }
                
                updateSkillsDisplay()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                // En caso de error, usar filtro local
                applyAllFilters()
            }
    }

    private fun applyAllFilters() {
        filteredSkills.clear()

        filteredSkills.addAll(allSkills.filter { skill ->
            val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory
            matchesCategory
        })

        updateSkillsDisplay()
    }

    private fun showSugerencias(query: String) {
        layoutSugerencias.removeAllViews()
        layoutSugerencias.visibility = View.VISIBLE

        val sugerencias = generateSugerencias(query)
        sugerencias.take(5).forEach { sugerencia ->
            val tvSugerencia = TextView(this).apply {
                text = sugerencia
                setPadding(32, 24, 32, 24)
                textSize = 14f
                setTextColor(getColor(R.color.text_primary))
                setBackgroundResource(android.R.drawable.list_selector_background)
                setOnClickListener {
                    etBuscar.setText(sugerencia)
                    etBuscar.setSelection(sugerencia.length)
                    hideSugerencias()
                    filterSkills(sugerencia)
                }
            }
            layoutSugerencias.addView(tvSugerencia)
        }
    }

    private fun hideSugerencias() {
        layoutSugerencias.visibility = View.GONE
    }

    private fun generateSugerencias(query: String): List<String> {
        val sugerencias = mutableSetOf<String>()
        val normalizedQuery = normalizeText(query)

        // Sugerencias basadas en habilidades existentes
        allSkills.forEach { skill ->
            if (normalizeText(skill.title).contains(normalizedQuery)) {
                sugerencias.add(skill.title)
            }
            if (normalizeText(skill.category).contains(normalizedQuery)) {
                sugerencias.add(skill.category)
            }
            if (normalizeText(skill.userName).contains(normalizedQuery)) {
                sugerencias.add(skill.userName)
            }
        }

        // Sugerencias basadas en usuarios registrados
        allUsers.forEach { user ->
            if (normalizeText(user.name).contains(normalizedQuery)) {
                sugerencias.add(user.name)
            }
            if (normalizeText(user.university).contains(normalizedQuery)) {
                sugerencias.add(user.university)
            }
        }

        // Sugerencias comunes
        val commonSuggestions = listOf(
            "programación", "python", "java", "matemáticas", "álgebra", "cálculo",
            "inglés", "español", "francés", "guitarra", "piano", "dibujo",
            "Universidad Nacional", "UNAM", "Universidad Tecnológica"
        )

        commonSuggestions.forEach { suggestion ->
            if (normalizeText(suggestion).contains(normalizedQuery)) {
                sugerencias.add(suggestion)
            }
        }

        return sugerencias.toList().sorted()
    }

    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
    }

    private fun loadUsersAndSkills() {
        progressBar.visibility = View.VISIBLE

        // Cargar usuarios y skills en paralelo
        val skillsTask = SkillRepository.getAllActiveSkills()
        val usersTask = UserRepository.getAllUsers()

        skillsTask.addOnCompleteListener { skillTaskResult ->
            usersTask.addOnCompleteListener { userTaskResult ->
                progressBar.visibility = View.GONE
                
                // Procesar skills
                allSkills.clear()
                if (skillTaskResult.isSuccessful) {
                    for (document in skillTaskResult.result.documents) {
                        val skill = document.toObject(Skill::class.java)
                        skill?.let { allSkills.add(it) }
                    }
                }
                
                // Procesar usuarios
                allUsers.clear()
                if (userTaskResult.isSuccessful) {
                    for (document in userTaskResult.result.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let { allUsers.add(it) }
                    }
                }

                // Enriquecer skills con información de usuarios
                enrichSkillsWithUserData()

                applyAllFilters()
            }
        }
        
        // Manejo de errores
        skillsTask.addOnFailureListener {
            usersTask.addOnFailureListener {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun enrichSkillsWithUserData() {
        // Enriquecer información de skills con datos de usuarios
        allSkills.forEach { skill ->
            val user = allUsers.find { it.uid == skill.userId }
            user?.let {
                // Si el skill no tiene userName o está vacío, usar el del usuario
                if (skill.userName.isEmpty()) {
                    skill.userName = it.getDisplayName()
                }
                // Si el skill no tiene userAvatar, usar información del usuario
                if (skill.userAvatar.isEmpty()) {
                    skill.userAvatar = if (it.profileImageUrl.isNotEmpty()) 
                        it.profileImageUrl 
                    else 
                        it.name.split(" ").map { name -> name.first() }.joinToString("")
                }
            }
        }
    }

    private fun updateSkillsDisplay() {
        // Limpiar las cards dinámicas
        val dynamicCards = skillsContainer.children.filter { view ->
            view.tag == "dynamic_card"
        }.toList()

        dynamicCards.forEach { view ->
            skillsContainer.removeView(view)
        }

        val count = filteredSkills.size
        tvResultadosContador.text = when {
            count == 0 -> "No se encontraron resultados"
            count == 1 -> "1 resultado encontrado"
            else -> "$count resultados encontrados"
        }

        if (count == 0) {
            tvNoResultados.visibility = View.VISIBLE
            scrollViewContent.visibility = View.GONE
        } else {
            tvNoResultados.visibility = View.GONE
            scrollViewContent.visibility = View.VISIBLE

            // Crear cards dinámicas para todas las habilidades filtradas
            filteredSkills.forEach { skill ->
                val cardView = createSkillCard(skill)
                cardView.tag = "dynamic_card"
                skillsContainer.addView(cardView)
            }
        }
    }

    private fun createSkillCard(skill: Skill): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.card_background)
            setPadding(48, 48, 48, 48)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
            isClickable = true
            isFocusable = true
            elevation = 4f
        }

        // Título
        val titleView = TextView(this).apply {
            text = skill.title
            setTextColor(getColor(R.color.text_primary))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }

        // Descripción
        val descriptionView = TextView(this).apply {
            text = skill.description
            setTextColor(getColor(R.color.text_secondary))
            textSize = 14f
            maxLines = 3
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
        }

        // Layout inferior con instructor y precio
        val bottomLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val instructorInfoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val instructorView = TextView(this).apply {
            text = "Por ${skill.userName}"
            setTextColor(getColor(R.color.text_secondary))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Mostrar universidad del instructor si está disponible
        val user = allUsers.find { it.uid == skill.userId }
        val universityView = TextView(this).apply {
            text = user?.university ?: ""
            setTextColor(getColor(R.color.text_hint))
            textSize = 11f
            visibility = if (user?.university?.isNotEmpty() == true) View.VISIBLE else View.GONE
        }

        instructorInfoLayout.addView(instructorView)
        instructorInfoLayout.addView(universityView)

        val priceView = TextView(this).apply {
            text = skill.getFormattedPrice()
            setTextColor(getColor(R.color.accent_primary))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        bottomLayout.addView(instructorInfoLayout)
        bottomLayout.addView(priceView)

        cardLayout.addView(titleView)
        cardLayout.addView(descriptionView)
        cardLayout.addView(bottomLayout)

        // Click listener para navegar al detalle
        cardLayout.setOnClickListener {
            val user = allUsers.find { it.uid == skill.userId }
            val intent = Intent(this, DetalleHabilidad::class.java)
            intent.putExtra("skill_title", skill.title)
            intent.putExtra("skill_description", skill.description)
            intent.putExtra("instructor_name", skill.userName)
            intent.putExtra("instructor_avatar", if (skill.userAvatar.isNotEmpty()) skill.userAvatar else skill.userName.split(" ").map { it.first() }.joinToString(""))
            intent.putExtra("skill_price", skill.price.toString())
            intent.putExtra("skill_category", skill.category)
            intent.putExtra("skill_modalidad", skill.modalidad)
            intent.putExtra("skill_rating", skill.rating.toString())
            intent.putExtra("skill_review_count", skill.reviewCount.toString())
            intent.putExtra("skill_id", skill.id)
            intent.putExtra("instructor_user_id", skill.userId)
            intent.putExtra("instructor_university", user?.university ?: "")
            intent.putExtra("instructor_bio", user?.bio ?: "")
            intent.putExtra("instructor_email", user?.email ?: "")
            startActivity(intent)
        }

        return cardLayout
    }
}
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

class Busqueda : AppCompatActivity() {

    private lateinit var etBuscar: EditText
    private lateinit var scrollViewContent: ScrollView
    private lateinit var skillsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResultados: LinearLayout
    private lateinit var tvResultadosContador: TextView

    private lateinit var categoriesContainer: LinearLayout
    
    private var allSkills = mutableListOf<Skill>()
    private var filteredSkills = mutableListOf<Skill>()
    private var allUsers = mutableListOf<User>()
    private var selectedCategory = "Todas"
    private lateinit var layoutSugerencias: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_busqueda)

        initViews()
        setupSearchFunctionality()
        setupCategoryFilters()
        setupNavigationButtons()
        loadSkills()
    }

    private fun initViews() {
        etBuscar = findViewById(R.id.et_buscar)
        scrollViewContent = findViewById(R.id.scroll_view_content)
        skillsContainer = findViewById(R.id.skills_container)
        progressBar = findViewById(R.id.progress_bar)
        tvNoResultados = findViewById(R.id.tv_no_resultados)
        tvResultadosContador = findViewById(R.id.tv_resultados_contador)

        categoriesContainer = findViewById(R.id.categories_container)
        
        layoutSugerencias = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
    }

    private fun setupNavigationButtons() {
        val btnPerfil = findViewById<ImageView>(R.id.btn_perfil)
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
                } else if (query.isEmpty()) {
                    filterSkillsByCategory()
                }
            }
        })
    }

    private fun setupCategoryFilters() {
        // Crear filtros dinámicamente basados en Constants
        val allCategories = listOf(getString(R.string.category_all)) + Constants.CATEGORIES

        allCategories.forEach { category ->
            val filterView = createCategoryFilterView(category)
            categoriesContainer.addView(filterView)
        }

        selectedCategory = getString(R.string.category_all)
        updateCategoryFilterSelection()
    }

    private fun createCategoryFilterView(category: String): TextView {
        return TextView(this).apply {
            text = category
            setPadding(32, 20, 32, 20)
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            isClickable = true
            isFocusable = true
            
            // Establecer márgenes
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
            
            setOnClickListener {
                selectedCategory = category
                updateCategoryFilterSelection()
                filterSkillsByCategory()
            }
        }
    }

    private fun updateCategoryFilterSelection() {
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
        
        if (normalizedQuery.length >= 2) {
            performSmartSearch(normalizedQuery)
        } else {
            applyAllFilters()
        }
    }

    private fun performSmartSearch(normalizedQuery: String) {
        progressBar.visibility = View.VISIBLE
        filteredSkills.clear()

        // Algoritmo de búsqueda mejorado con scoring
        val searchResults = mutableMapOf<Skill, Int>()

        allSkills.forEach { skill ->
            var score = 0
            
            // Buscar en título (mayor peso)
            val normalizedTitle = normalizeText(skill.title)
            when {
                normalizedTitle == normalizedQuery -> score += 100
                normalizedTitle.startsWith(normalizedQuery) -> score += 80
                normalizedTitle.contains(normalizedQuery) -> score += 60
            }

            // Buscar en descripción (peso medio)
            val normalizedDescription = normalizeText(skill.description)
            when {
                normalizedDescription.contains(normalizedQuery) -> score += 40
            }

            // Buscar en categoría (peso medio)
            val normalizedCategory = normalizeText(skill.category)
            when {
                normalizedCategory == normalizedQuery -> score += 70
                normalizedCategory.contains(normalizedQuery) -> score += 50
            }

            // Buscar en nombre del instructor (peso bajo)
            val normalizedUserName = normalizeText(skill.userName)
            when {
                normalizedUserName.contains(normalizedQuery) -> score += 30
            }

            // Buscar en datos del usuario (peso muy bajo)
            allUsers.find { it.uid == skill.userId }?.let { user ->
                val normalizedUserBio = normalizeText(user.bio)
                val normalizedUniversity = normalizeText(user.university)
                when {
                    normalizedUserBio.contains(normalizedQuery) -> score += 20
                    normalizedUniversity.contains(normalizedQuery) -> score += 25
                }
            }

            // Solo incluir resultados con score > 0
            if (score > 0) {
                searchResults[skill] = score
            }
        }

        // Ordenar por score y aplicar filtro de categoría
        val sortedResults = searchResults.toList()
            .sortedByDescending { it.second }
            .map { it.first }
            .filter { skill ->
                selectedCategory == "Todas" || skill.category == selectedCategory
            }

        filteredSkills.addAll(sortedResults)
        progressBar.visibility = View.GONE
        updateSkillsDisplay()
    }

    private fun filterSkillsByCategory() {
        val query = etBuscar.text.toString().trim()
        if (query.isNotEmpty()) {
            filterSkills(query)
        } else {
            applyAllFilters()
        }
    }

    private fun applyAllFilters() {
        filteredSkills.clear()

        filteredSkills.addAll(allSkills.filter { skill ->
            val isNotCurrentUser = skill.userId != FirebaseManager.getCurrentUserId()
            val matchesCategory = selectedCategory == getString(R.string.category_all) || skill.category == selectedCategory
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

        // Límite para evitar procesar demasiados elementos
        var skillProcessed = 0
        var userProcessed = 0
        val maxProcessed = 50

        // Sugerencias basadas en habilidades existentes (prioritarias)
        allSkills.take(maxProcessed).forEach { skill ->
            val normalizedTitle = normalizeText(skill.title)
            val normalizedCategory = normalizeText(skill.category)
            val normalizedUserName = normalizeText(skill.userName)
            
            when {
                normalizedTitle.startsWith(normalizedQuery) -> sugerencias.add(skill.title)
                normalizedTitle.contains(normalizedQuery) -> sugerencias.add(skill.title)
                normalizedCategory.startsWith(normalizedQuery) -> sugerencias.add(skill.category)
                normalizedCategory.contains(normalizedQuery) -> sugerencias.add(skill.category)
                normalizedUserName.startsWith(normalizedQuery) -> sugerencias.add(skill.userName)
                normalizedUserName.contains(normalizedQuery) -> sugerencias.add(skill.userName)
            }
            skillProcessed++
        }

        // Sugerencias basadas en usuarios registrados (limitadas)
        allUsers.take(maxProcessed).forEach { user ->
            val normalizedName = normalizeText(user.name)
            val normalizedUniversity = normalizeText(user.university)
            
            when {
                normalizedName.startsWith(normalizedQuery) -> sugerencias.add(user.name)
                normalizedName.contains(normalizedQuery) -> sugerencias.add(user.name)
                normalizedUniversity.startsWith(normalizedQuery) && user.university.isNotEmpty() -> sugerencias.add(user.university)
                normalizedUniversity.contains(normalizedQuery) && user.university.isNotEmpty() -> sugerencias.add(user.university)
            }
            userProcessed++
        }

        // Sugerencias comunes (solo si coinciden)
        val commonSuggestions = listOf(
            "programación", "python", "java", "javascript", "kotlin", "android",
            "matemáticas", "álgebra", "cálculo", "estadística", "física",
            "inglés", "español", "francés", "alemán", "italiano",
            "guitarra", "piano", "violín", "dibujo", "pintura", "fotografía",
            "diseño", "marketing", "economía", "contabilidad", "administración"
        )

        commonSuggestions.forEach { suggestion ->
            val normalizedSuggestion = normalizeText(suggestion)
            if (normalizedSuggestion.startsWith(normalizedQuery) || normalizedSuggestion.contains(normalizedQuery)) {
                sugerencias.add(suggestion)
            }
        }

        // Retornar ordenado: primero las que empiezan con la query, luego las que contienen
        return sugerencias.toList()
            .sortedWith(compareBy({ !normalizeText(it).startsWith(normalizedQuery) }, { it }))
    }

    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
    }

    private fun loadSkills() {
        progressBar.visibility = View.VISIBLE
        val currentUserId = FirebaseManager.getCurrentUserId()

        // Cargar habilidades activas únicamente (excluyendo las del usuario actual)
        SkillRepository.getAllActiveSkills()
            .addOnSuccessListener { skillSnapshot ->
                // Procesar skills con validación adicional
                allSkills.clear()
                for (document in skillSnapshot.documents) {
                    document.toObject(Skill::class.java)?.let { skill ->
                        // Doble validación: debe ser activa y no ser del usuario actual
                        if (skill.active && skill.userId != currentUserId) {
                            allSkills.add(skill)
                        }
                    }
                }

                // Cargar usuarios solo después de tener las skills
                loadUsers()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvResultadosContador.text = getString(R.string.error_loading_skills)
            }
    }

    private fun loadUsers() {
        val currentUserId = FirebaseManager.getCurrentUserId()
        
        UserRepository.getAllUsers()
            .addOnSuccessListener { userSnapshot ->
                progressBar.visibility = View.GONE
                
                // Procesar usuarios (excluyendo al usuario actual)
                allUsers.clear()
                for (document in userSnapshot.documents) {
                    document.toObject(User::class.java)?.let { user ->
                        // Validación adicional: no incluir al usuario actual
                        if (user.uid != currentUserId) {
                            allUsers.add(user)
                        }
                    }
                }

                // Enriquecer skills con información de usuarios y mostrar
                enrichSkillsWithUserData()
                applyAllFilters()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                // Aunque falle la carga de usuarios, mostrar las skills
                applyAllFilters()
            }
    }

    private fun enrichSkillsWithUserData() {
        // Crear un mapa para acceso rápido a usuarios
        val userMap = allUsers.associateBy { it.uid }
        
        // Enriquecer información de skills con datos de usuarios
        allSkills.forEach { skill ->
            userMap[skill.userId]?.let { user ->
                // Si el skill no tiene userName o está vacío, usar el del usuario
                if (skill.userName.isEmpty()) {
                    skill.userName = user.name.ifEmpty { getString(R.string.default_user) }
                }
                // Si el skill no tiene userAvatar, generar iniciales
                if (skill.userAvatar.isEmpty()) {
                    skill.userAvatar = user.name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                        .ifEmpty { getString(R.string.default_user_initial) }
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
            count == 0 -> getString(R.string.results_count_zero)
            count == 1 -> getString(R.string.results_count_one)
            else -> getString(R.string.results_count_many, count)
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

        // Layout para información del instructor
        val instructorInfoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val instructorView = TextView(this).apply {
            text = getString(R.string.skill_by_instructor, skill.userName)
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
            val intent = Intent(this, DetalleHabilidad::class.java)
            intent.putExtra("skill_title", skill.title)
            intent.putExtra("skill_description", skill.description)
            intent.putExtra("instructor_name", skill.userName)
            intent.putExtra("instructor_avatar", if (skill.userAvatar.isNotEmpty()) skill.userAvatar else skill.userName.split(" ").map { it.firstOrNull()?.toString() ?: "" }.take(2).joinToString(""))
            intent.putExtra("skill_price", skill.getFormattedPrice())
            intent.putExtra("skill_category", skill.category)
            intent.putExtra("skill_modalidad", skill.modalidad)
            intent.putExtra("skill_incluye", skill.incluye)
            intent.putExtra("skill_rating", skill.rating.toString())
            intent.putExtra("skill_review_count", skill.reviewCount.toString())
            intent.putExtra("skill_id", skill.id)
            intent.putExtra("instructor_id", skill.userId)  // Añadiendo el ID del instructor
            startActivity(intent)
        }

        return cardLayout
    }
}
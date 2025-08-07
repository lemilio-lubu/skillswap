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
import com.emilio.jacome.skillswap.model.Skill
import com.google.firebase.firestore.QuerySnapshot

class Busqueda : AppCompatActivity() {

    // Views principales
    private lateinit var etBuscar: EditText
    private lateinit var scrollViewContent: ScrollView
    private lateinit var skillsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResultados: LinearLayout
    private lateinit var tvResultadosContador: TextView

    // Filtros de categoría
    private val categoryFilters = mutableMapOf<TextView, String>()

    // Data
    private var allSkills = mutableListOf<Skill>()
    private var filteredSkills = mutableListOf<Skill>()
    private var selectedCategory = "Todas"

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
        // Views de búsqueda
        etBuscar = findViewById(R.id.et_buscar)
        scrollViewContent = findViewById(R.id.scroll_view_content)
        skillsContainer = findViewById(R.id.skills_container)
        progressBar = findViewById(R.id.progress_bar)
        tvNoResultados = findViewById(R.id.tv_no_resultados)
        tvResultadosContador = findViewById(R.id.tv_resultados_contador)
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
                } else if (query.isEmpty()) {
                    filterSkillsByCategory()
                }
            }
        })
    }

    private fun setupCategoryFilters() {
        // Crear filtros dinámicamente basados en Constants
        val filtrosScrollLayout = findViewById<LinearLayout>(R.id.filtros_scroll_layout)

        // Limpiar filtros existentes
        filtrosScrollLayout.removeAllViews()
        categoryFilters.clear()

        // Agregar filtro "Todas"
        val filtroTodas = createCategoryFilterView("Todas")
        filtrosScrollLayout.addView(filtroTodas)
        categoryFilters[filtroTodas] = "Todas"

        // Agregar filtros de categorías desde Constants
        Constants.Categories.LIST.forEach { category ->
            val filtroView = createCategoryFilterView(category)
            filtrosScrollLayout.addView(filtroView)
            categoryFilters[filtroView] = category
        }

        // Configurar click listeners
        categoryFilters.forEach { (view, category) ->
            view.setOnClickListener {
                selectCategoryFilter(view, category)
                selectedCategory = category
                filterSkillsByCategory()
            }
        }

        // Seleccionar "Todas" por defecto
        categoryFilters.entries.first { it.value == "Todas" }.let { entry ->
            selectCategoryFilter(entry.key, entry.value)
        }
    }

    private fun createCategoryFilterView(categoryName: String): TextView {
        return TextView(this).apply {
            text = categoryName
            setPadding(32, 24, 32, 24)
            textSize = 14f
            setTextColor(getColor(android.R.color.black))
            setBackgroundResource(R.drawable.category_filter_background)
            isClickable = true
            isFocusable = true

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)
            layoutParams = params
        }
    }

    private fun selectCategoryFilter(selectedFilter: TextView, category: String) {
        // Resetear todos los filtros
        categoryFilters.keys.forEach { filter ->
            filter.setBackgroundResource(R.drawable.category_filter_background)
            filter.setTextColor(getColor(android.R.color.black))
        }

        // Seleccionar el filtro actual
        selectedFilter.setBackgroundResource(R.drawable.category_filter_selected)
        selectedFilter.setTextColor(getColor(android.R.color.white))
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

        SkillRepository.searchSkills(query)
            .addOnSuccessListener { querySnapshot ->
                progressBar.visibility = View.GONE
                val searchResults = mutableListOf<Skill>()

                for (document in querySnapshot.documents) {
                    val skill = document.toObject(Skill::class.java)
                    skill?.let { searchResults.add(it) }
                }

                // Filtrar resultados excluyendo habilidades del usuario logueado
                filteredSkills.clear()
                filteredSkills.addAll(searchResults.filter { skill ->
                    val isNotCurrentUser = skill.userId != FirebaseManager.getCurrentUserId()
                    val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory

                    isNotCurrentUser && matchesCategory
                })

                updateSkillsDisplay()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                // En caso de error, usar búsqueda local
                performLocalSearch(normalizeText(query))
            }
    }

    private fun performLocalSearch(normalizedQuery: String) {
        filteredSkills.clear()

        filteredSkills.addAll(allSkills.filter { skill ->
            val matchesQuery = normalizeText(skill.title).contains(normalizedQuery) ||
                    normalizeText(skill.description).contains(normalizedQuery) ||
                    normalizeText(skill.category).contains(normalizedQuery) ||
                    normalizeText(skill.userName).contains(normalizedQuery)

            val isNotCurrentUser = skill.userId != FirebaseManager.getCurrentUserId()
            val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory

            matchesQuery && isNotCurrentUser && matchesCategory
        })

        updateSkillsDisplay()
    }

    private fun filterSkillsByCategory() {
        if (etBuscar.text.toString().trim().isNotEmpty()) {
            filterSkills(etBuscar.text.toString().trim())
        } else {
            applyAllFilters()
        }
    }

    private fun applyAllFilters() {
        filteredSkills.clear()

        filteredSkills.addAll(allSkills.filter { skill ->
            val isNotCurrentUser = skill.userId != FirebaseManager.getCurrentUserId()
            val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory

            isNotCurrentUser && matchesCategory
        })

        updateSkillsDisplay()
    }


    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
    }

    private fun loadSkills() {
        progressBar.visibility = View.VISIBLE

        // Cargar skills desde Firebase
        SkillRepository.getAllActiveSkills()
            .addOnSuccessListener { querySnapshot ->
                progressBar.visibility = View.GONE
                allSkills.clear()

                for (document in querySnapshot.documents) {
                    val skill = document.toObject(Skill::class.java)
                    skill?.let { allSkills.add(it) }
                }

                applyAllFilters()
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                // En caso de error, mostrar mensaje de error
                showErrorMessage()
            }
    }

    private fun showErrorMessage() {
        allSkills.clear()
        filteredSkills.clear()

        tvResultadosContador.text = "Error al cargar las habilidades"
        tvNoResultados.visibility = View.VISIBLE
        scrollViewContent.visibility = View.GONE

        // Actualizar el mensaje de error para ser más específico
        val errorIcon = tvNoResultados.getChildAt(0) as TextView
        val errorTitle = tvNoResultados.getChildAt(1) as TextView
        val errorMessage = tvNoResultados.getChildAt(2) as TextView

        errorIcon.text = "⚠️"
        errorTitle.text = "Error de conexión"
        errorMessage.text = "No se pudieron cargar las habilidades.\nVerifica tu conexión a internet."
    }

    private fun updateSkillsDisplay() {
        // Limpiar todas las cards dinámicas
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
            setBackgroundColor(getColor(android.R.color.white))
            setPadding(48, 48, 48, 48)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 36)
            layoutParams = params
            isClickable = true
            isFocusable = true
            elevation = 6f
        }

        // Título
        val titleView = TextView(this).apply {
            text = skill.title
            setTextColor(getColor(android.R.color.black))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
        }

        // Descripción
        val descriptionView = TextView(this).apply {
            text = skill.description
            setTextColor(getColor(android.R.color.darker_gray))
            textSize = 14f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 36)
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

        val instructorView = TextView(this).apply {
            text = "Por ${skill.userName}"
            setTextColor(getColor(android.R.color.darker_gray))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val priceView = TextView(this).apply {
            text = skill.getFormattedPrice()
            setTextColor(getColor(android.R.color.holo_blue_light))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        bottomLayout.addView(instructorView)
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
            intent.putExtra("instructor_avatar", if (skill.userAvatar.isNotEmpty()) skill.userAvatar else skill.userName.split(" ").map { it.first() }.joinToString(""))
            intent.putExtra("skill_price", skill.price.toString())
            intent.putExtra("skill_category", skill.category)
            intent.putExtra("skill_modalidad", skill.modalidad)
            intent.putExtra("skill_rating", skill.rating.toString())
            intent.putExtra("skill_review_count", skill.reviewCount.toString())
            intent.putExtra("skill_id", skill.id)
            startActivity(intent)
        }

        return cardLayout
    }
}
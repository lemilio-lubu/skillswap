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
    private lateinit var layoutSugerencias: LinearLayout
    private lateinit var scrollViewContent: ScrollView
    private lateinit var skillsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResultados: LinearLayout
    private lateinit var tvResultadosContador: TextView

    // Filtros de categoría
    private lateinit var filtroTodas: TextView
    private lateinit var filtroMatematicas: TextView
    private lateinit var filtroProgramacion: TextView

    // Filtros avanzados
    private lateinit var spinnerPrecio: Spinner
    private lateinit var spinnerModalidad: Spinner
    private lateinit var btnFiltrosAvanzados: Button
    private lateinit var layoutFiltrosAvanzados: LinearLayout
    private var filtrosAvanzadosVisible = false

    // Data
    private var allSkills = mutableListOf<Skill>()
    private var filteredSkills = mutableListOf<Skill>()
    private var selectedCategory = "Todas"
    private var selectedPriceRange = "Todos los precios"
    private var selectedModality = "Todas las modalidades"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_busqueda)

        initViews()
        setupSearchFunctionality()
        setupCategoryFilters()
        setupAdvancedFilters()
        setupNavigationButtons()
        loadSkills()
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

        // Filtros de categoría
        filtroTodas = findViewById(R.id.filtro_todas)
        filtroMatematicas = findViewById(R.id.filtro_matematicas)
        filtroProgramacion = findViewById(R.id.filtro_programacion)

        // Filtros avanzados
        spinnerPrecio = findViewById(R.id.spinner_precio)
        spinnerModalidad = findViewById(R.id.spinner_modalidad_filtro)
        btnFiltrosAvanzados = findViewById(R.id.btn_filtros_avanzados)
        layoutFiltrosAvanzados = findViewById(R.id.layout_filtros_avanzados)
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
        val filters = mapOf(
            filtroTodas to "Todas",
            filtroMatematicas to "Matemáticas",
            filtroProgramacion to "Programación"
        )

        filters.forEach { (view, category) ->
            view.setOnClickListener {
                selectCategoryFilter(view, category)
                selectedCategory = category
                filterSkillsByCategory()
            }
        }

        // Seleccionar "Todas" por defecto
        selectCategoryFilter(filtroTodas, "Todas")
    }

    private fun setupAdvancedFilters() {
        // Configurar spinner de precios
        val preciosAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Todos los precios", "$0-$5", "$5-$10", "$10-$20", "$20+")
        )
        preciosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrecio.adapter = preciosAdapter

        // Configurar spinner de modalidad
        val modalidadAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Todas las modalidades", "Presencial", "Virtual", "Híbrida")
        )
        modalidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModalidad.adapter = modalidadAdapter

        // Listeners
        spinnerPrecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPriceRange = parent?.getItemAtPosition(position).toString()
                applyAllFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerModalidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModality = parent?.getItemAtPosition(position).toString()
                applyAllFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Botón filtros avanzados
        btnFiltrosAvanzados.setOnClickListener {
            toggleAdvancedFilters()
        }
    }

    private fun selectCategoryFilter(selectedFilter: TextView, category: String) {
        // Resetear todos los filtros
        val filters = listOf(filtroTodas, filtroMatematicas, filtroProgramacion)
        filters.forEach { filter ->
            filter.setBackgroundColor(getColor(android.R.color.darker_gray))
            filter.setTextColor(getColor(android.R.color.black))
        }

        // Seleccionar el filtro actual
        selectedFilter.setBackgroundColor(getColor(android.R.color.holo_blue_light))
        selectedFilter.setTextColor(getColor(android.R.color.white))
    }

    private fun toggleAdvancedFilters() {
        filtrosAvanzadosVisible = !filtrosAvanzadosVisible
        layoutFiltrosAvanzados.visibility = if (filtrosAvanzadosVisible) View.VISIBLE else View.GONE
        btnFiltrosAvanzados.text = if (filtrosAvanzadosVisible) "Ocultar filtros" else "Más filtros"
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
                
                // Combinar resultados de Firebase con filtros locales
                filteredSkills.clear()
                filteredSkills.addAll(searchResults.filter { skill ->
                    val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory
                    val matchesPrice = matchesPriceRange(skill.price)
                    val matchesModality = selectedModality == "Todas las modalidades" || skill.modalidad == selectedModality
                    
                    matchesCategory && matchesPrice && matchesModality
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

            val matchesCategory = selectedCategory == "Todas" || skill.category == selectedCategory
            val matchesPrice = matchesPriceRange(skill.price)
            val matchesModality = selectedModality == "Todas las modalidades" || skill.modalidad == selectedModality

            matchesQuery && matchesCategory && matchesPrice && matchesModality
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
                        // Aplicar filtros adicionales
                        val matchesPrice = matchesPriceRange(it.price)
                        val matchesModality = selectedModality == "Todas las modalidades" || it.modalidad == selectedModality
                        
                        if (matchesPrice && matchesModality) {
                            filteredSkills.add(it)
                        }
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
            val matchesPrice = matchesPriceRange(skill.price)
            val matchesModality = selectedModality == "Todas las modalidades" || skill.modalidad == selectedModality

            matchesCategory && matchesPrice && matchesModality
        })

        updateSkillsDisplay()
    }

    private fun matchesPriceRange(price: Double): Boolean {
        return when (selectedPriceRange) {
            "Todos los precios" -> true
            "$0-$5" -> price <= 5.0
            "$5-$10" -> price > 5.0 && price <= 10.0
            "$10-$20" -> price > 10.0 && price <= 20.0
            "$20+" -> price > 20.0
            else -> true
        }
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
                setTextColor(getColor(android.R.color.black))
                setBackgroundResource(android.R.drawable.list_selector_background)
                setOnClickListener {
                    etBuscar.setText(sugerencia)
                    etBuscar.setSelection(sugerencia.length)
                    hideSugerencias()
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
        }

        // Sugerencias comunes
        val commonSuggestions = listOf(
            "programación", "python", "java", "matemáticas", "álgebra", "cálculo",
            "inglés", "español", "francés", "guitarra", "piano", "dibujo"
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

                // Configurar clicks de las cards originales
                setupOriginalCards()

                applyAllFilters()
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                // En caso de error, mostrar mensaje o usar datos de ejemplo
                loadFallbackData()
            }
    }

    private fun loadFallbackData() {
        // Datos de ejemplo como fallback en caso de error con Firebase
        val exampleSkills = listOf(
            Skill(
                id = "1",
                title = "Matemáticas básicas",
                description = "Ayudo con álgebra, geometría y cálculo básico. Explico conceptos claros y ejercicios prácticos.",
                category = "Matemáticas",
                price = 5.0,
                modalidad = "Presencial",
                userId = "user1",
                userName = "María García"
            ),
            Skill(
                id = "2",
                title = "Python para principiantes",
                description = "Aprende programación desde cero. Incluye proyectos prácticos y ejercicios.",
                category = "Programación",
                price = 8.0,
                modalidad = "Virtual",
                userId = "user2",
                userName = "Carlos López"
            ),
            Skill(
                id = "3",
                title = "Inglés conversacional",
                description = "Practica conversación en inglés. Mejora tu fluidez y confianza al hablar.",
                category = "Idiomas",
                price = 6.0,
                modalidad = "Híbrida",
                userId = "user3",
                userName = "Ana Ruiz"
            )
        )

        allSkills.clear()
        allSkills.addAll(exampleSkills)
        setupOriginalCards()
        applyAllFilters()
    }

    private fun setupOriginalCards() {
        // Cards originales de habilidades - mantener compatibilidad
        try {
            val cardMatematicas = findViewById<LinearLayout>(R.id.card_matematicas)
            val cardPython = findViewById<LinearLayout>(R.id.card_python)
            val cardIngles = findViewById<LinearLayout>(R.id.card_ingles)

            // Buscar skills específicas en los datos cargados
            val matematicasSkill = allSkills.find { it.title.contains("Matemáticas", ignoreCase = true) || it.category == "Matemáticas" }
            val pythonSkill = allSkills.find { it.title.contains("Python", ignoreCase = true) || it.title.contains("Programación", ignoreCase = true) }
            val inglesSkill = allSkills.find { it.title.contains("Inglés", ignoreCase = true) || it.category == "Idiomas" }

            cardMatematicas?.setOnClickListener {
                val skill = matematicasSkill
                val intent = Intent(this, DetalleHabilidad::class.java)
                intent.putExtra("skill_title", skill?.title ?: "Matemáticas básicas")
                intent.putExtra("skill_description", skill?.description ?: "Ayudo con álgebra, geometría y cálculo básico")
                intent.putExtra("instructor_name", skill?.userName ?: "María García")
                intent.putExtra("instructor_avatar", skill?.userAvatar ?: "MG")
                intent.putExtra("skill_price", skill?.price?.toString() ?: "5.0")
                intent.putExtra("skill_category", skill?.category ?: "Matemáticas")
                intent.putExtra("skill_modalidad", skill?.modalidad ?: "Presencial")
                intent.putExtra("skill_rating", skill?.rating?.toString() ?: "0.0")
                intent.putExtra("skill_review_count", skill?.reviewCount?.toString() ?: "0")
                intent.putExtra("skill_id", skill?.id ?: "")
                startActivity(intent)
            }

            cardPython?.setOnClickListener {
                val skill = pythonSkill
                val intent = Intent(this, DetalleHabilidad::class.java)
                intent.putExtra("skill_title", skill?.title ?: "Python para principiantes")
                intent.putExtra("skill_description", skill?.description ?: "Enseño fundamentos de Python desde cero")
                intent.putExtra("instructor_name", skill?.userName ?: "Carlos López")
                intent.putExtra("instructor_avatar", skill?.userAvatar ?: "CL")
                intent.putExtra("skill_price", skill?.price?.toString() ?: "8.0")
                intent.putExtra("skill_category", skill?.category ?: "Programación")
                intent.putExtra("skill_modalidad", skill?.modalidad ?: "Virtual")
                intent.putExtra("skill_rating", skill?.rating?.toString() ?: "0.0")
                intent.putExtra("skill_review_count", skill?.reviewCount?.toString() ?: "0")
                intent.putExtra("skill_id", skill?.id ?: "")
                startActivity(intent)
            }

            cardIngles?.setOnClickListener {
                val skill = inglesSkill
                val intent = Intent(this, DetalleHabilidad::class.java)
                intent.putExtra("skill_title", skill?.title ?: "Inglés conversacional")
                intent.putExtra("skill_description", skill?.description ?: "Práctica de conversación en inglés")
                intent.putExtra("instructor_name", skill?.userName ?: "Ana Martínez")
                intent.putExtra("instructor_avatar", skill?.userAvatar ?: "AM")
                intent.putExtra("skill_price", skill?.price?.toString() ?: "6.0")
                intent.putExtra("skill_category", skill?.category ?: "Idiomas")
                intent.putExtra("skill_modalidad", skill?.modalidad ?: "Híbrida")
                intent.putExtra("skill_rating", skill?.rating?.toString() ?: "0.0")
                intent.putExtra("skill_review_count", skill?.reviewCount?.toString() ?: "0")
                intent.putExtra("skill_id", skill?.id ?: "")
                startActivity(intent)
            }
        } catch (e: Exception) {
            // Las cards originales no existen o están siendo reemplazadas dinámicamente
        }
    }

    private fun updateSkillsDisplay() {
        // Limpiar solo las cards dinámicas, no las originales
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

            // Ocultar también las cards originales cuando no hay resultados
            hideOriginalCards()
        } else {
            tvNoResultados.visibility = View.GONE
            scrollViewContent.visibility = View.VISIBLE

            // Mostrar/ocultar cards originales basado en el filtro
            manageOriginalCardsVisibility()

            // Crear cards dinámicas para habilidades que no están en las originales
            val originalSkillTitles = setOf("Matemáticas básicas", "Python para principiantes", "Inglés conversacional")

            filteredSkills.filter { skill ->
                !originalSkillTitles.contains(skill.title)
            }.forEach { skill ->
                val cardView = createSkillCard(skill)
                cardView.tag = "dynamic_card"
                skillsContainer.addView(cardView)
            }
        }
    }

    private fun hideOriginalCards() {
        try {
            findViewById<LinearLayout>(R.id.card_matematicas)?.visibility = View.GONE
            findViewById<LinearLayout>(R.id.card_python)?.visibility = View.GONE
            findViewById<LinearLayout>(R.id.card_ingles)?.visibility = View.GONE
        } catch (e: Exception) {
            // Las cards originales no existen
        }
    }

    private fun manageOriginalCardsVisibility() {
        val cardMatematicas = findViewById<LinearLayout>(R.id.card_matematicas)
        val cardPython = findViewById<LinearLayout>(R.id.card_python)
        val cardIngles = findViewById<LinearLayout>(R.id.card_ingles)

        // Mapear títulos de skills filtradas
        val filteredTitles = filteredSkills.map { it.title.lowercase() }

        // Mostrar/ocultar cada card original según si está en los resultados filtrados
        cardMatematicas?.visibility = if (filteredTitles.contains("matemáticas básicas")) View.VISIBLE else View.GONE
        cardPython?.visibility = if (filteredTitles.contains("python para principiantes")) View.VISIBLE else View.GONE
        cardIngles?.visibility = if (filteredTitles.contains("inglés conversacional")) View.VISIBLE else View.GONE
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
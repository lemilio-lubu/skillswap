package com.emilio.jacome.skillswap

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.model.Skill
import com.emilio.jacome.skillswap.utils.SkillRepository

class CrearHabilidad : AppCompatActivity() {

    private lateinit var btnAgregar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_habilidad)
        
        // Referencias a las vistas
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnAgregar = findViewById<Button>(R.id.btn_agregar) // Ahora asignamos a la propiedad de la clase
        val etTitulo = findViewById<EditText>(R.id.et_titulo)
        val etDescripcion = findViewById<EditText>(R.id.et_descripcion)
        val etPrecio = findViewById<EditText>(R.id.et_precio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerModalidad = findViewById<Spinner>(R.id.spinner_modalidad)

        // Configurar spinner de categorías
        val categorias = arrayOf("Matemáticas", "Programación", "Idiomas", "Diseño", "Música", "Deportes", "Otros")
        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategorias

        // Configurar spinner de modalidad
        val modalidades = arrayOf("Selecciona una modalidad", "Presencial", "Virtual")
        val adapterModalidad = ArrayAdapter(this, android.R.layout.simple_spinner_item, modalidades)
        adapterModalidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModalidad.adapter = adapterModalidad

        // Botón de regreso
        btnBack.setOnClickListener {
            finish()
        }
        
        // Botón agregar habilidad
        btnAgregar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precioText = etPrecio.text.toString().trim()
            val categoria = spinnerCategoria.selectedItem.toString()
            val modalidadIndex = spinnerModalidad.selectedItemPosition

            when {
                titulo.isEmpty() -> {
                    Toast.makeText(this, "Por favor ingresa un título", Toast.LENGTH_SHORT).show()
                }
                descripcion.isEmpty() -> {
                    Toast.makeText(this, "Por favor ingresa una descripción", Toast.LENGTH_SHORT).show()
                }
                precioText.isEmpty() -> {
                    Toast.makeText(this, "Por favor ingresa un precio", Toast.LENGTH_SHORT).show()
                }
                modalidadIndex == 0 -> {
                    Toast.makeText(this, "Por favor selecciona una modalidad", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    crearNuevaHabilidad(
                        titulo,
                        descripcion,
                        precioText.toDouble(),
                        categoria,
                        modalidades[modalidadIndex]
                    )
                }
            }
        }
    }

    private fun crearNuevaHabilidad(titulo: String, descripcion: String, precio: Double, categoria: String, modalidad: String) {
        // Obtener ID del usuario actual
        val userId = FirebaseManager.getCurrentUserId()
        val userName = FirebaseManager.getCurrentUserDisplayName() ?: "Usuario"

        if (userId == null) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear objeto Skill
        val skill = Skill(
            title = titulo,
            description = descripcion,
            category = categoria,
            price = precio,
            modalidad = modalidad,
            userId = userId,
            userName = userName
        )

        btnAgregar.isEnabled = false

        SkillRepository.createSkill(skill)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Habilidad creada exitosamente!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear habilidad: ${e.message}", Toast.LENGTH_LONG).show()
                btnAgregar.isEnabled = true
            }
    }
}
package com.emilio.jacome.skillswap

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditarHabilidad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_habilidad)
        
        // Referencias a las vistas
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnDelete = findViewById<ImageView>(R.id.btn_delete)
        val btnGuardar = findViewById<Button>(R.id.btn_guardar)
        val etTitulo = findViewById<EditText>(R.id.et_titulo)
        val etDescripcion = findViewById<EditText>(R.id.et_descripcion)
        val etPrecio = findViewById<EditText>(R.id.et_precio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        
        // Configurar spinner de categorías
        val categorias = arrayOf("Matemáticas", "Programación", "Idiomas", "Diseño", "Música", "Deportes", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
        
        // Obtener datos del intent y llenar los campos
        val skillTitle = intent.getStringExtra("skill_title") ?: ""
        val skillDescription = intent.getStringExtra("skill_description") ?: ""
        val skillCategory = intent.getStringExtra("skill_category") ?: "Programación"
        val skillPrice = intent.getStringExtra("skill_price") ?: "0"
        
        etTitulo.setText(skillTitle)
        etDescripcion.setText(skillDescription)
        etPrecio.setText(skillPrice)
        
        // Seleccionar la categoría actual en el spinner
        val categoryPosition = categorias.indexOf(skillCategory)
        if (categoryPosition >= 0) {
            spinnerCategoria.setSelection(categoryPosition)
        }
        
        // Botón de regreso
        btnBack.setOnClickListener {
            finish()
        }
        
        // Botón eliminar habilidad
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar Habilidad")
                .setMessage("¿Estás seguro de que quieres eliminar esta habilidad?")
                .setPositiveButton("Eliminar") { _, _ ->
                    Toast.makeText(this, "Habilidad eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        
        // Botón guardar cambios
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim()
            
            when {
                titulo.isEmpty() -> {
                    Toast.makeText(this, "Por favor ingresa un título", Toast.LENGTH_SHORT).show()
                }
                descripcion.isEmpty() -> {
                    Toast.makeText(this, "Por favor ingresa una descripción", Toast.LENGTH_SHORT).show()
                }
                precio.isEmpty() -> {
                    Toast.makeText(this, "Por favor ingresa un precio", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Simulación de guardado exitoso
                    Toast.makeText(this, "¡Cambios guardados exitosamente!", Toast.LENGTH_SHORT).show()
                    finish() // Regresa al perfil
                }
            }
        }
    }
}
package com.emilio.jacome.skillswap

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CrearHabilidad : AppCompatActivity() {    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_habilidad)
        
        // Referencias a las vistas
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnAgregar = findViewById<Button>(R.id.btn_agregar)
        val etTitulo = findViewById<EditText>(R.id.et_titulo)
        val etDescripcion = findViewById<EditText>(R.id.et_descripcion)
        val etPrecio = findViewById<EditText>(R.id.et_precio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        
        // Configurar spinner de categorías
        val categorias = arrayOf("Matemáticas", "Programación", "Idiomas", "Diseño", "Música", "Deportes", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
        
        // Botón de regreso
        btnBack.setOnClickListener {
            finish()
        }
        
        // Botón agregar habilidad
        btnAgregar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim()
            val categoria = spinnerCategoria.selectedItem.toString()
            
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
                    Toast.makeText(this, "¡Habilidad creada exitosamente!", Toast.LENGTH_SHORT).show()
                    finish() // Regresa al perfil
                }
            }
        }
    }
}
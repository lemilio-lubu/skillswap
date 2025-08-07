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
import com.emilio.jacome.skillswap.utils.Constants
import com.emilio.jacome.skillswap.utils.SkillRepository

class CrearHabilidad : AppCompatActivity() {

    private lateinit var btnAgregar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_habilidad)
        
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnAgregar = findViewById<Button>(R.id.btn_agregar) // Ahora asignamos a la propiedad de la clase
        val etTitulo = findViewById<EditText>(R.id.et_titulo)
        val etDescripcion = findViewById<EditText>(R.id.et_descripcion)
        val etPrecio = findViewById<EditText>(R.id.et_precio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerModalidad = findViewById<Spinner>(R.id.spinner_modalidad)

        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.Categories.LIST)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategorias

        val adapterModalidad = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.Modalities.LIST_WITH_DEFAULT)
        adapterModalidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModalidad.adapter = adapterModalidad

        btnBack.setOnClickListener {
            finish()
        }
        
        btnAgregar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precioText = etPrecio.text.toString().trim()
            val categoria = spinnerCategoria.selectedItem.toString()
            val modalidadIndex = spinnerModalidad.selectedItemPosition

            when {
                titulo.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_titulo_vacio), Toast.LENGTH_SHORT).show()
                }
                descripcion.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_descripcion_vacia), Toast.LENGTH_SHORT).show()
                }
                precioText.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_precio_vacio), Toast.LENGTH_SHORT).show()
                }
                modalidadIndex == 0 -> {
                    Toast.makeText(this, getString(R.string.error_modalidad_no_seleccionada), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    crearNuevaHabilidad(
                        titulo,
                        descripcion,
                        precioText.toDouble(),
                        categoria,
                        Constants.Modalities.LIST_WITH_DEFAULT[modalidadIndex]
                    )
                }
            }
        }
    }

    private fun crearNuevaHabilidad(titulo: String, descripcion: String, precio: Double, categoria: String, modalidad: String) {
        val userId = FirebaseManager.getCurrentUserId()
        val userName = FirebaseManager.getCurrentUserDisplayName() ?: "Usuario"

        if (userId == null) {
            Toast.makeText(this, getString(R.string.error_usuario_no_identificado), Toast.LENGTH_SHORT).show()
            return
        }

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
                Toast.makeText(this, getString(R.string.habilidad_creada_exito), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.error_crear_habilidad, e.message), Toast.LENGTH_LONG).show()
                btnAgregar.isEnabled = true
            }
    }
}
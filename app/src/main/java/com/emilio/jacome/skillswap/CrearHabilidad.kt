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
import com.emilio.jacome.skillswap.utils.UserProfileCache

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
        val etIncluye = findViewById<EditText>(R.id.et_incluye)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerModalidad = findViewById<Spinner>(R.id.spinner_modalidad)

        val adapterCategorias = ArrayAdapter(this, R.layout.custom_spinner_item, Constants.CATEGORIES)
        adapterCategorias.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategorias

        val adapterModalidad = ArrayAdapter(this, R.layout.custom_spinner_item, Constants.MODALITIES)
        adapterModalidad.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinnerModalidad.adapter = adapterModalidad

        btnBack.setOnClickListener {
            finish()
        }
        
        btnAgregar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precioText = etPrecio.text.toString().trim()
            val incluye = etIncluye.text.toString().trim()
            val categoria = spinnerCategoria.selectedItem.toString()
            val modalidad = spinnerModalidad.selectedItem.toString()

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
                incluye.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_incluye_vacio), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    crearNuevaHabilidad(
                        titulo,
                        descripcion,
                        precioText.toDouble(),
                        categoria,
                        modalidad,
                        incluye
                    )
                }
            }
        }
    }

    private fun crearNuevaHabilidad(titulo: String, descripcion: String, precio: Double,
                                   categoria: String, modalidad: String, incluye: String) {
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
            incluye = incluye,
            userId = userId,
            userName = userName
        )

        btnAgregar.isEnabled = false

        SkillRepository.createSkill(skill)
            .addOnSuccessListener {
                // Invalidar caché de skills para que se recarguen en el perfil
                UserProfileCache.invalidateSkillsCache()
                Toast.makeText(this, getString(R.string.habilidad_creada_exito), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.error_crear_habilidad, e.message), Toast.LENGTH_LONG).show()
                btnAgregar.isEnabled = true
            }
    }
}
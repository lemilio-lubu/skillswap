package com.emilio.jacome.skillswap

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.utils.Constants
import com.emilio.jacome.skillswap.utils.SkillRepository

class EditarHabilidad : AppCompatActivity() {

    private lateinit var btnGuardar: Button
    private lateinit var skillId: String
    private var skillRating: Double = 0.0
    private var skillReviewCount: Int = 0
    private var skillSesionesCompletadas: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_habilidad)
        
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnDelete = findViewById<ImageView>(R.id.btn_delete)
        btnGuardar = findViewById<Button>(R.id.btn_guardar)
        val etTitulo = findViewById<EditText>(R.id.et_titulo)
        val etDescripcion = findViewById<EditText>(R.id.et_descripcion)
        val etPrecio = findViewById<EditText>(R.id.et_precio)
        val etIncluye = findViewById<EditText>(R.id.et_incluye)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerModalidad = findViewById<Spinner>(R.id.spinner_modalidad)
        val tvRating = findViewById<TextView>(R.id.tv_rating)
        val tvSesionesCompletadas = findViewById<TextView>(R.id.tv_sesiones_completadas)

        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.CATEGORIES)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategorias

        val adapterModalidad = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.MODALITIES)
        adapterModalidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModalidad.adapter = adapterModalidad

        skillId = intent.getStringExtra("skill_id") ?: ""
        val skillTitle = intent.getStringExtra("skill_title") ?: ""
        val skillDescription = intent.getStringExtra("skill_description") ?: ""
        val skillCategory = intent.getStringExtra("skill_category") ?: "Programación"
        val skillPrice = intent.getStringExtra("skill_price") ?: "0"
        val skillModalidad = intent.getStringExtra("skill_modalidad") ?: Constants.MODALITIES[0]
        val skillIncluye = intent.getStringExtra("skill_incluye") ?: ""
        skillRating = intent.getDoubleExtra("skill_rating", 0.0)
        skillReviewCount = intent.getIntExtra("skill_review_count", 0)
        skillSesionesCompletadas = intent.getIntExtra("skill_sesiones_completadas", 0)

        etTitulo.setText(skillTitle)
        etDescripcion.setText(skillDescription)
        etPrecio.setText(skillPrice)
        etIncluye.setText(skillIncluye)

        val categoryPosition = Constants.CATEGORIES.indexOf(skillCategory)
        if (categoryPosition >= 0) {
            spinnerCategoria.setSelection(categoryPosition)
        }
        
        val modalityPosition = Constants.MODALITIES.indexOf(skillModalidad)
        if (modalityPosition >= 0) {
            spinnerModalidad.setSelection(modalityPosition)
        }

        if (skillReviewCount > 0) {
            tvRating.text = getString(R.string.rating_con_reviews, skillRating, skillReviewCount)
        } else {
            tvRating.text = getString(R.string.rating_sin_reviews)
        }

        tvSesionesCompletadas.text = getString(R.string.sesiones_completadas, skillSesionesCompletadas)

        btnBack.setOnClickListener {
            finish()
        }
        
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmar_eliminar_titulo))
                .setMessage(getString(R.string.confirmar_eliminar_mensaje))
                .setPositiveButton(getString(R.string.eliminar)) { _, _ ->
                    eliminarHabilidad()
                }
                .setNegativeButton(getString(R.string.cancelar), null)
                .show()
        }
        
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim()
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
                precio.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_precio_vacio), Toast.LENGTH_SHORT).show()
                }
                incluye.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_incluye_vacio), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    actualizarHabilidad(titulo, descripcion, precio.toDouble(), categoria, modalidad, incluye)
                }
            }
        }
    }

    private fun eliminarHabilidad() {
        if (skillId.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_skill_id_invalido), Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false

        SkillRepository.deleteSkill(skillId)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.habilidad_eliminada), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.error_eliminar_habilidad, e.message), Toast.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
            }
    }

    private fun actualizarHabilidad(titulo: String, descripcion: String, precio: Double,
                                   categoria: String, modalidad: String, incluye: String) {
        if (skillId.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_skill_id_invalido), Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false

        val updates = mapOf(
            "title" to titulo,
            "description" to descripcion,
            "price" to precio,
            "category" to categoria,
            "modalidad" to modalidad,
            "incluye" to incluye,
            "updatedAt" to System.currentTimeMillis()
        )

        SkillRepository.updateSkill(skillId, updates)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.cambios_guardados), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.error_actualizar_habilidad, e.message), Toast.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
            }
    }
}
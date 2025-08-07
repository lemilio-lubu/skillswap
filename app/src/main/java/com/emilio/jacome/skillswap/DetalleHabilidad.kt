package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.utils.UserRepository

class DetalleHabilidad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_habilidad)
        
        // Obtener referencias a las vistas
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnContactar = findViewById<Button>(R.id.btn_contactar)
        val tvAvatar = findViewById<TextView>(R.id.tv_avatar)
        val tvInstructorName = findViewById<TextView>(R.id.tv_instructor_name)
        val tvUniversity = findViewById<TextView>(R.id.tv_university)
        val tvRating = findViewById<TextView>(R.id.tv_rating)
        val tvSkillTitle = findViewById<TextView>(R.id.tv_skill_title)
        val tvSkillDescription = findViewById<TextView>(R.id.tv_skill_description)
        val tvPrice = findViewById<TextView>(R.id.tv_price)
        val tvModalidad = findViewById<TextView>(R.id.tv_modalidad)
        val tvIncluye = findViewById<TextView>(R.id.tv_incluye)

        // Obtener datos del intent
        val skillTitle = intent.getStringExtra("skill_title") ?: "Habilidad"
        val skillDescription = intent.getStringExtra("skill_description") ?: "Descripción no disponible"
        val skillPrice = intent.getStringExtra("skill_price") ?: "$0/hora"
        val skillModalidad = intent.getStringExtra("skill_modalidad") ?: "No especificada"
        val skillIncluye = intent.getStringExtra("skill_incluye") ?: "No especificado"
        val instructorName = intent.getStringExtra("instructor_name") ?: "Instructor"
        val instructorAvatar = intent.getStringExtra("instructor_avatar") ?: "IN"
        val skillRating = intent.getStringExtra("skill_rating")?.toDoubleOrNull() ?: 0.0
        val skillReviewCount = intent.getStringExtra("skill_review_count")?.toIntOrNull() ?: 0
        val instructorId = intent.getStringExtra("instructor_id") ?: ""

        // Establecer los datos en las vistas
        tvSkillTitle.text = skillTitle
        tvSkillDescription.text = skillDescription
        tvPrice.text = skillPrice
        tvModalidad.text = skillModalidad
        tvIncluye.text = skillIncluye
        tvInstructorName.text = instructorName
        tvAvatar.text = instructorAvatar
        
        // Formatear y mostrar el rating
        if (skillReviewCount > 0) {
            val formattedRating = String.format("%.1f", skillRating)
            tvRating.text = "$formattedRating ($skillReviewCount reseñas)"
        } else {
            tvRating.text = "Sin reseñas"
        }

        // Obtener información adicional del instructor si tenemos su ID
        if (instructorId.isNotEmpty()) {
            UserRepository.getUser(instructorId)
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val university = documentSnapshot.getString("university") ?: ""
                        if (university.isNotEmpty()) {
                            tvUniversity.text = university
                        } else {
                            tvUniversity.text = "Universidad no especificada"
                        }
                    }
                }
                .addOnFailureListener {
                    tvUniversity.text = "Universidad no disponible"
                }
        } else {
            tvUniversity.text = "Universidad no disponible"
        }

        // Botón de regreso
        btnBack.setOnClickListener {
            finish()
        }
        
        // Botón contactar - navegar al chat
        btnContactar.setOnClickListener {
            val intent = Intent(this, Chat::class.java)
            intent.putExtra("instructor_name", instructorName)
            intent.putExtra("skill_title", skillTitle)
            intent.putExtra("instructor_id", instructorId)
            startActivity(intent)
        }
    }
}
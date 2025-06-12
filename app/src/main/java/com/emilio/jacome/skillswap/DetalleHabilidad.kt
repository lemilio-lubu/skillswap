package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleHabilidad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_habilidad)
        
        // Obtener referencias a las vistas
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnContactar = findViewById<Button>(R.id.btn_contactar)
        val tvAvatar = findViewById<TextView>(R.id.tv_avatar)
        val tvInstructorName = findViewById<TextView>(R.id.tv_instructor_name)
        val tvSkillTitle = findViewById<TextView>(R.id.tv_skill_title)
        val tvSkillDescription = findViewById<TextView>(R.id.tv_skill_description)
        
        // Obtener datos del intent
        val skillTitle = intent.getStringExtra("skill_title") ?: "Habilidad"
        val skillDescription = intent.getStringExtra("skill_description") ?: "Descripción no disponible"
        val instructorName = intent.getStringExtra("instructor_name") ?: "Instructor"
        val instructorAvatar = intent.getStringExtra("instructor_avatar") ?: "IN"
        
        // Establecer los datos en las vistas
        tvSkillTitle.text = skillTitle
        tvSkillDescription.text = skillDescription
        tvInstructorName.text = instructorName
        tvAvatar.text = instructorAvatar
        
        // Botón de regreso
        btnBack.setOnClickListener {
            finish()
        }
        
        // Botón contactar - navegar al chat
        btnContactar.setOnClickListener {
            val intent = Intent(this, Chat::class.java)
            intent.putExtra("instructor_name", instructorName)
            intent.putExtra("skill_title", skillTitle)
            startActivity(intent)
        }
    }
}
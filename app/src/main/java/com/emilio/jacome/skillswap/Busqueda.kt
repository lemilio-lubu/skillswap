package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Busqueda : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_busqueda)
        
        // Botón de perfil en el header
        val btnPerfil = findViewById<TextView>(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }
        
        // Cards de habilidades - navegar a detalle
        val cardMatematicas = findViewById<LinearLayout>(R.id.card_matematicas)
        val cardPython = findViewById<LinearLayout>(R.id.card_python)
        val cardIngles = findViewById<LinearLayout>(R.id.card_ingles)
        
        cardMatematicas.setOnClickListener {
            val intent = Intent(this, DetalleHabilidad::class.java)
            intent.putExtra("skill_title", "Matemáticas básicas")
            intent.putExtra("skill_description", "Ayudo con álgebra, geometría y cálculo básico")
            intent.putExtra("instructor_name", "María García")
            intent.putExtra("instructor_avatar", "MG")
            startActivity(intent)
        }
        
        cardPython.setOnClickListener {
            val intent = Intent(this, DetalleHabilidad::class.java)
            intent.putExtra("skill_title", "Python para principiantes")
            intent.putExtra("skill_description", "Enseño fundamentos de Python desde cero")
            intent.putExtra("instructor_name", "Carlos López")
            intent.putExtra("instructor_avatar", "CL")
            startActivity(intent)
        }
        
        cardIngles.setOnClickListener {
            val intent = Intent(this, DetalleHabilidad::class.java)
            intent.putExtra("skill_title", "Inglés conversacional")
            intent.putExtra("skill_description", "Práctica de conversación en inglés")
            intent.putExtra("instructor_name", "Ana Martínez")
            intent.putExtra("instructor_avatar", "AM")
            startActivity(intent)
        }
    }
}
package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.utils.AuthenticationHelper

class Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_de_perfil)
        
        // Referencias a las vistas
        val btnBack = findViewById<TextView>(R.id.btn_back)
        val btnConfiguracion = findViewById<TextView>(R.id.btn_configuracion)
        val btnAgregarHabilidad = findViewById<Button>(R.id.btn_agregar_habilidad)
        val cardJavascript = findViewById<LinearLayout>(R.id.card_javascript)
        val cardDiseno = findViewById<LinearLayout>(R.id.card_diseno)
        
        // Botón de regreso - volver a búsqueda
        btnBack.setOnClickListener {
            finish()
        }
        
        // Botón de configuración - mostrar opciones
        btnConfiguracion.setOnClickListener {
            showConfigurationOptions()
        }
        
        // Botón agregar nueva habilidad
        btnAgregarHabilidad.setOnClickListener {
            startActivity(Intent(this, CrearHabilidad::class.java))
        }
        
        // Cards de habilidades existentes - navegar a editar
        cardJavascript.setOnClickListener {
            val intent = Intent(this, EditarHabilidad::class.java)
            intent.putExtra("skill_id", "javascript")
            intent.putExtra("skill_title", "JavaScript Avanzado")
            intent.putExtra("skill_description", "Enseño React, Node.js y desarrollo web moderno.")
            intent.putExtra("skill_category", "Programación")
            intent.putExtra("skill_price", "8")
            startActivity(intent)
        }
        
        cardDiseno.setOnClickListener {
            val intent = Intent(this, EditarHabilidad::class.java)
            intent.putExtra("skill_id", "diseno")
            intent.putExtra("skill_title", "Diseño UX/UI")
            intent.putExtra("skill_description", "Aprende diseño centrado en el usuario y herramientas como Figma.")
            intent.putExtra("skill_category", "Diseño")
            intent.putExtra("skill_price", "10")
            startActivity(intent)
        }
    }
    
    private fun showConfigurationOptions() {
        val options = arrayOf("Cerrar sesión")
        
        AlertDialog.Builder(this)
            .setTitle("Configuración")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showLogoutConfirmation()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun logout() {
        AuthenticationHelper.logout()
        
        // Navigate to login screen and clear the back stack
        val intent = Intent(this, Inicio::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
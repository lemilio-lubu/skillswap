package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Navegar automáticamente a la pantalla de inicio (login)
        startActivity(Intent(this, Inicio::class.java))
        finish()
    }
}
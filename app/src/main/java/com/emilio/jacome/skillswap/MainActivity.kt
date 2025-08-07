package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        FirebaseManager.initialize(this)
        
        if (FirebaseManager.isUserLoggedIn()) {
            startActivity(Intent(this, Busqueda::class.java))
        } else {
            startActivity(Intent(this, Inicio::class.java))
        }
        finish()
    }
}
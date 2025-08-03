package com.emilio.jacome.skillswap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Firebase
        FirebaseManager.initialize(this)
        
        // Check if user is already logged in
        if (FirebaseManager.isUserLoggedIn()) {
            // User is logged in, go to main app (Busqueda activity)
            startActivity(Intent(this, Busqueda::class.java))
        } else {
            // User not logged in, go to login screen
            startActivity(Intent(this, Inicio::class.java))
        }
        finish()
    }
}
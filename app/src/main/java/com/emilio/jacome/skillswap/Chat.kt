package com.emilio.jacome.skillswap

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.R.*

class Chat : AppCompatActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_chat)

        // Referencias a las vistas
        messagesContainer = findViewById(id.messages_container)
        messageInput = findViewById(id.et_message)
        sendButton = findViewById(id.btn_send)
        val btnBack = findViewById<ImageView>(id.btn_back)
        val tvInstructorName = findViewById<TextView>(id.tv_instructor_name)
        
        // Obtener datos del intent
        val instructorName = intent.getStringExtra("instructor_name") ?: "Instructor"
        val skillTitle = intent.getStringExtra("skill_title") ?: "Habilidad"
        
        // Establecer el nombre del instructor en el header
        tvInstructorName.text = instructorName
        
        // Botón de regreso
        btnBack.setOnClickListener {
            finish()
        }

        // Botón de enviar
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Cargar mensajes de ejemplo
        loadSampleMessages(instructorName, skillTitle)
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            addSentMessage(messageText, getCurrentTime())
            messageInput.text.clear()
        }
    }

    private fun addReceivedMessage(message: String, time: String) {
        val messageView = layoutInflater.inflate(layout.layout_message_received, messagesContainer, false)
        messageView.findViewById<TextView>(id.tv_message).text = message
        messageView.findViewById<TextView>(id.tv_time).text = time
        messagesContainer.addView(messageView)
        scrollToBottom()
    }

    private fun addSentMessage(message: String, time: String) {
        val messageView = layoutInflater.inflate(layout.layout_message_sent, messagesContainer, false)
        messageView.findViewById<TextView>(id.tv_message).text = message
        messageView.findViewById<TextView>(id.tv_time).text = time
        messagesContainer.addView(messageView)
        scrollToBottom()
    }    private fun loadSampleMessages(instructorName: String, skillTitle: String) {
        addReceivedMessage("¡Hola! Vi que necesitas ayuda con $skillTitle. ¿En qué tema específico te gustaría que te apoye?", "10:30 AM")
        addSentMessage("Hola $instructorName! Tengo dificultades con algunos conceptos básicos. ¿Podrías ayudarme?", "10:32 AM")
        addReceivedMessage("¡Por supuesto! Ese es mi fuerte. ¿Cuándo te gustaría programar una sesión?", "10:35 AM")
    }

    private fun getCurrentTime(): String {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    private fun scrollToBottom() {
        val scrollView = findViewById<ScrollView>(id.scroll_messages)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
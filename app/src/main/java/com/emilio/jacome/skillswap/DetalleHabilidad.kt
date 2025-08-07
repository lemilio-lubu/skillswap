package com.emilio.jacome.skillswap

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilio.jacome.skillswap.utils.SkillRepository
import com.emilio.jacome.skillswap.utils.UserRepository

class DetalleHabilidad : AppCompatActivity() {

    private lateinit var tvRating: TextView
    private lateinit var skillId: String
    private var userHasRated: Boolean = false
    private var userRating: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_habilidad)
        
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnContactar = findViewById<Button>(R.id.btn_contactar)
        val btnCalificar = findViewById<Button>(R.id.btn_calificar)
        val tvAvatar = findViewById<TextView>(R.id.tv_avatar)
        val tvInstructorName = findViewById<TextView>(R.id.tv_instructor_name)
        val tvUniversity = findViewById<TextView>(R.id.tv_university)
        tvRating = findViewById(R.id.tv_rating)
        val tvSkillTitle = findViewById<TextView>(R.id.tv_skill_title)
        val tvSkillDescription = findViewById<TextView>(R.id.tv_skill_description)
        val tvPrice = findViewById<TextView>(R.id.tv_price)
        val tvModalidad = findViewById<TextView>(R.id.tv_modalidad)
        val tvIncluye = findViewById<TextView>(R.id.tv_incluye)

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
        skillId = intent.getStringExtra("skill_id") ?: ""

        tvSkillTitle.text = skillTitle
        tvSkillDescription.text = skillDescription
        tvPrice.text = skillPrice
        tvModalidad.text = skillModalidad
        tvIncluye.text = skillIncluye
        tvInstructorName.text = instructorName
        tvAvatar.text = instructorAvatar
        
        updateRatingDisplay(skillRating, skillReviewCount)

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

        val currentUserId = FirebaseManager.getCurrentUserId()
        val isOwner = currentUserId == instructorId

        btnCalificar.visibility = if (isOwner) View.GONE else View.VISIBLE

        if (skillId.isNotEmpty() && currentUserId != null && !isOwner) {
            checkUserRating(currentUserId)
        }

        btnCalificar.setOnClickListener {
            if (currentUserId != null) {
                showRatingDialog(skillTitle, currentUserId)
            } else {
                Toast.makeText(this, getString(R.string.error_usuario_no_identificado), Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
        
        btnContactar.setOnClickListener {
            val intent = Intent(this, Chat::class.java)
            intent.putExtra("instructor_name", instructorName)
            intent.putExtra("skill_title", skillTitle)
            intent.putExtra("instructor_id", instructorId)
            startActivity(intent)
        }
    }

    private fun checkUserRating(userId: String) {
        SkillRepository.getUserRating(skillId, userId)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userHasRated = true
                    userRating = document.getDouble("rating")?.toFloat() ?: 0f

                    findViewById<Button>(R.id.btn_calificar).text = getString(R.string.modificar_calificacion)
                }
            }
    }

    private fun updateRatingDisplay(rating: Double, reviewCount: Int) {
        if (reviewCount > 0) {
            val formattedRating = String.format("%.1f", rating)
            tvRating.text = "$formattedRating ($reviewCount reseñas)"
        } else {
            tvRating.text = "Sin reseñas"
        }
    }

    private fun showRatingDialog(skillTitle: String, userId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rating)

        val tvSkillTitleDialog = dialog.findViewById<TextView>(R.id.tv_skill_title_dialog)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.rating_bar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btn_cancelar)
        val btnCalificar = dialog.findViewById<Button>(R.id.btn_calificar)

        tvSkillTitleDialog.text = skillTitle

        if (userHasRated) {
            ratingBar.rating = userRating
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnCalificar.setOnClickListener {
            val rating = ratingBar.rating

            if (rating <= 0) {
                Toast.makeText(this, getString(R.string.error_calificacion_vacia), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            submitRating(rating, userId)
        }

        dialog.show()
    }

    private fun submitRating(rating: Float, userId: String) {
        if (skillId.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_skill_id_invalido), Toast.LENGTH_SHORT).show()
            return
        }

        val btnCalificar = findViewById<Button>(R.id.btn_calificar)
        btnCalificar.isEnabled = false

        SkillRepository.rateSkill(skillId, userId, rating)
            .addOnSuccessListener {
                // Actualizar la UI
                SkillRepository.getSkill(skillId)
                    .addOnSuccessListener { document ->
                        document.toObject(com.emilio.jacome.skillswap.model.Skill::class.java)?.let { skill ->
                            updateRatingDisplay(skill.rating, skill.reviewCount)
                        }

                        userHasRated = true
                        userRating = rating
                        btnCalificar.text = getString(R.string.modificar_calificacion)
                        btnCalificar.isEnabled = true

                        Toast.makeText(this, getString(R.string.calificacion_enviada), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        btnCalificar.isEnabled = true
                        Toast.makeText(this, getString(R.string.error_obtener_habilidad), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                btnCalificar.isEnabled = true
                Toast.makeText(this, getString(R.string.error_enviar_calificacion, e.message), Toast.LENGTH_SHORT).show()
            }
    }
}
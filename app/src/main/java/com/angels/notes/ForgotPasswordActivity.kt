package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSendReset: MaterialButton
    private lateinit var tvBackToLogin: TextView
    private lateinit var btnBack: ImageButton
    private var fromChangePassword: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        fromChangePassword = intent.getBooleanExtra("FROM_CHANGE_PASSWORD", false)

        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        btnSendReset = findViewById(R.id.btnSendReset)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
        btnBack = findViewById(R.id.btnBack)

        if (fromChangePassword) {
            findViewById<TextView>(R.id.tvRemember).visibility = android.view.View.GONE
            tvBackToLogin.visibility = android.view.View.GONE
        }

        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (validateEmail(email)) {
                // Langsung pindah ke OTP, toast nanti di OtpActivity
                val intent = Intent(this, OtpActivity::class.java)
                intent.putExtra("EMAIL", email)
                intent.putExtra("FROM_CHANGE_PASSWORD", fromChangePassword)
                startActivity(intent)
                // TIDAK finish() di sini, biar user bisa back ke sini kalau perlu
            }
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            tilEmail.error = "Email cannot be empty"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Invalid email format"
            false
        } else {
            tilEmail.error = null
            true
        }
    }
}
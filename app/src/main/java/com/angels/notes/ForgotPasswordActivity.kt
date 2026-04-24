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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // cari id-nya dari layout dulu ya
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        btnSendReset = findViewById(R.id.btnSendReset)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (validateEmail(email)) {
                // TODO: kirim email reset password di sini
                Toast.makeText(this, "Link reset sudah dikirim ke email kamu!", Toast.LENGTH_SHORT).show()
                // abis submit kita balikkan ke login ya biar user bisa masuk pake pass baru
                tvBackToLogin.performClick()
            }
        }

        tvBackToLogin.setOnClickListener {
            // balik ke halaman login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }

        btnBack.setOnClickListener {
            // sama kayak tvBackToLogin, tinggal panggil aja
            tvBackToLogin.performClick()
        }
    }

    private fun validateEmail(email: String): Boolean {
        // mastiin email-nya gak kosong dan formatnya bener
        return if (email.isEmpty()) {
            tilEmail.error = "Email tidak boleh kosong"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Format email tidak valid"
            false
        } else {
            tilEmail.error = null
            true
        }
    }

    // ini biar pas pindah halaman ada efek fade yang halus
    override fun finish() {
        super.finish()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSendReset: MaterialButton
    private lateinit var tvBackToLogin: TextView
    private lateinit var btnBack: ImageButton
    private var fromChangePassword: Boolean = false
    private var defaultBtnText: CharSequence = "Send"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        fromChangePassword = intent.getBooleanExtra("FROM_CHANGE_PASSWORD", false)

        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        btnSendReset = findViewById(R.id.btnSendReset)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
        btnBack = findViewById(R.id.btnBack)
        defaultBtnText = btnSendReset.text

        if (fromChangePassword) {
            findViewById<TextView>(R.id.tvRemember).visibility = android.view.View.GONE
            tvBackToLogin.visibility = android.view.View.GONE
        }

        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (validateEmail(email)) {
                sendResetRequest(email)
            }
        }

        tvBackToLogin.setOnClickListener { finish() }
        btnBack.setOnClickListener { finish() }
    }

    private fun sendResetRequest(email: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService()
                    .forgotPassword(ForgotPasswordRequest(email))

                // Backend selalu balas "success" (biar email orang lain ga ketebak),
                // jadi langsung lanjut ke layar OTP.
                Toast.makeText(this@ForgotPasswordActivity, response.message, Toast.LENGTH_SHORT).show()

                if (response.status == "success") {
                    val intent = Intent(this@ForgotPasswordActivity, OtpActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    intent.putExtra("FLOW", "reset")
                    intent.putExtra("FROM_CHANGE_PASSWORD", fromChangePassword)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Gagal terhubung ke server: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnSendReset.isEnabled = !isLoading
        btnSendReset.text = if (isLoading) "Sending..." else defaultBtnText
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
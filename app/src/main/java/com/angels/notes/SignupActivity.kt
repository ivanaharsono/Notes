package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etName: android.widget.EditText
    private lateinit var etEmail: android.widget.EditText
    private lateinit var etPassword: android.widget.EditText
    private lateinit var etConfirmPassword: android.widget.EditText
    private lateinit var btnSignUp: MaterialButton
    private lateinit var tvLogin: TextView

    // Tambahan variabel pelindung internet biar ga error
    private var isConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            if (!isConnected) {
                Toast.makeText(this, "No internet connection. Cannot sign up.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil semua inputan text dari form
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Perbaikan: Masukin 4 data ke fungsi validasi sesuai syarat fungsinya
            if (validateInput(name, email, password, confirmPassword)) {
                lifecycleScope.launch {
                    try {
                        val response = ApiConfig.getApiService().registerUser(AuthRequest(name, email, password))

                        if (response.status == "success") {
                            Toast.makeText(this@SignupActivity, "Sign up successful! Please login.", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@SignupActivity, response.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SignupActivity, "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        tvLogin.setOnClickListener {
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
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            tilName.error = "Nama tidak boleh kosong"
            isValid = false
        } else {
            tilName.error = null
        }

        if (email.isEmpty()) {
            tilEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Format email tidak valid"
            isValid = false
        } else {
            tilEmail.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Password minimal 6 karakter"
            isValid = false
        } else {
            tilPassword.error = null
        }

        if (confirmPassword != password) {
            tilConfirmPassword.error = "Password tidak cocok"
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }

        return isValid
    }
}
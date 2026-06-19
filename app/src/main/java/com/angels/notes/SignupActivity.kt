package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

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
    private lateinit var btnGoogle: MaterialButton
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // hubungin variabel sama id yang ada di xml
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                // abis daftar langsung ke login aja
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

        // ini masih pajangan ya
        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-Up coming soon!", Toast.LENGTH_SHORT).show()
        }

        tvLogin.setOnClickListener {
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
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // cek dulu inputannya udah bener apa belum
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

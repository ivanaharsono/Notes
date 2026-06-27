package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private var isConnected = true
    private var defaultBtnText: CharSequence = "Sign Up"

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

        defaultBtnText = btnSignUp.text
    }

    private fun setupClickListeners() {

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilName.error = null
                tilName.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilEmail.error = null
                tilEmail.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilPassword.error = null
                tilPassword.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilConfirmPassword.error = null
                tilConfirmPassword.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSignUp.setOnClickListener {
            if (!isConnected) {
                Toast.makeText(this, "No internet connection. Cannot sign up.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                signUpToServer(name, email, password)
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

    // panggil api signup. backend kirim OTP ke email terus buka OtpActivity
    private fun signUpToServer(name: String, email: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService()
                    .signup(SignupRequest(name, email, password))

                if (response.status == "success") {
                    Toast.makeText(this@SignupActivity, response.message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignupActivity, OtpActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    intent.putExtra("FLOW", "signup")
                    startActivity(intent)
                } else {
                    // contoh: "Email sudah terdaftar!"
                    Toast.makeText(this@SignupActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@SignupActivity,
                    "Gagal terhubung ke server: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnSignUp.isEnabled = !isLoading
        btnSignUp.text = if (isLoading) "Please wait..." else defaultBtnText
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!.]).{8,}$")

        if (name.isEmpty()) {
            tilName.isErrorEnabled = true
            tilName.error = "Name cannot be empty"
            isValid = false
        } else {
            tilName.error = null
            tilName.isErrorEnabled = false
        }

        if (email.isEmpty()) {
            tilEmail.isErrorEnabled = true
            tilEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.isErrorEnabled = true
            tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            tilEmail.error = null
            tilEmail.isErrorEnabled = false
        }

        if (password.isEmpty()) {
            tilPassword.isErrorEnabled = true
            tilPassword.error = "Password cannot be empty"
            isValid = false
        } else if (!passwordRegex.matches(password)) {
            tilPassword.isErrorEnabled = true
            tilPassword.error = "Min. 8 characters with uppercase, lowercase, number & special character"
            isValid = false
        } else {
            tilPassword.error = null
            tilPassword.isErrorEnabled = false
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.isErrorEnabled = true
            tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            tilConfirmPassword.isErrorEnabled = true
            tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            tilConfirmPassword.error = null
            tilConfirmPassword.isErrorEnabled = false
        }

        return isValid
    }
}
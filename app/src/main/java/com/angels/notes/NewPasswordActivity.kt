package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private var fromChangePassword: Boolean = false
    private var email: String = ""
    private var otp: String = ""
    private var defaultBtnText: CharSequence = "Save"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        fromChangePassword = intent.getBooleanExtra("FROM_CHANGE_PASSWORD", false)
        email = intent.getStringExtra("EMAIL") ?: ""
        otp = intent.getStringExtra("OTP") ?: ""

        tilNewPassword = findViewById(R.id.tilNewPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        defaultBtnText = btnSave.text

        etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilNewPassword.error = null
                tilNewPassword.isErrorEnabled = false
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

        btnSave.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (validatePasswords(newPassword, confirmPassword)) {
                resetPasswordOnServer(newPassword)
            }
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    // panggil api reset password
    private fun resetPasswordOnServer(newPassword: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService()
                    .resetPassword(ResetPasswordRequest(email, otp, newPassword))

                if (response.status == "success") {
                    Toast.makeText(this@NewPasswordActivity, response.message, Toast.LENGTH_SHORT).show()
                    navigateAfterReset()
                } else {
                    // "OTP salah atau sudah kedaluwarsa."
                    Toast.makeText(this@NewPasswordActivity, response.message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@NewPasswordActivity,
                    "Gagal terhubung ke server: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun navigateAfterReset() {
        if (fromChangePassword) {
            // kalo user masih login, balik ke settings
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_SETTINGS", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
        btnSave.text = if (isLoading) "Please wait..." else defaultBtnText
    }

    private fun validatePasswords(new: String, confirm: String): Boolean {
        var isValid = true
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!.]).{8,}$")

        if (new.isEmpty()) {
            tilNewPassword.isErrorEnabled = true
            tilNewPassword.error = "Password cannot be empty"
            isValid = false
        } else if (!passwordRegex.matches(new)) {
            tilNewPassword.isErrorEnabled = true
            tilNewPassword.error = "Min. 8 characters with uppercase, lowercase, number & special character"
            isValid = false
        } else {
            tilNewPassword.error = null
            tilNewPassword.isErrorEnabled = false
        }

        if (confirm.isEmpty()) {
            tilConfirmPassword.isErrorEnabled = true
            tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (confirm != new) {
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
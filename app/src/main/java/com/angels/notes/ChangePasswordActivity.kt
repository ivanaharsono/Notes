package com.angels.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var tilOldPassword: TextInputLayout
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etOldPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private var defaultBtnText: CharSequence = "Save"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        tilOldPassword = findViewById(R.id.tilOldPassword)
        tilNewPassword = findViewById(R.id.tilNewPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        defaultBtnText = btnSave.text

        etOldPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilOldPassword.error = null
                tilOldPassword.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (validatePasswords(oldPassword, newPassword, confirmPassword)) {
                changePasswordOnServer(oldPassword, newPassword)
            }
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            intent.putExtra("FROM_CHANGE_PASSWORD", true)
            startActivity(intent)
        }
    }

    // 🔗 PANGGIL API CHANGE PASSWORD (email sesi + password lama + password baru)
    private fun changePasswordOnServer(oldPassword: String, newPassword: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", "") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(this, "Sesi tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService()
                    .changePassword(ChangePasswordRequest(email, oldPassword, newPassword))

                if (response.status == "success") {
                    Toast.makeText(this@ChangePasswordActivity, response.message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // contoh: "Password lama salah!"
                    tilOldPassword.isErrorEnabled = true
                    tilOldPassword.error = response.message
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChangePasswordActivity,
                    "Gagal terhubung ke server: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
        btnSave.text = if (isLoading) "Please wait..." else defaultBtnText
    }

    private fun validatePasswords(old: String, new: String, confirm: String): Boolean {
        var isValid = true
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!.]).{8,}$")

        if (old.isEmpty()) {
            tilOldPassword.isErrorEnabled = true
            tilOldPassword.error = "Old password cannot be empty"
            isValid = false
        } else {
            tilOldPassword.error = null
            tilOldPassword.isErrorEnabled = false
        }

        if (new.isEmpty()) {
            tilNewPassword.isErrorEnabled = true
            tilNewPassword.error = "Password cannot be empty"
            isValid = false
        } else if (!passwordRegex.matches(new)) {
            tilNewPassword.isErrorEnabled = true
            tilNewPassword.error = "Min. 8 characters with uppercase, lowercase, number & special character"
            isValid = false
        } else if (new == old) {
            tilNewPassword.isErrorEnabled = true
            tilNewPassword.error = "New password must be different from old password"
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
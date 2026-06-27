package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {

    private lateinit var otpBoxes: List<EditText>
    private lateinit var btnVerify: MaterialButton
    private var email: String = ""
    private var fromChangePassword: Boolean = false
    private var flow: String = "reset"
    private var defaultBtnText: CharSequence = "Verify"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        email = intent.getStringExtra("EMAIL") ?: ""
        fromChangePassword = intent.getBooleanExtra("FROM_CHANGE_PASSWORD", false)
        flow = intent.getStringExtra("FLOW") ?: "reset"

        otpBoxes = listOf(
            findViewById(R.id.etOtp1),
            findViewById(R.id.etOtp2),
            findViewById(R.id.etOtp3),
            findViewById(R.id.etOtp4),
            findViewById(R.id.etOtp5),
            findViewById(R.id.etOtp6)
        )
        btnVerify = findViewById(R.id.btnVerify)
        defaultBtnText = btnVerify.text

        setupOtpBoxes()

        btnVerify.setOnClickListener {
            val otp = otpBoxes.joinToString("") { it.text.toString() }
            if (otp.length < 6) {
                Toast.makeText(this, "Masukkan semua 6 digit kode OTP", Toast.LENGTH_SHORT).show()
            } else {
                if (flow == "signup") {
                    verifySignupOtp(otp)
                } else {
                    // OTP divalidasi nanti di langkah reset-password,
                    // jadi cukup teruskan kode-nya ke NewPasswordActivity.
                    goToNewPassword(otp)
                }
            }
        }

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        otpBoxes[0].requestFocus()
        otpBoxes.forEach { it.setTextColor(android.graphics.Color.BLACK) }
    }

    // cek OTP ke backend, kalau benar, maka akun aktif, lalu login
    private fun verifySignupOtp(otp: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService()
                    .verifyOtp(VerifyOtpRequest(email, otp))

                if (response.status == "success") {
                    Toast.makeText(this@OtpActivity, response.message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OtpActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // "OTP salah atau sudah kedaluwarsa."
                    Toast.makeText(this@OtpActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@OtpActivity,
                    "Gagal terhubung ke server: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun goToNewPassword(otp: String) {
        val intent = Intent(this, NewPasswordActivity::class.java)
        intent.putExtra("EMAIL", email)
        intent.putExtra("OTP", otp)
        intent.putExtra("FROM_CHANGE_PASSWORD", fromChangePassword)
        startActivity(intent)
    }

    private fun setLoading(isLoading: Boolean) {
        btnVerify.isEnabled = !isLoading
        btnVerify.text = if (isLoading) "Verifying..." else defaultBtnText
    }

    private fun setupOtpBoxes() {
        otpBoxes.forEachIndexed { index, editText ->

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        editText.isSelected = true
                        if (index < otpBoxes.size - 1) {
                            otpBoxes[index + 1].requestFocus()
                        }
                    } else {
                        editText.isSelected = false
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() &&
                    index > 0
                ) {
                    otpBoxes[index - 1].apply {
                        requestFocus()
                        text.clear()
                        isSelected = false
                    }
                    true
                } else false
            }
        }
    }
}
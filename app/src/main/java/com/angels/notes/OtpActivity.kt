package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OtpActivity : AppCompatActivity() {

    private lateinit var otpBoxes: List<EditText>
    private lateinit var btnVerify: MaterialButton
    private var email: String = ""
    private var fromChangePassword: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        email = intent.getStringExtra("EMAIL") ?: ""
        fromChangePassword = intent.getBooleanExtra("FROM_CHANGE_PASSWORD", false)

        otpBoxes = listOf(
            findViewById(R.id.etOtp1),
            findViewById(R.id.etOtp2),
            findViewById(R.id.etOtp3),
            findViewById(R.id.etOtp4),
            findViewById(R.id.etOtp5),
            findViewById(R.id.etOtp6)
        )
        btnVerify = findViewById(R.id.btnVerify)

        setupOtpBoxes()

        btnVerify.setOnClickListener {
            val otp = otpBoxes.joinToString("") { it.text.toString() }
            if (otp.length < 6) {
                Toast.makeText(this, "Masukkan semua 6 digit kode OTP", Toast.LENGTH_SHORT).show()
            } else {
                // 🚧 Bypass verifikasi sementara — nanti temen lo handle API-nya
                val intent = Intent(this, NewPasswordActivity::class.java)
                intent.putExtra("EMAIL", email)
                intent.putExtra("FROM_CHANGE_PASSWORD", fromChangePassword)
                startActivity(intent)
            }
        }

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Fokus ke kotak pertama otomatis
        otpBoxes[0].requestFocus()
        otpBoxes.forEach { it.setTextColor(android.graphics.Color.BLACK) }
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
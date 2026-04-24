package com.angels.notes

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { finish() }

        setupFaq()
        setupFeedback()
    }

    // ── FAQ: accordion (ketuk pertanyaan → tampilkan/sembunyikan jawaban) ──
    private fun setupFaq() {
        val faqPairs = listOf(
            Pair(R.id.faq1, R.id.faq1Answer),
            Pair(R.id.faq2, R.id.faq2Answer),
            Pair(R.id.faq3, R.id.faq3Answer),
            Pair(R.id.faq4, R.id.faq4Answer),
        )

        for ((containerId, answerId) in faqPairs) {
            val container: LinearLayout = findViewById(containerId)
            val answer: TextView        = findViewById(answerId)

            container.setOnClickListener {
                // Toggle visibility jawaban
                answer.visibility = if (answer.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
    }

    // ── Feedback: validasi input lalu kirim ──
    private fun setupFeedback() {
        val etFeedback: TextInputEditText = findViewById(R.id.etFeedback)

        findViewById<View>(R.id.btnSendFeedback).setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            if (feedback.isEmpty()) {
                etFeedback.error = "Please describe your feedback"
                return@setOnClickListener
            }

            // TODO: Kirim feedback ke server / email
            // Sementara tampilkan Toast konfirmasi
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
            etFeedback.text?.clear()
        }
    }
}
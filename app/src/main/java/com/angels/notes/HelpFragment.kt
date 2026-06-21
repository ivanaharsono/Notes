package com.angels.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupFaq(view)
        setupFeedback(view)
    }

    private fun setupFaq(view: View) {
        val faqPairs = listOf(
            Pair(R.id.faq1, R.id.faq1Answer),
            Pair(R.id.faq2, R.id.faq2Answer),
            Pair(R.id.faq3, R.id.faq3Answer),
            Pair(R.id.faq4, R.id.faq4Answer)
        )

        for ((containerId, answerId) in faqPairs) {
            val container: LinearLayout = view.findViewById(containerId)
            val answer: TextView = view.findViewById(answerId)

            container.setOnClickListener {
                answer.visibility =
                    if (answer.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupFeedback(view: View) {
        val etFeedback: TextInputEditText = view.findViewById(R.id.etFeedback)

        view.findViewById<View>(R.id.btnSendFeedback).setOnClickListener {
            val feedback = etFeedback.text.toString().trim()

            if (feedback.isEmpty()) {
                etFeedback.error = "Please describe your feedback"
                return@setOnClickListener
            }

            Toast.makeText(
                requireContext(),
                "Thank you for your feedback!",
                Toast.LENGTH_LONG
            ).show()

            etFeedback.text?.clear()
        }
    }
}
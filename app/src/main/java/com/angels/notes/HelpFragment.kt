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
import android.content.Context
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
        val btnSendFeedback: View = view.findViewById(R.id.btnSendFeedback)

        btnSendFeedback.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()

            if (feedback.isEmpty()) {
                etFeedback.error = "Please describe your feedback"
                return@setOnClickListener
            }

            // Ambil email user yang lagi login dari session
            val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val email = sharedPref.getString("USER_EMAIL", "") ?: ""

            // Tembak ke API Backend
            btnSendFeedback.isEnabled = false   // loading: cegah double-tap
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = ApiConfig.getApiService()
                        .sendFeedback(FeedbackRequest(email, feedback))

                    if (response.status == "success") {
                        Toast.makeText(
                            requireContext(),
                            "Thank you! Feedback sent successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                        etFeedback.text?.clear()
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to send feedback: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    btnSendFeedback.isEnabled = true
                }
            }
        }
    }
}
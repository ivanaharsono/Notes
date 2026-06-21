package com.angels.notes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupProfile(view)
        setupChangePassword(view)
        setupDarkMode(view)
        setupNotifications(view)
        setupSignOut(view)
    }

    private fun setupProfile(view: View) {
        view.findViewById<View>(R.id.itemProfile).setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_profile)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialog.findViewById<ImageButton>(R.id.btnCloseProfile)
                .setOnClickListener {
                    dialog.dismiss()
                }

            dialog.show()
        }
    }

    private fun setupChangePassword(view: View) {
        view.findViewById<View>(R.id.itemChangePassword).setOnClickListener {
            startActivity(Intent(requireContext(), ForgotPasswordActivity::class.java))
        }
    }

    private fun setupDarkMode(view: View) {
        val switchDark: SwitchCompat = view.findViewById(R.id.switchDarkMode)

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                requireContext(),
                if (isChecked) "Dark mode enabled" else "Dark mode disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupNotifications(view: View) {
        val switchNotif: SwitchCompat = view.findViewById(R.id.switchNotif)

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupSignOut(view: View) {
        view.findViewById<View>(R.id.btnSignOut).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    val sharedPref = requireContext().getSharedPreferences(
                        "UserSession",
                        Context.MODE_PRIVATE
                    )

                    sharedPref.edit().clear().apply()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
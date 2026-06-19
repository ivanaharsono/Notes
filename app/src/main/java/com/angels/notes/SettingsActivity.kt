package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { finish() }

        // ── Account ──────────────────────────────────────────
        // Item Profile → buka dialog profil yang sama dengan di MainActivity
        findViewById<android.view.View>(R.id.itemProfile).setOnClickListener {
            val dialog = android.app.Dialog(this)
            dialog.setContentView(R.layout.dialog_profile)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.findViewById<android.widget.ImageButton>(R.id.btnCloseProfile)
                .setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        // Item Change Password → buka ForgotPasswordActivity yang sudah ada
        findViewById<android.view.View>(R.id.itemChangePassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // ── Display ──────────────────────────────────────────
        // Switch Dark Mode (saat ini hanya visual; implementasi penuh perlu AppCompatDelegate)
        val switchDark: SwitchCompat = findViewById(R.id.switchDarkMode)
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Dark mode enabled" else "Dark mode disabled",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: AppCompatDelegate.setDefaultNightMode(
            //           if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
            //           else AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Switch Notifications
        val switchNotif: SwitchCompat = findViewById(R.id.switchNotif)
        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ── About ─────────────────────────────────────────────
        // Tombol Sign Out → konfirmasi lalu kembali ke LoginActivity
        findViewById<android.view.View>(R.id.btnSignOut).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    // Kembali ke LoginActivity dan bersihkan back stack
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
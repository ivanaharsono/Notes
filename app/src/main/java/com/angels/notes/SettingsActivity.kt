package com.angels.notes

import android.content.Context
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

        findViewById<android.view.View>(R.id.itemProfile).setOnClickListener {
            val dialog = android.app.Dialog(this)
            dialog.setContentView(R.layout.dialog_profile)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.findViewById<android.widget.ImageButton>(R.id.btnCloseProfile)
                .setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        findViewById<android.view.View>(R.id.itemChangePassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        val switchDark: SwitchCompat = findViewById(R.id.switchDarkMode)
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Dark mode enabled" else "Dark mode disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        val switchNotif: SwitchCompat = findViewById(R.id.switchNotif)
        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<android.view.View>(R.id.btnSignOut).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    
                    val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
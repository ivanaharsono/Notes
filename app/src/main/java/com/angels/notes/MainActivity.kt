package com.angels.notes

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        topAppBar = findViewById(R.id.topAppBar)
        navView = findViewById(R.id.navView)

        setupToolbar()
        setupNavigationDrawer()

        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
            topAppBar.title = "My Notes"
            navView.setCheckedItem(R.id.nav_dashboard)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupToolbar() {
        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile -> {
                    showProfileDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigationDrawer() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    topAppBar.title = "My Notes"
                    replaceFragment(DashboardFragment())
                }

                R.id.nav_arsip -> {
                    topAppBar.title = "Archive"
                    replaceFragment(ArchiveFragment())
                }

                R.id.nav_trash -> {
                    topAppBar.title = "Trash"
                    replaceFragment(TrashFragment())
                }

                R.id.nav_label -> {
                    topAppBar.title = "Labels"
                    replaceFragment(LabelsFragment())
                }

                R.id.nav_settings -> {
                    topAppBar.title = "Settings"
                    replaceFragment(SettingsFragment())
                }

                R.id.nav_help -> {
                    topAppBar.title = "Help & Feedback"
                    replaceFragment(HelpFragment())
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_profile)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnCloseProfile)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
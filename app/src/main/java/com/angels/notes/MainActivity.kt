package com.angels.notes

import android.app.Dialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import android.view.View
import android.view.WindowInsetsController

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var navView: NavigationView

    private val batteryReceiver = BatteryLevelReceiver()
    private var activeProfileImageView: ImageView? = null

    private val pickProfilePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val savedPath = saveProfilePhoto(uri)
                if (savedPath != null) {
                    getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        .edit()
                        .putString("PROFILE_PHOTO_PATH", savedPath)
                        .apply()

                    activeProfileImageView?.imageTintList = null
                    activeProfileImageView?.setImageURI(Uri.fromFile(File(savedPath)))
                    activeProfileImageView?.setPadding(0, 0, 0, 0)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.IndigoBlue)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,  // 0 = icon putih (bukan light)
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        topAppBar = findViewById(R.id.topAppBar)
        navView = findViewById(R.id.navView)

        setupToolbar()
        setupNavigationDrawer()

        topAppBar.post {
            val menuView = topAppBar.getChildAt(topAppBar.childCount - 1)
            menuView?.setPadding(0, 0, dp(8), 0)  // 8dp dari kanan, ubah sesuai selera
        }

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

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(Intent.ACTION_BATTERY_LOW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(batteryReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(batteryReceiver)
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
                    openPage(R.id.nav_dashboard, "My Notes", DashboardFragment())
                }

                R.id.nav_arsip -> {
                    openPage(R.id.nav_arsip, "Archive", ArchiveFragment())
                }

                R.id.nav_trash -> {
                    openPage(R.id.nav_trash, "Trash", TrashFragment())
                }

                R.id.nav_label -> {
                    openPage(R.id.nav_label, "Labels", LabelsFragment())
                }

                R.id.nav_settings -> {
                    openPage(R.id.nav_settings, "Settings", SettingsFragment())
                }

                R.id.nav_help -> {
                    openPage(R.id.nav_help, "Help & Feedback", HelpFragment())
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

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", "Email belum tersedia")
        val photoPath = sharedPref.getString("PROFILE_PHOTO_PATH", null)

        val imgProfile = dialog.findViewById<ImageView>(R.id.imgProfilePhoto)
        val tvEmail = dialog.findViewById<TextView>(R.id.tvDialogEmail)
        val tvChangePhoto = dialog.findViewById<TextView>(R.id.tvChangePhoto)
        val tvRemovePhoto = dialog.findViewById<TextView>(R.id.tvRemovePhoto)

        tvEmail.text = email

        if (!photoPath.isNullOrEmpty() && File(photoPath).exists()) {
            imgProfile.imageTintList = null
            imgProfile.setImageURI(Uri.fromFile(File(photoPath)))
            imgProfile.setPadding(0, 0, 0, 0)
        } else {
            imgProfile.setImageResource(R.drawable.ic_person)
            imgProfile.imageTintList = ColorStateList.valueOf(getColor(android.R.color.white))
            imgProfile.setPadding(20, 20, 20, 20)
        }

        tvRemovePhoto.setOnClickListener {
            val savedPhoto = sharedPref.getString("PROFILE_PHOTO_PATH", null)

            if (!savedPhoto.isNullOrEmpty()) {
                File(savedPhoto).delete()
            }

            sharedPref.edit()
                .remove("PROFILE_PHOTO_PATH")
                .apply()

            imgProfile.setImageResource(R.drawable.ic_person)
            imgProfile.imageTintList = ColorStateList.valueOf(getColor(android.R.color.white))
            imgProfile.setPadding(20, 20, 20, 20)
        }

        tvChangePhoto.setOnClickListener {
            activeProfileImageView = imgProfile
            pickProfilePhotoLauncher.launch("image/*")
        }

        dialog.findViewById<ImageButton>(R.id.btnCloseProfile).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveProfilePhoto(uri: Uri): String? {
        return try {
            val profileDir = File(filesDir, "profile")
            if (!profileDir.exists()) profileDir.mkdirs()

            val outputFile = File(profileDir, "profile_photo.jpg")

            contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            outputFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun openPage(menuItemId: Int, title: String, fragment: Fragment) {
        topAppBar.title = title
        navView.setCheckedItem(menuItemId)
        replaceFragment(fragment)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
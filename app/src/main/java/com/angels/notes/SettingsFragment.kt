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
import android.widget.TextView
import android.net.Uri
import android.widget.ImageView
import java.io.File
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatDelegate

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showUserEmail(view)
        setupProfile(view)
        setupChangePassword(view)
        setupNoteLayout(view)
        setupNotifications(view)
        setupSignOut(view)
    }

    private fun setupProfile(view: View) {
        view.findViewById<View>(R.id.itemProfile).setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_profile)
            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val email = sharedPref.getString("USER_EMAIL", "Email belum tersedia")
            val photoPath = sharedPref.getString("PROFILE_PHOTO_PATH", null)

            dialog.findViewById<TextView>(R.id.tvDialogEmail).text = email

            val imgProfile = dialog.findViewById<ImageView>(R.id.imgProfilePhoto)
            if (!photoPath.isNullOrEmpty() && File(photoPath).exists()) {
                imgProfile.imageTintList = null
                imgProfile.setImageURI(Uri.fromFile(File(photoPath)))
                imgProfile.setPadding(0, 0, 0, 0)
            }
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
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }
    }

    private fun setupNoteLayout(view: View) {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val tvLayoutSub = view.findViewById<TextView>(R.id.tvLayoutSub)

        tvLayoutSub.text = sharedPref.getString("NOTE_LAYOUT", "Grid")

        view.findViewById<View>(R.id.itemNoteLayoutContainer).setOnClickListener {
            val options = arrayOf("Grid", "List")
            val current = sharedPref.getString("NOTE_LAYOUT", "Grid")
            var checked = if (current == "List") 1 else 0

            val dialogBuilder = AlertDialog.Builder(requireContext())
                .setTitle("Note Layout")

            val adapter = object : android.widget.ArrayAdapter<String>(
                requireContext(),
                R.layout.item_note_layout_option,
                R.id.text1,
                options
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val rowView = convertView ?: LayoutInflater.from(context)
                        .inflate(R.layout.item_note_layout_option, parent, false)

                    val radioButton = rowView.findViewById<android.widget.RadioButton>(R.id.radioButton)
                    val text = rowView.findViewById<TextView>(R.id.text1)

                    text.text = options[position]
                    radioButton.isChecked = position == checked

                    return rowView
                }
            }

            val dialog = dialogBuilder
                .setAdapter(adapter) { d, which ->
                    val selected = options[which]
                    sharedPref.edit().putString("NOTE_LAYOUT", selected).apply()
                    tvLayoutSub.text = selected
                    d.dismiss()
                }
                .create()

            dialog.show()
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

                    sharedPref.edit()
                        .remove("IS_LOGGED_IN")
                        .remove("USER_EMAIL")
                        .apply()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showUserEmail(view: View) {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", "Email belum tersedia")

        view.findViewById<TextView>(R.id.tvProfileSub).text = email
    }
}
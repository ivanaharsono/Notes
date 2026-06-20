package com.angels.notes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import java.util.Locale
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    private lateinit var rvNotes: RecyclerView
    private lateinit var fabAdd: android.widget.ImageButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var navView: NavigationView
    private lateinit var etSearch: EditText
    private lateinit var noteAdapter: NoteAdapter
    private val list = ArrayList<Note>()

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvNotes      = findViewById(R.id.rvNotes)
        fabAdd       = findViewById(R.id.fabAddNote)
        drawerLayout = findViewById(R.id.drawerLayout)
        topAppBar    = findViewById(R.id.topAppBar)
        navView      = findViewById(R.id.navView)
        etSearch     = findViewById(R.id.etSearch)

        dbHelper = DatabaseHelper(this)

        loadNotesFromDB()
        showNotesGrid()
        setupFab()
        setupToolbar()
        setupNavigationDrawer()
        setupSearch()

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

    override fun onResume() {
        super.onResume()
        loadNotesFromDB()
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            val intent = android.content.Intent(this, DetailActivity::class.java)
            startActivity(intent)
        }
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
        navView.setCheckedItem(R.id.nav_dashboard)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    topAppBar.title = "My Notes"
                    showAllNotes()
                }
                R.id.nav_arsip -> {
                    val intent = android.content.Intent(this, ArchiveActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_trash -> {
                    val intent = android.content.Intent(this, TrashActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_label -> {
                    val intent = android.content.Intent(this, LabelsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_settings -> {
                    val intent = android.content.Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_help -> {
                    val intent = android.content.Intent(this, HelpActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })
    }

    private fun showProfileDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_profile)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<android.widget.ImageButton>(R.id.btnCloseProfile)
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun loadNotesFromDB() {
        list.clear()
        val dataDariDatabase = dbHelper.getAllNotes()
        list.addAll(dataDariDatabase)
        
        if (::noteAdapter.isInitialized) {
            noteAdapter.filterList(list)
        }
    }

    private fun showNotesGrid() {
        rvNotes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        
        noteAdapter = NoteAdapter(list) { selectedNote ->
            val intent = android.content.Intent(this@MainActivity, DetailActivity::class.java)

            intent.putExtra("EXTRA_ID", selectedNote.id)
            intent.putExtra("EXTRA_JUDUL", selectedNote.judul)
            intent.putExtra("EXTRA_ISI", selectedNote.isi)
            intent.putExtra("EXTRA_TANGGAL", selectedNote.tanggal)
            intent.putExtra("EXTRA_IS_ARCHIVED", selectedNote.isArchived)
            intent.putExtra("EXTRA_IS_TRASHED", selectedNote.isTrashed)

            startActivity(intent)
        }

        rvNotes.adapter = noteAdapter
    }

    private fun showAllNotes() {
        noteAdapter.filterList(list)
    }

    private fun filter(text: String) {
        val filteredList = ArrayList<Note>()
        for (item in list) {
            if (item.judul.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault())) ||
                item.isi.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        noteAdapter.filterList(filteredList)
    }
}
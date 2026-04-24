package com.angels.notes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvNotes      = findViewById(R.id.rvNotes)
        fabAdd       = findViewById(R.id.fabAddNote)
        drawerLayout = findViewById(R.id.drawerLayout)
        topAppBar    = findViewById(R.id.topAppBar)
        navView      = findViewById(R.id.navView)
        etSearch     = findViewById(R.id.etSearch)

        prepareDummyData()
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

    // ─────────────────────────────────────────────
    // FAB: buka DetailActivity untuk membuat catatan baru
    // ─────────────────────────────────────────────
    private fun setupFab() {
        fabAdd.setOnClickListener {
            val intent = android.content.Intent(this, DetailActivity::class.java)
            startActivity(intent)
        }
    }

    // ─────────────────────────────────────────────
    // TOOLBAR: hamburger → buka drawer | profile icon → dialog
    // ─────────────────────────────────────────────
    private fun setupToolbar() {
        // Ketuk ikon hamburger (navigationIcon) → buka sidebar dari kiri
        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Ketuk ikon profil di menu toolbar → tampilkan dialog profil
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

    // ─────────────────────────────────────────────
    // NAVIGATION DRAWER (SIDEBAR):
    //   Menangani klik setiap item menu di sidebar.
    //   Sidebar ditutup otomatis setelah item dipilih.
    //
    //   Item yang tersedia:
    //   - nav_dashboard : kembali ke halaman utama (daftar catatan)
    //   - nav_arsip     : membuka halaman arsip (catatan yang diarsipkan)
    //   - nav_trash     : membuka halaman sampah (catatan yang dihapus)
    //   - nav_label     : membuka manajemen label / kategori catatan
    //   - nav_settings  : membuka pengaturan aplikasi
    //   - nav_help      : membuka halaman bantuan & umpan balik
    // ─────────────────────────────────────────────
    private fun setupNavigationDrawer() {
        // Tandai "My Notes" sebagai item yang sedang aktif saat pertama dibuka
        navView.setCheckedItem(R.id.nav_dashboard)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_dashboard -> {
                    // Sudah di halaman utama — tidak perlu navigasi ulang
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

            // Tutup sidebar setelah item dipilih
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }


    // ─────────────────────────────────────────────
    // SEARCH: filter catatan berdasarkan judul atau isi
    // ─────────────────────────────────────────────
    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })
    }

    // ─────────────────────────────────────────────
    // DIALOG PROFIL
    // ─────────────────────────────────────────────
    private fun showProfileDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_profile)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<android.widget.ImageButton>(R.id.btnCloseProfile)
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ─────────────────────────────────────────────
    // DATA & GRID
    // ─────────────────────────────────────────────
    private fun prepareDummyData() {
        list.clear()
        list.add(Note(1, "Belanja Bulanan",  "Beli telur, susu, roti, kopi, dan sabun cuci.", "24 April"))
        list.add(Note(2, "Olahraga Pagi",    "Jogging 30 menit keliling taman jam 6 pagi.",   "25 April"))
        list.add(Note(3, "Baca Buku",        "Selesaikan membaca bab 4 buku fiksi yang baru dibeli.", "25 April"))
        list.add(Note(4, "Telepon Keluarga", "Tanya kabar orang tua dan obrolin rencana liburan akhir pekan.", "26 April"))
        list.add(Note(5, "Bersih-bersih",   "Rapikan meja kamar dan cuci sepatu sneakers sebelum hari Senin.", "27 April"))
        list.add(Note(6, "Servis Motor",     "Ganti oli dan cek tekanan ban di bengkel langganan.", "28 April"))
    }

    private fun showNotesGrid() {
        rvNotes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        noteAdapter = NoteAdapter(list)
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
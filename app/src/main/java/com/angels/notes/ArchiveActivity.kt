package com.angels.notes

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.MaterialToolbar

class ArchiveActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvArchive: androidx.recyclerview.widget.RecyclerView
    private lateinit var layoutEmpty: android.widget.LinearLayout

    // Dummy data catatan yang diarsipkan
    // Pada implementasi nyata, data ini diambil dari database/repository
    private val archivedNotes = arrayListOf(
        Note(101, "Resep Kue Lebaran", "Tepung 500g, gula 200g, mentega 250g, telur 3 butir, vanili secukupnya.", "10 Maret"),
        Note(102, "Lirik Lagu Favorit", "Ketika waktu terasa berhenti, dan dunia seakan tak berputar lagi...", "2 Februari"),
        Note(103, "Catatan Kuliah Semester 3", "Algoritma dan Struktur Data: Binary Tree, Graph, Dynamic Programming.", "15 Januari")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        toolbar     = findViewById(R.id.topAppBar)
        rvArchive   = findViewById(R.id.rvArchive)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        // Tombol back untuk kembali ke MainActivity
        toolbar.setNavigationOnClickListener { finish() }

        showArchivedNotes()
    }

    private fun showArchivedNotes() {
        if (archivedNotes.isEmpty()) {
            // Tampilkan empty state jika tidak ada catatan
            layoutEmpty.visibility = View.VISIBLE
            rvArchive.visibility   = View.GONE
        } else {
            // Tampilkan daftar catatan yang diarsipkan
            layoutEmpty.visibility = View.GONE
            rvArchive.visibility   = View.VISIBLE

            rvArchive.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            rvArchive.adapter       = NoteAdapter(archivedNotes)
        }
    }
}
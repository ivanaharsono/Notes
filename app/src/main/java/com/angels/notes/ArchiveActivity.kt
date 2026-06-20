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
    private lateinit var noteAdapter: NoteAdapter
    private val archivedNotes = ArrayList<Note>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        dbHelper = DatabaseHelper(this)

        toolbar     = findViewById(R.id.topAppBar)
        rvArchive   = findViewById(R.id.rvArchive)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        toolbar.setNavigationOnClickListener { finish() }

        showArchivedNotes()
    }

    override fun onResume() {
        super.onResume()
        loadArchivedFromDB()
    }

    private fun loadArchivedFromDB() {
        archivedNotes.clear()
        archivedNotes.addAll(dbHelper.getArchivedNotes())
        
        if (archivedNotes.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvArchive.visibility   = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvArchive.visibility   = View.VISIBLE
            if (::noteAdapter.isInitialized) {
                noteAdapter.filterList(archivedNotes)
            }
        }
    }

    private fun showArchivedNotes() {
        rvArchive.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        
        noteAdapter = NoteAdapter(archivedNotes) { selectedNote ->
            val intent = android.content.Intent(this@ArchiveActivity, DetailActivity::class.java)
            
            intent.putExtra("EXTRA_ID", selectedNote.id)
            intent.putExtra("EXTRA_JUDUL", selectedNote.judul)
            intent.putExtra("EXTRA_ISI", selectedNote.isi)
            intent.putExtra("EXTRA_TANGGAL", selectedNote.tanggal)
            intent.putExtra("EXTRA_IS_ARCHIVED", selectedNote.isArchived)
            intent.putExtra("EXTRA_IS_TRASHED", selectedNote.isTrashed)
            
            startActivity(intent)
        }
        rvArchive.adapter = noteAdapter
        loadArchivedFromDB()
    }
}
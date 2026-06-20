package com.angels.notes

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.MaterialToolbar

class TrashActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvTrash: androidx.recyclerview.widget.RecyclerView
    private lateinit var layoutEmpty: android.widget.LinearLayout
    private lateinit var trashAdapter: NoteAdapter
    private val trashedNotes = ArrayList<Note>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        dbHelper = DatabaseHelper(this)

        toolbar     = findViewById(R.id.topAppBar)
        rvTrash     = findViewById(R.id.rvTrash)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        toolbar.setNavigationOnClickListener { finish() }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_empty_trash -> {
                    confirmEmptyTrash()
                    true
                }
                else -> false
            }
        }

        showTrashedNotes()
    }

    override fun onResume() {
        super.onResume()
        loadTrashFromDB()
    }

    private fun loadTrashFromDB() {
        trashedNotes.clear()
        trashedNotes.addAll(dbHelper.getTrashedNotes())

        if (trashedNotes.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvTrash.visibility     = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvTrash.visibility     = View.VISIBLE
            if (::trashAdapter.isInitialized) {
                trashAdapter.filterList(trashedNotes)
            }
        }
    }

    private fun showTrashedNotes() {
        rvTrash.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        
        trashAdapter = NoteAdapter(trashedNotes) { selectedNote ->
            val intent = android.content.Intent(this@TrashActivity, DetailActivity::class.java)

            intent.putExtra("EXTRA_ID", selectedNote.id)
            intent.putExtra("EXTRA_JUDUL", selectedNote.judul)
            intent.putExtra("EXTRA_ISI", selectedNote.isi)
            intent.putExtra("EXTRA_TANGGAL", selectedNote.tanggal)
            intent.putExtra("EXTRA_IS_ARCHIVED", selectedNote.isArchived)
            intent.putExtra("EXTRA_IS_TRASHED", selectedNote.isTrashed)

            startActivity(intent)
        }
        rvTrash.adapter = trashAdapter
        loadTrashFromDB()
    }

    private fun confirmEmptyTrash() {
        AlertDialog.Builder(this)
            .setTitle("Empty Trash?")
            .setMessage("All notes in Trash will be permanently deleted. This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                dbHelper.emptyTrash()
                loadTrashFromDB()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
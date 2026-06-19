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

    // Dummy data catatan yang sudah dihapus
    // Pada implementasi nyata, data ini diambil dari database dengan flag isDeleted = true
    private val trashedNotes = arrayListOf(
        Note(201, "Draft Lama", "Ini adalah draft yang sudah tidak terpakai dan siap dihapus.", "5 April"),
        Note(202, "Todo Bulan Lalu", "Bayar listrik, beli sembako, telepon dokter.", "28 Maret")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        toolbar     = findViewById(R.id.topAppBar)
        rvTrash     = findViewById(R.id.rvTrash)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        toolbar.setNavigationOnClickListener { finish() }

        // Menu "Empty Trash" di overflow toolbar
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

    private fun showTrashedNotes() {
        if (trashedNotes.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvTrash.visibility     = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvTrash.visibility     = View.VISIBLE

            rvTrash.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            trashAdapter          = NoteAdapter(trashedNotes)
            rvTrash.adapter       = trashAdapter
        }
    }

    // Dialog konfirmasi sebelum menghapus semua catatan di Trash secara permanen
    private fun confirmEmptyTrash() {
        AlertDialog.Builder(this)
            .setTitle("Empty Trash?")
            .setMessage("All notes in Trash will be permanently deleted. This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                trashedNotes.clear()
                trashAdapter.filterList(trashedNotes)
                // Tampilkan empty state setelah trash dikosongkan
                layoutEmpty.visibility = View.VISIBLE
                rvTrash.visibility     = View.GONE
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
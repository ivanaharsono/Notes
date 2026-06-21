package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class TrashFragment : Fragment() {

    private lateinit var rvTrash: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var trashAdapter: NoteAdapter
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var layoutTrashSelectionActions: View
    private lateinit var btnCancelTrashSelection: TextView
    private lateinit var btnRestoreAllTrash: TextView
    private lateinit var btnRestoreSelectedTrash: TextView
    private lateinit var btnDeleteSelectedTrash: TextView

    private val trashedNotes = ArrayList<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvTrash = view.findViewById(R.id.rvTrash)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)

        layoutTrashSelectionActions = view.findViewById(R.id.layoutTrashSelectionActions)
        btnCancelTrashSelection = view.findViewById(R.id.btnCancelTrashSelection)
        btnRestoreAllTrash = view.findViewById(R.id.btnRestoreAllTrash)
        btnRestoreSelectedTrash = view.findViewById(R.id.btnRestoreSelectedTrash)
        btnDeleteSelectedTrash = view.findViewById(R.id.btnDeleteSelectedTrash)

        dbHelper = DatabaseHelper(requireContext())

        setupTrashSelectionActions()
        showTrashedNotes()
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized) {
            loadTrashFromDB()
        }
    }

    private fun setupTrashSelectionActions() {
        btnCancelTrashSelection.setOnClickListener {
            trashAdapter.clearSelection()
            updateTrashSelectionActions()
        }

        btnRestoreAllTrash.setOnClickListener {
            restoreAllNotes()
        }

        btnRestoreSelectedTrash.setOnClickListener {
            restoreSelectedNotes()
        }

        btnDeleteSelectedTrash.setOnClickListener {
            val selectedNotes = trashAdapter.getSelectedNotes()

            if (selectedNotes.isEmpty()) {
                confirmEmptyTrash()
            } else {
                confirmDeleteSelectedNotes()
            }
        }
    }

    private fun loadTrashFromDB() {
        trashedNotes.clear()
        trashedNotes.addAll(dbHelper.getTrashedNotes())

        if (trashedNotes.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvTrash.visibility = View.GONE
            layoutTrashSelectionActions.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvTrash.visibility = View.VISIBLE
            layoutTrashSelectionActions.visibility = View.GONE

            if (::trashAdapter.isInitialized) {
                trashAdapter.filterList(trashedNotes)
                updateTrashSelectionActions()
            }
        }
    }

    private fun showTrashedNotes() {
        rvTrash.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        trashAdapter = NoteAdapter(
            trashedNotes,
            onItemClick = { selectedNote ->
                val intent = Intent(requireContext(), DetailActivity::class.java)

                intent.putExtra("EXTRA_ID", selectedNote.id)
                intent.putExtra("EXTRA_JUDUL", selectedNote.judul)
                intent.putExtra("EXTRA_ISI", selectedNote.isi)
                intent.putExtra("EXTRA_TANGGAL", selectedNote.tanggal)
                intent.putExtra("EXTRA_IS_ARCHIVED", selectedNote.isArchived)
                intent.putExtra("EXTRA_IS_TRASHED", selectedNote.isTrashed)
                intent.putExtra("EXTRA_ATTACHMENTS", selectedNote.attachments)

                startActivity(intent)
            },
            onItemLongClick = {
                updateTrashSelectionActions()
            },
            onSelectionChanged = {
                updateTrashSelectionActions()
            }
        )

        rvTrash.adapter = trashAdapter
        loadTrashFromDB()
    }

    private fun updateTrashSelectionActions() {
        if (!::trashAdapter.isInitialized) return

        layoutTrashSelectionActions.visibility =
            if (trashAdapter.isSelectionMode) View.VISIBLE else View.GONE
    }

    private fun restoreSelectedNotes() {
        val selectedNotes = trashAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in selectedNotes) {
            dbHelper.updateNoteStatus(note.id, 0, 0)
        }

        trashAdapter.clearSelection()
        loadTrashFromDB()
        Toast.makeText(requireContext(), "Notes restored", Toast.LENGTH_SHORT).show()
    }

    private fun restoreAllNotes() {
        if (trashedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "Trash is empty", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in trashedNotes) {
            dbHelper.updateNoteStatus(note.id, 0, 0)
        }

        trashAdapter.clearSelection()
        loadTrashFromDB()
        Toast.makeText(requireContext(), "All notes restored", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDeleteSelectedNotes() {
        val selectedNotes = trashAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Delete permanently?")
            .setMessage("Selected notes will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                for (note in selectedNotes) {
                    dbHelper.deleteNotePermanently(note.id)
                }

                trashAdapter.clearSelection()
                loadTrashFromDB()
                Toast.makeText(requireContext(), "Notes deleted permanently", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmEmptyTrash() {
        AlertDialog.Builder(requireContext())
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
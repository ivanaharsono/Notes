package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context

class ArchiveFragment : Fragment() {

    private lateinit var rvArchive: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var layoutArchiveSelectionActions: View
    private lateinit var btnCancelArchiveSelection: TextView
    private lateinit var btnUnarchiveAll: TextView
    private lateinit var btnUnarchiveSelected: TextView
    private lateinit var btnDeleteArchiveSelected: TextView

    private val archivedNotes = ArrayList<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvArchive = view.findViewById(R.id.rvArchive)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)

        layoutArchiveSelectionActions = view.findViewById(R.id.layoutArchiveSelectionActions)
        btnCancelArchiveSelection = view.findViewById(R.id.btnCancelArchiveSelection)
        btnUnarchiveAll = view.findViewById(R.id.btnUnarchiveAll)
        btnUnarchiveSelected = view.findViewById(R.id.btnUnarchiveSelected)
        btnDeleteArchiveSelected = view.findViewById(R.id.btnDeleteArchiveSelected)

        dbHelper = DatabaseHelper(requireContext())

        setupArchiveSelectionActions()
        showArchivedNotes()
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized) {
            loadArchivedFromDB()
        }
    }

    private fun setupArchiveSelectionActions() {
        btnCancelArchiveSelection.setOnClickListener {
            noteAdapter.clearSelection()
            updateArchiveSelectionActions()
        }

        btnUnarchiveAll.setOnClickListener {
            unarchiveAllNotes()
        }

        btnUnarchiveSelected.setOnClickListener {
            unarchiveSelectedNotes()
        }

        btnDeleteArchiveSelected.setOnClickListener {
            deleteSelectedArchiveNotes()
        }
    }

    private fun loadArchivedFromDB() {
        archivedNotes.clear()
        archivedNotes.addAll(dbHelper.getArchivedNotes())

        if (archivedNotes.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvArchive.visibility = View.GONE
            layoutArchiveSelectionActions.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvArchive.visibility = View.VISIBLE

            if (::noteAdapter.isInitialized) {
                noteAdapter.filterList(archivedNotes)
                updateArchiveSelectionActions()
            }
        }
    }

    private fun showArchivedNotes() {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val layoutMode = sharedPref.getString("NOTE_LAYOUT", "Grid")
        rvArchive.layoutManager = if (layoutMode == "List") {
            LinearLayoutManager(requireContext())
        } else {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        noteAdapter = NoteAdapter(
            archivedNotes,
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
                updateArchiveSelectionActions()
            },
            onSelectionChanged = {
                updateArchiveSelectionActions()
            }
        )

        rvArchive.adapter = noteAdapter
        loadArchivedFromDB()
    }

    private fun updateArchiveSelectionActions() {
        if (!::noteAdapter.isInitialized) return

        layoutArchiveSelectionActions.visibility =
            if (noteAdapter.isSelectionMode) View.VISIBLE else View.GONE
    }

    private fun unarchiveSelectedNotes() {
        val selectedNotes = noteAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in selectedNotes) {
            dbHelper.updateNoteStatus(note.id, 0, 0)
        }

        noteAdapter.clearSelection()
        loadArchivedFromDB()
        Toast.makeText(requireContext(), "Notes unarchived", Toast.LENGTH_SHORT).show()
    }

    private fun unarchiveAllNotes() {
        if (archivedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "Archive is empty", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in archivedNotes) {
            dbHelper.updateNoteStatus(note.id, 0, 0)
        }

        noteAdapter.clearSelection()
        loadArchivedFromDB()
        Toast.makeText(requireContext(), "All notes unarchived", Toast.LENGTH_SHORT).show()
    }

    private fun deleteSelectedArchiveNotes() {
        val selectedNotes = noteAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in selectedNotes) {
            dbHelper.updateNoteStatus(note.id, 0, 1)
        }

        noteAdapter.clearSelection()
        loadArchivedFromDB()
        Toast.makeText(requireContext(), "Notes moved to Trash", Toast.LENGTH_SHORT).show()
    }
}
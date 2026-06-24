package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.Locale
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager


class DashboardFragment : Fragment() {

    private lateinit var rvNotes: RecyclerView
    private lateinit var fabAdd: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var layoutSelectionActions: View
    private lateinit var btnCancelSelection: TextView
    private lateinit var btnSelectAll: TextView
    private lateinit var btnArchiveSelected: TextView
    private lateinit var btnDeleteSelected: TextView
    private lateinit var btnLabelSelected: TextView

    private val list = ArrayList<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvNotes = view.findViewById(R.id.rvNotes)
        fabAdd = view.findViewById(R.id.fabAddNote)
        etSearch = view.findViewById(R.id.etSearch)

        layoutSelectionActions = view.findViewById(R.id.layoutSelectionActions)
        btnCancelSelection = view.findViewById(R.id.btnCancelSelection)
        btnSelectAll = view.findViewById(R.id.btnSelectAll)
        btnArchiveSelected = view.findViewById(R.id.btnArchiveSelected)
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected)
        btnLabelSelected = view.findViewById(R.id.btnLabelSelected)

        dbHelper = DatabaseHelper(requireContext())

        showNotes()
        loadNotesFromDB()
        setupFab()
        setupSearch()
        setupSelectionActions()
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized) {
            loadNotesFromDB()
        }
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), DetailActivity::class.java))
        }
    }

    private fun loadNotesFromDB() {
        list.clear()
        list.addAll(dbHelper.getAllNotes())

        if (::noteAdapter.isInitialized) {
            noteAdapter.filterList(list)
            updateSelectionActions()
        }
    }

    private fun showNotes() {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val layoutMode = sharedPref.getString("NOTE_LAYOUT", "Grid")

        rvNotes.layoutManager = if (layoutMode == "List") {
            LinearLayoutManager(requireContext())
        } else {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        noteAdapter = NoteAdapter(
            list,
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
            onItemLongClick = { updateSelectionActions() },
            onSelectionChanged = { updateSelectionActions() }
        )

        rvNotes.adapter = noteAdapter
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

    private fun filter(text: String) {
        val filteredList = ArrayList<Note>()

        for (item in list) {
            if (
                item.judul.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault())) ||
                item.isi.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }

        noteAdapter.filterList(filteredList)
        updateSelectionActions()
    }

    private fun setupSelectionActions() {
        btnCancelSelection.setOnClickListener {
            noteAdapter.clearSelection()
            updateSelectionActions()
        }

        btnSelectAll.setOnClickListener {
            noteAdapter.selectAll()
            updateSelectionActions()
        }

        btnArchiveSelected.setOnClickListener {
            archiveSelectedNotes()
        }

        btnDeleteSelected.setOnClickListener {
            deleteSelectedNotes()
        }

        btnLabelSelected.setOnClickListener {
            showLabelPickerForSelectedNotes()
        }
    }

    private fun updateSelectionActions() {
        if (!::noteAdapter.isInitialized) return

        val isSelecting = noteAdapter.isSelectionMode

        layoutSelectionActions.visibility = if (isSelecting) View.VISIBLE else View.GONE
        fabAdd.visibility = if (isSelecting) View.GONE else View.VISIBLE
    }

    private fun archiveSelectedNotes() {
        val selectedNotes = noteAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in selectedNotes) {
            dbHelper.updateNoteStatus(note.id, 1, 0)
        }

        noteAdapter.clearSelection()
        loadNotesFromDB()
        Toast.makeText(requireContext(), "Notes archived", Toast.LENGTH_SHORT).show()
    }

    private fun deleteSelectedNotes() {
        val selectedNotes = noteAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        for (note in selectedNotes) {
            dbHelper.updateNoteStatus(note.id, 0, 1)
        }

        noteAdapter.clearSelection()
        loadNotesFromDB()
        Toast.makeText(requireContext(), "Notes moved to Trash", Toast.LENGTH_SHORT).show()
    }

    private fun showLabelPickerForSelectedNotes() {
        val selectedNotes = noteAdapter.getSelectedNotes()

        if (selectedNotes.isEmpty()) {
            Toast.makeText(requireContext(), "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = LabelStore.getLabels(requireContext())

        if (labels.isEmpty()) {
            Toast.makeText(requireContext(), "No labels available", Toast.LENGTH_SHORT).show()
            return
        }

        val labelNames = mutableListOf("Remove label")
        labelNames.addAll(labels.map { it.name })

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Choose label")
            .setItems(labelNames.toTypedArray()) { _, which ->
                if (which == 0) {
                    for (note in selectedNotes) {
                        dbHelper.updateNoteLabel(note.id, "", "#4B22C6")
                    }

                    noteAdapter.clearSelection()
                    loadNotesFromDB()

                    Toast.makeText(
                        requireContext(),
                        "Label removed",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val selectedLabel = labels[which - 1]

                    for (note in selectedNotes) {
                        dbHelper.updateNoteLabel(
                            note.id,
                            selectedLabel.name,
                            selectedLabel.color
                        )
                    }

                    noteAdapter.clearSelection()
                    loadNotesFromDB()

                    Toast.makeText(
                        requireContext(),
                        "Label added",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }
}
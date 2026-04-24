package com.angels.notes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class LabelsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etNewLabel: TextInputEditText
    private lateinit var rvLabels: androidx.recyclerview.widget.RecyclerView
    private lateinit var layoutEmpty: android.widget.LinearLayout
    private lateinit var labelAdapter: LabelAdapter

    // Dummy data label yang sudah ada
    // Pada implementasi nyata, data ini disimpan dan diambil dari database
    private val labelList = mutableListOf("Personal", "Work", "Ideas", "Shopping")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labels)

        toolbar     = findViewById(R.id.topAppBar)
        etNewLabel  = findViewById(R.id.etNewLabel)
        rvLabels    = findViewById(R.id.rvLabels)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        updateEmptyState()

        // Tombol Add: tambahkan label baru ke daftar
        findViewById<View>(R.id.btnAddLabel).setOnClickListener {
            val newLabel = etNewLabel.text.toString().trim()
            when {
                newLabel.isEmpty() -> {
                    etNewLabel.error = "Label name cannot be empty"
                }
                labelList.any { it.equals(newLabel, ignoreCase = true) } -> {
                    etNewLabel.error = "Label already exists"
                }
                else -> {
                    labelList.add(newLabel)
                    labelAdapter.notifyItemInserted(labelList.size - 1)
                    etNewLabel.text?.clear()
                    updateEmptyState()
                    Toast.makeText(this, "Label \"$newLabel\" created", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        labelAdapter = LabelAdapter(labelList) { position ->
            // Callback hapus label: tampilkan konfirmasi lalu hapus
            val labelName = labelList[position]
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Label?")
                .setMessage("Delete \"$labelName\"? Notes with this label will not be deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    labelList.removeAt(position)
                    labelAdapter.notifyItemRemoved(position)
                    updateEmptyState()
                    Toast.makeText(this, "Label deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        rvLabels.layoutManager = LinearLayoutManager(this)
        rvLabels.adapter       = labelAdapter
        rvLabels.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    // Tampilkan atau sembunyikan empty state berdasarkan jumlah label
    private fun updateEmptyState() {
        if (labelList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvLabels.visibility    = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvLabels.visibility    = View.VISIBLE
        }
    }
}
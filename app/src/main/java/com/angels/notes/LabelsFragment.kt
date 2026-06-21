package com.angels.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class LabelsFragment : Fragment() {

    private lateinit var etNewLabel: TextInputEditText
    private lateinit var rvLabels: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var labelAdapter: LabelAdapter

    private val labelList = mutableListOf<NoteLabel>()

    private val labelColors = listOf(
        "#4B22C6",
        "#1976D2",
        "#2E7D32",
        "#F9A825",
        "#E65100",
        "#C2185B"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_labels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etNewLabel = view.findViewById(R.id.etNewLabel)
        rvLabels = view.findViewById(R.id.rvLabels)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)

        labelList.clear()
        labelList.addAll(LabelStore.getLabels(requireContext()))

        setupRecyclerView()
        setupAddLabelButton(view)
        updateEmptyState()
    }

    private fun setupAddLabelButton(view: View) {
        view.findViewById<View>(R.id.btnAddLabel).setOnClickListener {
            val newLabel = etNewLabel.text.toString().trim()

            when {
                newLabel.isEmpty() -> {
                    etNewLabel.error = "Label name cannot be empty"
                }

                labelList.any { it.name.equals(newLabel, ignoreCase = true) } -> {
                    etNewLabel.error = "Label already exists"
                }

                else -> {
                    val color = labelColors[labelList.size % labelColors.size]
                    val label = NoteLabel(newLabel, color)

                    labelList.add(label)
                    LabelStore.saveLabels(requireContext(), labelList)

                    labelAdapter.notifyItemInserted(labelList.size - 1)
                    etNewLabel.text?.clear()
                    updateEmptyState()

                    Toast.makeText(
                        requireContext(),
                        "Label \"$newLabel\" created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        labelAdapter = LabelAdapter(labelList) { position ->
            val labelName = labelList[position].name

            AlertDialog.Builder(requireContext())
                .setTitle("Delete Label?")
                .setMessage("Delete \"$labelName\"? Notes with this label will not be deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    labelList.removeAt(position)
                    LabelStore.saveLabels(requireContext(), labelList)

                    labelAdapter.notifyItemRemoved(position)
                    updateEmptyState()

                    Toast.makeText(
                        requireContext(),
                        "Label deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        rvLabels.layoutManager = LinearLayoutManager(requireContext())
        rvLabels.adapter = labelAdapter

        if (rvLabels.itemDecorationCount == 0) {
            rvLabels.addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun updateEmptyState() {
        if (labelList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvLabels.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvLabels.visibility = View.VISIBLE
        }
    }
}
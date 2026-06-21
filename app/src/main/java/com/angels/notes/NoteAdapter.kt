package com.angels.notes

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private var listNote: ArrayList<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Unit,
    private val onSelectionChanged: () -> Unit = {}
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val selectedNoteIds = mutableSetOf<Int>()

    var isSelectionMode = false
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = listNote[position]
        val isSelected = selectedNoteIds.contains(note.id)
        val hasLabel = note.labelName.isNotEmpty()

        holder.tvTitle.text = note.judul
        holder.tvDesc.text = note.isi

        holder.checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = isSelected

        holder.card.setCardBackgroundColor(
            if (isSelected) Color.parseColor("#E8E0FF") else Color.WHITE
        )

        if (hasLabel) {
            val labelColor = parseColorSafe(note.labelColor)

            holder.labelAccent.visibility = View.VISIBLE
            holder.labelAccent.setBackgroundColor(labelColor)

            holder.tvLabel.visibility = View.VISIBLE
            holder.tvLabel.text = note.labelName
            holder.tvLabel.background = makeChipBackground(labelColor)
            holder.tvLabel.setTextColor(Color.WHITE)
        } else {
            holder.labelAccent.visibility = View.GONE
            holder.tvLabel.visibility = View.INVISIBLE
            holder.tvLabel.text = ""
        }

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(note)
            } else {
                onItemClick(note)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
            }

            toggleSelection(note)
            onItemLongClick(note)
            true
        }
    }

    override fun getItemCount(): Int = listNote.size

    fun filterList(filteredList: ArrayList<Note>) {
        listNote = filteredList
        selectedNoteIds.clear()
        isSelectionMode = false
        onSelectionChanged()
        notifyDataSetChanged()
    }

    fun toggleSelection(note: Note) {
        if (selectedNoteIds.contains(note.id)) {
            selectedNoteIds.remove(note.id)
        } else {
            selectedNoteIds.add(note.id)
        }

        if (selectedNoteIds.isEmpty()) {
            isSelectionMode = false
        }

        onSelectionChanged()
        notifyDataSetChanged()
    }

    fun selectAll() {
        isSelectionMode = true
        selectedNoteIds.clear()
        selectedNoteIds.addAll(listNote.map { it.id })
        onSelectionChanged()
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedNoteIds.clear()
        isSelectionMode = false
        onSelectionChanged()
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<Note> {
        return listNote.filter { selectedNoteIds.contains(it.id) }
    }

    private fun parseColorSafe(color: String): Int {
        return try {
            Color.parseColor(color)
        } catch (_: Exception) {
            Color.parseColor("#4B22C6")
        }
    }

    private fun makeChipBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 18f
        }
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.noteCard)
        val labelAccent: View = itemView.findViewById(R.id.labelAccent)
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvItemDescription)
        val tvLabel: TextView = itemView.findViewById(R.id.tvItemLabel)
        val checkBox: CheckBox = itemView.findViewById(R.id.cbSelected)
    }
}
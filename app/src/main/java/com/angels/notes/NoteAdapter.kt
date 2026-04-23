package com.angels.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// UBAH 'val' menjadi 'var' agar list bisa di-update oleh fungsi pencarian
class NoteAdapter(private var listNote: ArrayList<Note>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = listNote[position]
        holder.tvTitle.text = note.judul
        holder.tvDesc.text = note.isi
    }

    override fun getItemCount(): Int {
        return listNote.size
    }

    fun filterList(filteredList: ArrayList<Note>) {
        listNote = filteredList
        notifyDataSetChanged()
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvItemDescription)
    }
}
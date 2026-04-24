package com.angels.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LabelAdapter(
    private val labels: MutableList<String>,
    private val onDeleteClick: (Int) -> Unit   // callback posisi item yang dihapus
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_label, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.tvLabelName.text = labels[position]

        holder.btnDelete.setOnClickListener {
            onDeleteClick(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = labels.size

    class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLabelName: TextView   = itemView.findViewById(R.id.tvLabelName)
        val btnDelete: ImageButton  = itemView.findViewById(R.id.btnDeleteLabel)
    }
}
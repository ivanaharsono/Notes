package com.angels.notes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LabelAdapter(
    private val labels: MutableList<NoteLabel>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_label, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        val label = labels[position]

        holder.tvLabelName.text = label.name
        holder.imgLabelIcon.setColorFilter(Color.parseColor(label.color))

        holder.btnDelete.setOnClickListener {
            val adapterPosition = holder.adapterPosition

            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClick(adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = labels.size

    class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgLabelIcon: ImageView = itemView.findViewById(R.id.imgLabelIcon)
        val tvLabelName: TextView = itemView.findViewById(R.id.tvLabelName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteLabel)
    }
}
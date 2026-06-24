package com.angels.notes

import android.content.Context

data class NoteLabel(
    val name: String,
    val color: String
)

object LabelStore {
    private const val PREF_NAME = "labels_pref"
    private const val KEY_LABELS = "labels"

    private val defaultLabels = listOf(
        NoteLabel("Personal", "#4B22C6"),
        NoteLabel("Work", "#1976D2"),
        NoteLabel("Ideas", "#F9A825"),
        NoteLabel("Shopping", "#2E7D32")
    )

    fun getLabels(context: Context): MutableList<NoteLabel> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LABELS, null)

        if (saved.isNullOrBlank()) {
            saveLabels(context, defaultLabels.toMutableList())
            return defaultLabels.toMutableList()
        }

        return saved.split("|")
            .mapNotNull {
                val parts = it.split("::")
                if (parts.size == 2) NoteLabel(parts[0], parts[1]) else null
            }
            .toMutableList()
    }

    fun saveLabels(context: Context, labels: MutableList<NoteLabel>) {
        val raw = labels.joinToString("|") { "${it.name}::${it.color}" }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LABELS, raw)
            .apply()
    }
}
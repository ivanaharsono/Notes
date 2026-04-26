package com.angels.notes

import java.io.Serializable

data class Note(
    val id: Int,
    val judul: String,
    val isi: String,
    val tanggal: String
) : Serializable
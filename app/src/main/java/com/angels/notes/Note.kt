package com.angels.notes

data class Note(
    var id: Int = 0,
    var judul: String,
    var isi: String,
    var tanggal: String,
    var isArchived: Int = 0,
    var isTrashed: Int = 0
)
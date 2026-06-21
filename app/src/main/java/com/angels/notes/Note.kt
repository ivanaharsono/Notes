package com.angels.notes

data class Note(
    var id: Int = 0,
    var judul: String,
    var isi: String,
    var tanggal: String,
    var isArchived: Int = 0,
    var isTrashed: Int = 0,
    var attachments: String = "",
    var labelName: String = "",
    var labelColor: String = "#4B22C6"
)
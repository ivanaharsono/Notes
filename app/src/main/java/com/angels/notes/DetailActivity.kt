package com.angels.notes

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    // KELOMPOK 1: Inisialisasi komponen UI/Layout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnBold: MaterialButton
    private lateinit var btnItalic: MaterialButton
    private lateinit var btnUnderline: MaterialButton
    private lateinit var btnList: MaterialButton
    private lateinit var btnToggleFormat: ImageButton
    private lateinit var formatScrollView: HorizontalScrollView

    // KELOMPOK 2: State penanda aktif/tidaknya format teks
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var isBullet = false

    // KELOMPOK 3: Warna tombol format (aktif = Indigo Blue, tidak aktif = Abu-abu)
    private lateinit var colorActive: ColorStateList
    private lateinit var colorInactive: ColorStateList

    // KELOMPOK 4: Variabel penanda status catatan (diambil dari Intent)
    private var noteId: Int = -1
    private var noteDate: String = ""
    private var isArchivedNote: Int = 0
    private var isTrashedNote: Int = 0

    // KELOMPOK 5: Instance Database SQLite
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Membuka koneksi ke database lokal SQLite
        dbHelper = DatabaseHelper(this)

        // Konfigurasi warna untuk tombol formatting teks
        colorActive = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.IndigoBlue))
        colorInactive = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_text))

        // Memanggil semua fungsi inisialisasi awal halaman
        setupViews()
        setupToolbar()
        setupFormattingButtons()
        setupToggleFormatting()
        loadNoteData()       // Membaca status data (apakah dari Main, Archive, atau Trash)
        setupSaveButton()    // Mengurus logika simpan/edit ke SQLite
        setupTextWatcher()   // Mengurus format teks dinamis saat mengetik
    }

    // Menghubungkan variabel Kotlin dengan ID komponen di file XML
    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        etTitle = findViewById(R.id.etTitle)
        etBody = findViewById(R.id.etBody)
        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnUnderline = findViewById(R.id.btnUnderline)
        btnList = findViewById(R.id.btnList)
        btnToggleFormat = findViewById(R.id.btnToggleFormat)
        formatScrollView = findViewById(R.id.formatScrollView)
    }

    // Mengatur aksi klik menu di bagian Toolbar atas (Aksi Pindah Status)
    private fun setupToolbar() {
        // Tombol panah back untuk menutup halaman
        toolbar.setNavigationOnClickListener { finish() }
        
        toolbar.setOnMenuItemClickListener { menuItem ->
            val currentNoteId = intent.getIntExtra("EXTRA_ID", -1)
            if (currentNoteId != -1) {
                when (menuItem.itemId) {
                    1 -> { // Pilihan 1: Pulihkan dari Trash (is_archived=0, is_trashed=0)
                        dbHelper.updateNoteStatus(currentNoteId, 0, 0)
                        Toast.makeText(this, "Note restored to main menu!", Toast.LENGTH_SHORT).show()
                        finish()
                        true
                    }
                    2 -> { // Pilihan 2: Hapus permanen data dari SQLite
                        dbHelper.deleteNotePermanently(currentNoteId)
                        Toast.makeText(this, "Note deleted permanently!", Toast.LENGTH_SHORT).show()
                        finish()
                        true
                    }
                    3 -> { // Pilihan 3: Keluarkan dari Archive (is_archived=0, is_trashed=0)
                        dbHelper.updateNoteStatus(currentNoteId, 0, 0)
                        Toast.makeText(this, "Note restored to main menu!", Toast.LENGTH_SHORT).show()
                        finish()
                        true
                    }
                    4, 6 -> { // Pilihan 4 & 6: Buang ke kotak sampah (is_archived=0, is_trashed=1)
                        dbHelper.updateNoteStatus(currentNoteId, 0, 1)
                        Toast.makeText(this, "Note moved to trash!", Toast.LENGTH_SHORT).show()
                        finish()
                        true
                    }
                    5 -> { // Pilihan 5: Masukkan ke Archive (is_archived=1, is_trashed=0)
                        dbHelper.updateNoteStatus(currentNoteId, 1, 0)
                        Toast.makeText(this, "Note archived!", Toast.LENGTH_SHORT).show()
                        finish()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }

    // Membaca data kiriman halaman lain dan menampilkan menu toolbar secara dinamis
    private fun loadNoteData() {
        noteId = intent.getIntExtra("EXTRA_ID", -1)
        isArchivedNote = intent.getIntExtra("EXTRA_IS_ARCHIVED", 0)
        isTrashedNote = intent.getIntExtra("EXTRA_IS_TRASHED", 0)

        // Reset menu toolbar terlebih dahulu
        toolbar.menu.clear()

        if (noteId != -1) { // MODE EDIT: Tampilkan teks lama ke kolom inputan
            val judul = intent.getStringExtra("EXTRA_JUDUL")
            val isi = intent.getStringExtra("EXTRA_ISI")
            noteDate = intent.getStringExtra("EXTRA_TANGGAL") ?: ""

            etTitle.setText(judul)
            etBody.setText(isi)

            // Cek lokasi asal data untuk membedakan isi menu pilihan di pojok kanan toolbar
            if (isTrashedNote == 1) {
                toolbar.title = "Trash"
                toolbar.menu.add(0, 1, 0, "Restore Note")
                toolbar.menu.add(0, 2, 1, "Delete Permanently")
            } else if (isArchivedNote == 1) {
                toolbar.title = "Archive"
                toolbar.menu.add(0, 3, 0, "Restore to Main Notes")
                toolbar.menu.add(0, 4, 1, "Move to Trash")
            } else {
                toolbar.title = "Edit Note"
                toolbar.menu.add(0, 5, 0, "Archive")
                toolbar.menu.add(0, 6, 1, "Move to Trash")
            }
        } else { // MODE BARU: Jika ID data adalah -1 (Menekan tombol + dari beranda)
            toolbar.title = "New Note"
        }
    }

    // Menangani aksi klik tombol Save (Eksekusi fungsi CRUD ke SQLite)
    private fun setupSaveButton() {
        findViewById<View>(R.id.btnSave).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val body = etBody.text.toString().trim()

            // Validasi: Judul catatan tidak boleh kosong
            if (title.isEmpty()) {
                etTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }

            val currentNoteId = intent.getIntExtra("EXTRA_ID", -1)

            if (currentNoteId != -1) {
                // FUNGSI UPDATE: Memperbarui data lama di SQLite
                val updatedNote = Note(
                    id = currentNoteId,
                    judul = title,
                    isi = body,
                    tanggal = noteDate,
                    isArchived = isArchivedNote,
                    isTrashed = isTrashedNote
                )
                dbHelper.updateNote(updatedNote)
                Toast.makeText(this, "Note updated!", Toast.LENGTH_SHORT).show()
            } else {
                // FUNGSI CREATE: Membuat baris catatan baru ke SQLite
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val currentDate = sdf.format(Date()) // Tanggal otomatis hari ini

                val newNote = Note(
                    judul = title,
                    isi = body,
                    tanggal = currentDate,
                    isArchived = 0,
                    isTrashed = 0
                )
                dbHelper.addNote(newNote)
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
            }

            finish() // Menutup halaman detail dan kembali ke menu utama
        }
    }

    // Mengatur trigger klik pada deretan tombol editing teks (Bold, Italic, dll)
    private fun setupFormattingButtons() {
        btnBold.setOnClickListener {
            isBold = !isBold
            updateButtonState(btnBold, isBold)
            applyFormattingToSelectionOrCursor()
        }

        btnItalic.setOnClickListener {
            isItalic = !isItalic
            updateButtonState(btnItalic, isItalic)
            applyFormattingToSelectionOrCursor()
        }

        btnUnderline.setOnClickListener {
            isUnderline = !isUnderline
            updateButtonState(btnUnderline, isUnderline)
            applyFormattingToSelectionOrCursor()
        }

        btnList.setOnClickListener {
            isBullet = !isBullet
            updateButtonState(btnList, isBullet)
            applyFormattingToSelectionOrCursor()
        }
    }

    // Mengubah visual tombol format teks (ganti warna outline jika aktif)
    private fun updateButtonState(button: MaterialButton, isActive: Boolean) {
        if (isActive) {
            button.iconTint = colorActive
            button.strokeColor = colorActive
            button.strokeWidth = 2
        } else {
            button.iconTint = colorInactive
            button.strokeColor = ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            button.strokeWidth = 0
        }
    }

    // Mengecek posisi kursor/blok teks untuk diaplikasikan format barunya
    private fun applyFormattingToSelectionOrCursor() {
        val selectionStart = etBody.selectionStart
        val selectionEnd = etBody.selectionEnd

        if (selectionStart != selectionEnd) {
            applyFormattingToRange(selectionStart, selectionEnd)
        }
    }

    // Menghapus format teks lama lalu menimpanya dengan objek Span format baru
    private fun applyFormattingToRange(start: Int, end: Int) {
        val spannable = etBody.text as Spannable

        val styleSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        for (span in styleSpans) spannable.removeSpan(span)

        val underlines = spannable.getSpans(start, end, UnderlineSpan::class.java)
        for (span in underlines) spannable.removeSpan(span)

        val bullets = spannable.getSpans(start, end, BulletSpan::class.java)
        for (span in bullets) spannable.removeSpan(span)

        if (isBold) {
            spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (isItalic) {
            spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (isUnderline) {
            spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (isBullet) {
            spannable.setSpan(BulletSpan(16), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    // Logika TextWatcher agar teks baru yang sedang diketik otomatis mengikuti format aktif
    private fun setupTextWatcher() {
        etBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return

                val spannable = s as Spannable

                if (!isBold && !isItalic && !isUnderline && !isBullet) return

                val length = s.length
                if (length == 0) return

                val lastCharStart = length - 1

                val existingStyleSpans = spannable.getSpans(lastCharStart, length, StyleSpan::class.java)
                for (span in existingStyleSpans) spannable.removeSpan(span)

                val existingUnderlines = spannable.getSpans(lastCharStart, length, UnderlineSpan::class.java)
                for (span in existingUnderlines) spannable.removeSpan(span)

                val existingBullets = spannable.getSpans(lastCharStart, length, BulletSpan::class.java)
                for (span in existingBullets) spannable.removeSpan(span)

                if (isBold) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), lastCharStart, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isItalic) {
                    spannable.setSpan(StyleSpan(Typeface.ITALIC), lastCharStart, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isUnderline) {
                    spannable.setSpan(UnderlineSpan(), lastCharStart, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isBullet) {
                    spannable.setSpan(BulletSpan(16), lastCharStart, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        })
    }

    // Menyembunyikan/menampilkan baris tombol format teks (Rich Text Editor menu)
    private fun setupToggleFormatting() {
        btnToggleFormat.setOnClickListener {
            formatScrollView.visibility = if (formatScrollView.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
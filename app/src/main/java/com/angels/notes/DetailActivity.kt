package com.angels.notes

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.Window
import android.os.Build
import android.view.WindowInsetsController

class DetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var tvNoteMeta: TextView
    private lateinit var formatScrollView: HorizontalScrollView
    private lateinit var mediaScrollView: HorizontalScrollView
    private lateinit var layoutAttachments: LinearLayout
    private lateinit var btnBold: MaterialButton
    private lateinit var btnItalic: MaterialButton
    private lateinit var btnUnderline: MaterialButton
    private lateinit var btnList: MaterialButton
    private lateinit var btnAddImage: MaterialButton
    private lateinit var btnDrawing: MaterialButton

    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var isBullet = false

    private lateinit var colorActive: ColorStateList
    private lateinit var colorInactive: ColorStateList

    private var noteId: Int = -1
    private var noteDate: String = ""
    private var displayDate: String = ""
    private var isArchivedNote: Int = 0
    private var isTrashedNote: Int = 0
    private val currentAttachments = mutableListOf<String>()

    private lateinit var dbHelper: DatabaseHelper
    private val autoSaveHandler = Handler(Looper.getMainLooper())
    private var lastSavedTitle = ""
    private var lastSavedBody = ""
    private var lastSavedAttachments = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val savedPath = saveImageFromUri(uri)
            if (savedPath != null) {
                addAttachment(savedPath)
            } else {
                Toast.makeText(this, "Photo could not be added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val autoSaveRunnable = Runnable {
        saveNoteAutomatically()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setupStatusBar()
        window.statusBarColor = ContextCompat.getColor(this, R.color.IndigoBlue)

        dbHelper = DatabaseHelper(this)
        colorActive = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.IndigoBlue))
        colorInactive = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_text))

        setupViews()
        setupToolbar()
        setupFormattingButtons()
        setupKeyboardToolbar()
        loadNoteData()
        renderAttachments()
        setupAutoSave()
        setupTextWatcher()
        updateNoteMeta()
    }

    override fun onPause() {
        super.onPause()
        autoSaveHandler.removeCallbacks(autoSaveRunnable)
        saveNoteAutomatically()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        etTitle = findViewById(R.id.etTitle)
        etBody = findViewById(R.id.etBody)
        tvNoteMeta = findViewById(R.id.tvNoteMeta)
        formatScrollView = findViewById(R.id.formatScrollView)
        mediaScrollView = findViewById(R.id.mediaScrollView)
        layoutAttachments = findViewById(R.id.layoutAttachments)
        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnUnderline = findViewById(R.id.btnUnderline)
        btnList = findViewById(R.id.btnList)
        btnAddImage = findViewById(R.id.btnAddImage)
        btnDrawing = findViewById(R.id.btnDrawing)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            saveNoteAutomatically()
            finish()
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            saveNoteAutomatically()

            if (noteId == -1) {
                return@setOnMenuItemClickListener false
            }

            when (menuItem.itemId) {
                1 -> {
                    dbHelper.updateNoteStatus(noteId, 0, 0)
                    Toast.makeText(this, "Note restored to main menu!", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }
                2 -> {
                    dbHelper.deleteNotePermanently(noteId)
                    Toast.makeText(this, "Note deleted permanently!", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }
                3 -> {
                    dbHelper.updateNoteStatus(noteId, 0, 0)
                    Toast.makeText(this, "Note restored to main menu!", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }
                4, 6 -> {
                    dbHelper.updateNoteStatus(noteId, 0, 1)
                    Toast.makeText(this, "Note moved to trash!", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }
                5 -> {
                    dbHelper.updateNoteStatus(noteId, 1, 0)
                    Toast.makeText(this, "Note archived!", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }
                else -> false
            }
        }

        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, android.R.color.black))
    }

    private fun loadNoteData() {
        noteId = intent.getIntExtra("EXTRA_ID", -1)
        isArchivedNote = intent.getIntExtra("EXTRA_IS_ARCHIVED", 0)
        isTrashedNote = intent.getIntExtra("EXTRA_IS_TRASHED", 0)
        currentAttachments.clear()
        currentAttachments.addAll(parseAttachments(intent.getStringExtra("EXTRA_ATTACHMENTS") ?: ""))
        toolbar.menu.clear()

        if (noteId != -1) {
            val judul = intent.getStringExtra("EXTRA_JUDUL")
            val isi = intent.getStringExtra("EXTRA_ISI")
            noteDate = intent.getStringExtra("EXTRA_TANGGAL") ?: ""
            displayDate = noteDate.ifEmpty { getCurrentDisplayDate() }

            etTitle.setText(judul)
            etBody.setText(isi)

            when {
                isTrashedNote == 1 -> {
                    toolbar.title = "Trash"
                    toolbar.menu.add(0, 1, 0, "Restore Note")
                    toolbar.menu.add(0, 2, 1, "Delete Permanently")
                }
                isArchivedNote == 1 -> {
                    toolbar.title = "Archive"
                    toolbar.menu.add(0, 3, 0, "Restore to Main Notes")
                    toolbar.menu.add(0, 4, 1, "Move to Trash")
                }
                else -> {
                    toolbar.title = "Edit Note"
                    showDefaultNoteMenu()
                }
            }
        } else {
            toolbar.title = "New Note"
            noteDate = getCurrentDatabaseDate()
            displayDate = getCurrentDisplayDate()
        }
    }

    private fun showDefaultNoteMenu() {
        toolbar.menu.clear()
        toolbar.menu.add(0, 5, 0, "Archive")
        toolbar.menu.add(0, 6, 1, "Move to Trash")
    }

    private fun setupKeyboardToolbar() {
        formatScrollView.visibility = View.VISIBLE
    }

    private fun setupAutoSave() {
        lastSavedTitle = etTitle.text.toString().trim()
        lastSavedBody = etBody.text.toString().trim()
        lastSavedAttachments = serializeAttachments()

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateNoteMeta()
                scheduleAutoSave()
            }
        }

        etTitle.addTextChangedListener(watcher)
        etBody.addTextChangedListener(watcher)
    }

    private fun scheduleAutoSave() {
        autoSaveHandler.removeCallbacks(autoSaveRunnable)
        autoSaveHandler.postDelayed(autoSaveRunnable, 800)
    }

    private fun updateNoteMeta() {
        val characterCount = etBody.text.toString().length
        tvNoteMeta.text = "$displayDate | $characterCount characters"
    }

    private fun saveNoteAutomatically() {
        val rawTitle = etTitle.text.toString().trim()
        val body = etBody.text.toString().trim()
        val attachments = serializeAttachments()

        if (rawTitle.isEmpty() && body.isEmpty() && attachments.isEmpty()) {
            return
        }

        val title = if (rawTitle.isEmpty()) "Untitled Note" else rawTitle

        if (title == lastSavedTitle && body == lastSavedBody && attachments == lastSavedAttachments) {
            return
        }

        if (noteId != -1) {
            val updatedNote = Note(
                id = noteId,
                judul = title,
                isi = body,
                tanggal = noteDate,
                isArchived = isArchivedNote,
                isTrashed = isTrashedNote,
                attachments = attachments
            )
            dbHelper.updateNote(updatedNote)
        } else {
            val newNote = Note(
                judul = title,
                isi = body,
                tanggal = noteDate,
                isArchived = 0,
                isTrashed = 0,
                attachments = attachments
            )

            val newId = dbHelper.addNote(newNote)
            if (newId != -1L) {
                noteId = newId.toInt()
                toolbar.title = "Edit Note"
                showDefaultNoteMenu()
            }
        }

        lastSavedTitle = title
        lastSavedBody = body
        lastSavedAttachments = attachments
    }

    private fun getCurrentDatabaseDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date())
    }

    private fun getCurrentDisplayDate(): String {
        val sdf = SimpleDateFormat("MMMM dd HH:mm", Locale.ENGLISH)
        return sdf.format(Date())
    }

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

        btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnDrawing.setOnClickListener {
            showDrawingDialog()
        }
    }

    private fun updateButtonState(button: MaterialButton, isActive: Boolean) {
        if (isActive) {
            button.iconTint = colorActive
            button.strokeColor = colorActive
            button.strokeWidth = 2
        } else {
            button.iconTint = colorInactive
            button.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            button.strokeWidth = 0
        }
    }

    private fun applyFormattingToSelectionOrCursor() {
        val selectionStart = etBody.selectionStart
        val selectionEnd = etBody.selectionEnd

        if (selectionStart != selectionEnd) {
            applyFormattingToRange(selectionStart, selectionEnd)
        }
    }

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

        scheduleAutoSave()
    }

    private fun setupTextWatcher() {
        etBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                if (!isBold && !isItalic && !isUnderline && !isBullet) return

                val spannable = s as Spannable
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

    private fun addAttachment(path: String) {
        currentAttachments.add(path)
        renderAttachments()
        scheduleAutoSave()
    }

    private fun renderAttachments() {
        layoutAttachments.removeAllViews()
        mediaScrollView.visibility = if (currentAttachments.isEmpty()) View.GONE else View.VISIBLE

        val imageWidth = resources.displayMetrics.widthPixels - dp(32)
        val imageHeight = dp(260)

        currentAttachments.forEach { path ->
            val imageView = ImageView(this)

            val params = LinearLayout.LayoutParams(imageWidth, imageHeight)
            params.marginEnd = dp(12)

            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true
            imageView.setBackgroundColor(Color.WHITE)
            imageView.setPadding(dp(4), dp(4), dp(4), dp(4))
            imageView.setImageURI(Uri.fromFile(File(path)))

            imageView.setOnLongClickListener {
                confirmRemoveAttachment(path)
                true
            }

            layoutAttachments.addView(imageView)
        }
    }

    private fun confirmRemoveAttachment(path: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove attachment?")
            .setMessage("This photo or drawing will be removed from this note.")
            .setPositiveButton("Remove") { _, _ ->
                currentAttachments.remove(path)
                renderAttachments()
                scheduleAutoSave()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDrawingDialog() {
        val drawingView = DrawingCanvasView(this)
        val height = resources.displayMetrics.heightPixels / 2
        drawingView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

        AlertDialog.Builder(this)
            .setTitle("Drawing")
            .setView(drawingView)
            .setPositiveButton("Save") { _, _ ->
                val savedPath = saveDrawingView(drawingView)
                if (savedPath != null) {
                    addAttachment(savedPath)
                } else {
                    Toast.makeText(this, "Drawing could not be saved", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveImageFromUri(uri: Uri): String? {
        return try {
            val outputFile = createMediaFile("photo")
            contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            outputFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun saveDrawingView(drawingView: DrawingCanvasView): String? {
        if (drawingView.width == 0 || drawingView.height == 0) return null

        return try {
            val bitmap = Bitmap.createBitmap(drawingView.width, drawingView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            drawingView.draw(canvas)

            val outputFile = createMediaFile("drawing")
            outputFile.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            outputFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun createMediaFile(prefix: String): File {
        val mediaDir = File(filesDir, "note_media")
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        return File(mediaDir, "${prefix}_${System.currentTimeMillis()}.png")
    }

    private fun parseAttachments(raw: String): List<String> {
        return raw.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun serializeAttachments(): String {
        return currentAttachments.joinToString("\n")
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private class DrawingCanvasView(context: Context) : View(context) {
        private val path = Path()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        init {
            setBackgroundColor(Color.WHITE)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawPath(path, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> path.moveTo(event.x, event.y)
                MotionEvent.ACTION_MOVE -> path.lineTo(event.x, event.y)
            }
            invalidate()
            return true
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.IndigoBlue)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        val statusBarBackground = findViewById<View>(R.id.statusBarBackground)
        val statusBarHeight = resources.getIdentifier("status_bar_height", "dimen", "android")
            .takeIf { it > 0 }
            ?.let { resources.getDimensionPixelSize(it) }
            ?: 0

        statusBarBackground.layoutParams.height = statusBarHeight
        statusBarBackground.requestLayout()
    }
}

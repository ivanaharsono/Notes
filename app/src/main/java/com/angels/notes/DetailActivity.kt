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
import android.os.Build
import android.view.WindowInsetsController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ScrollView

class DetailActivity : AppCompatActivity() {

    companion object {
        private const val IMG_MARK_OPEN = "\u0002IMG:"
        private const val IMG_MARK_CLOSE = "\u0003"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var tvNoteMeta: TextView
    private lateinit var formatScrollView: HorizontalScrollView
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
    private var activeEditText: EditText? = null

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
        activeEditText = etBody

        etBody.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                activeEditText = etBody
                syncFormattingButtonsToCursor(etBody)
            }
        }

        etBody.setOnTouchListener { _, _ ->
            etBody.postDelayed({
                syncFormattingButtonsToCursor(etBody)
            }, 100)
            false
        }

        tvNoteMeta = findViewById(R.id.tvNoteMeta)
        formatScrollView = findViewById(R.id.formatScrollView)
        layoutAttachments = findViewById(R.id.dynamicContent)
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
            rebuildBodyContent(isi ?: "")

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
        val rootView = findViewById<View>(android.R.id.content)
        val mainScrollView = findViewById<ScrollView>(R.id.mainScrollView)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            if (isKeyboardVisible) {
                formatScrollView.visibility = View.VISIBLE
                val basePadding = if (imeHeight > navBarHeight) imeHeight - navBarHeight else imeHeight
                val extraLift = dp(12)
                view.setPadding(0, 0, 0, basePadding + extraLift)
            } else {
                formatScrollView.visibility = View.GONE
                view.setPadding(0, 0, 0, 0)
            }

            insets
        }
    }

    private fun setupAutoSave() {
        lastSavedTitle = etTitle.text.toString().trim()
        lastSavedBody = collectAllBodyText().trim()
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
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        var characterCount = 0
        for (i in 0 until mainContainer.childCount) {
            val child = mainContainer.getChildAt(i)
            if (child is EditText) characterCount += child.text.toString().length
        }
        tvNoteMeta.text = "$displayDate | $characterCount characters"
    }

    // Kumpulkan isi dari semua child di mainContainer SESUAI URUTAN ASLINYA
    // (teks - gambar - teks - gambar dst), bukan teks semua digabung lalu gambar di akhir.
    private fun collectAllBodyText(): String {
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val sb = StringBuilder()
        for (i in 0 until mainContainer.childCount) {
            val child = mainContainer.getChildAt(i)
            if (child is EditText) {
                val text = child.text
                if (text != null && text.isNotEmpty()) {
                    sb.append(spannableToHtml(text))
                }
            } else if (child is ImageView) {
                val path = child.tag as? String
                if (path != null) {
                    sb.append(IMG_MARK_OPEN).append(path).append(IMG_MARK_CLOSE)
                }
            }
        }
        return sb.toString()
    }

    private fun saveNoteAutomatically() {
        val rawTitle = etTitle.text.toString().trim()
        val body = collectAllBodyText().trim()
        val attachments = serializeAttachments()

        if (rawTitle.isEmpty() && body.isEmpty() && attachments.isEmpty()) return
        val title = if (rawTitle.isEmpty()) "Untitled Note" else rawTitle
        if (title == lastSavedTitle && body == lastSavedBody && attachments == lastSavedAttachments) return

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

            val target = activeEditText ?: etBody
            val text = target.text.toString()
            val cursorPosition = target.selectionStart

            var lineStart = 0
            if (cursorPosition > 0) {
                val lastNewLine = text.lastIndexOf('\n', cursorPosition - 1)
                lineStart = if (lastNewLine != -1) lastNewLine + 1 else 0
            }

            val bulletString = "\u2022 "
            val lineEnd = text.indexOf('\n', lineStart)
            val currentLine = if (lineEnd != -1) text.substring(lineStart, lineEnd) else text.substring(lineStart)

            if (isBullet) {
                if (!currentLine.startsWith(bulletString)) {
                    target.text.insert(lineStart, bulletString)
                }
            } else {
                if (currentLine.startsWith(bulletString)) {
                    target.text.delete(lineStart, lineStart + bulletString.length)
                }
            }
            target.requestFocus()
            if (isBullet && target.text.isEmpty()) {
                target.text.insert(0, bulletString)
            }
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
        val target = activeEditText ?: etBody
        val selectionStart = target.selectionStart
        val selectionEnd = target.selectionEnd
        if (selectionStart != selectionEnd) {
            applyFormattingToRange(target, selectionStart, selectionEnd)
        }
    }

    private fun applyFormattingToRange(target: EditText, start: Int, end: Int) {
        val spannable = target.text as Spannable
        val styleSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        for (span in styleSpans) spannable.removeSpan(span)
        val underlines = spannable.getSpans(start, end, UnderlineSpan::class.java)
        for (span in underlines) spannable.removeSpan(span)

        if (isBold) spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (isItalic) spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (isUnderline) spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        scheduleAutoSave()
    }

    private fun setupTextWatcher() {
        attachFormattingWatcher(etBody)
    }

    private fun attachFormattingWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            var wasEnterPressed = false
            var insertPos = -1
            var changeStart = -1
            var changeCount = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isBullet && count == 1 && s?.get(start) == '\n') {
                    wasEnterPressed = true
                    insertPos = start + 1
                }
                if (count > 0) {
                    changeStart = start
                    changeCount = count
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (wasEnterPressed) {
                    wasEnterPressed = false
                    s?.insert(insertPos, "\u2022 ")
                }

                if (s == null) return
                if (!isBold && !isItalic && !isUnderline) return

                val spannable = s as Spannable
                val length = s.length
                if (length == 0) return

                val applyStart = if (changeStart >= 0) changeStart else length - 1
                val applyEnd = if (changeStart >= 0 && changeCount > 0) changeStart + changeCount else length

                val safeStart = applyStart.coerceIn(0, length)
                val safeEnd = applyEnd.coerceIn(0, length)

                if (safeStart >= safeEnd) return

                val existingStyleSpans = spannable.getSpans(safeStart, safeEnd, StyleSpan::class.java)
                for (span in existingStyleSpans) spannable.removeSpan(span)
                val existingUnderlines = spannable.getSpans(safeStart, safeEnd, UnderlineSpan::class.java)
                for (span in existingUnderlines) spannable.removeSpan(span)

                if (isBold) spannable.setSpan(StyleSpan(Typeface.BOLD), safeStart, safeEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (isItalic) spannable.setSpan(StyleSpan(Typeface.ITALIC), safeStart, safeEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (isUnderline) spannable.setSpan(UnderlineSpan(), safeStart, safeEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                changeStart = -1
                changeCount = 0
            }
        })
    }

    private fun syncFormattingButtonsToCursor(editText: EditText) {
        val selStart = editText.selectionStart
        val selEnd = editText.selectionEnd
        if (selStart < 0) return
        val spannable = editText.text as? Spannable ?: return

        val checkStart = if (selStart != selEnd) selStart else if (selStart > 0) selStart - 1 else selStart
        val checkEnd = if (selStart != selEnd) selEnd else selStart

        val styleSpans = spannable.getSpans(checkStart, checkEnd, StyleSpan::class.java)
        isBold = styleSpans.any { it.style == Typeface.BOLD }
        isItalic = styleSpans.any { it.style == Typeface.ITALIC }
        isUnderline = spannable.getSpans(checkStart, checkEnd, UnderlineSpan::class.java).isNotEmpty()

        val text = editText.text.toString()
        val lastNewLine = if (selStart > 0) text.lastIndexOf('\n', selStart - 1) else -1
        val lineStart = if (lastNewLine != -1) lastNewLine + 1 else 0
        val lineEnd = text.indexOf('\n', lineStart)
        val currentLine = if (lineEnd != -1) text.substring(lineStart, lineEnd) else text.substring(lineStart)
        isBullet = currentLine.startsWith("\u2022 ")

        updateButtonState(btnBold, isBold)
        updateButtonState(btnItalic, isItalic)
        updateButtonState(btnUnderline, isUnderline)
        updateButtonState(btnList, isBullet)
    }

    private fun addAttachment(path: String) {
        insertImageAtCursor(path)
        currentAttachments.add(path)
        scheduleAutoSave()
    }

    // Membuat ulang konten body (teks + gambar) sesuai urutan yang disimpan di "savedHtml".
    // Dipanggil pas note dibuka, supaya gambar dan teks di bawahnya tetap di posisi yang benar
    // (bukan semua teks ditumpuk di atas baru semua gambar di bawah).
    private fun rebuildBodyContent(savedHtml: String) {
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)

        for (i in mainContainer.childCount - 1 downTo 0) {
            val child = mainContainer.getChildAt(i)
            if (child is ImageView || (child is EditText && child !== etBody)) {
                mainContainer.removeViewAt(i)
            }
        }
        etBody.setText("")
        activeEditText = etBody

        if (savedHtml.isEmpty()) {
            return
        }

        if (!savedHtml.contains(IMG_MARK_OPEN)) {
            etBody.setText(htmlToSpannable(savedHtml))
            currentAttachments.forEach { path -> appendImageView(path) }
            return
        }

        val regex = Regex(Regex.escape(IMG_MARK_OPEN) + "(.*?)" + Regex.escape(IMG_MARK_CLOSE))
        var lastIndex = 0
        var isFirstTextSegment = true
        var lastTarget: EditText = etBody

        for (match in regex.findAll(savedHtml)) {
            val textPart = savedHtml.substring(lastIndex, match.range.first)
            lastTarget = if (isFirstTextSegment) {
                etBody.setText(htmlToSpannable(textPart))
                etBody
            } else {
                appendBodyEditText(textPart)
            }
            isFirstTextSegment = false

            val path = match.groupValues[1]
            if (File(path).exists()) {
                appendImageView(path)
            }
            lastIndex = match.range.last + 1
        }

        val remaining = savedHtml.substring(lastIndex)
        lastTarget = if (isFirstTextSegment) {
            etBody.setText(htmlToSpannable(remaining))
            etBody
        } else {
            appendBodyEditText(remaining)
        }

        activeEditText = lastTarget
    }

    // Bikin EditText baru buat teks setelah gambar, lengkap dengan semua watcher
    // (format, auto save), terus ditempel di akhir mainContainer.
    private fun appendBodyEditText(initialHtml: String): EditText {
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val newEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(16), dp(0), dp(16), dp(0))
            }
            minHeight = dp(0)
            background = null
            hint = ""
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            gravity = android.view.Gravity.TOP
            textSize = 16f
            setPadding(dp(12), dp(4), dp(12), dp(0))

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeEditText = this
                    syncFormattingButtonsToCursor(this)
                }
            }

            setOnTouchListener { _, _ ->
                postDelayed({
                    syncFormattingButtonsToCursor(this)
                }, 100)
                false
            }
        }

        mainContainer.addView(newEditText)
        attachFormattingWatcher(newEditText)
        newEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateNoteMeta()
                scheduleAutoSave()
            }
        })

        if (initialHtml.isNotEmpty()) {
            newEditText.setText(htmlToSpannable(initialHtml))
        }
        return newEditText
    }

    // Bikin ImageView buat satu attachment dan ditempel di akhir mainContainer.
    private fun appendImageView(path: String) {
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(16), dp(0), dp(16), dp(4))
            }
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageURI(Uri.fromFile(File(path)))
            tag = path
            setOnLongClickListener {
                confirmRemoveAttachment(path)
                true
            }
        }
        mainContainer.addView(imageView)
    }

    private fun confirmRemoveAttachment(path: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove attachment?")
            .setMessage("This photo or drawing will be removed from this note.")
            .setPositiveButton("Remove") { _, _ ->
                val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
                for (i in mainContainer.childCount - 1 downTo 0) {
                    val child = mainContainer.getChildAt(i)
                    if (child is ImageView && child.tag == path) {
                        mainContainer.removeViewAt(i)
                    }
                }
                currentAttachments.remove(path)
                updateNoteMeta()
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
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val paths = mutableListOf<String>()
        for (i in 0 until mainContainer.childCount) {
            val child = mainContainer.getChildAt(i)
            if (child is ImageView) {
                (child.tag as? String)?.let { paths.add(it) }
            }
        }
        return paths.joinToString("\n")
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

    private fun insertImageAtCursor(path: String) {
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val currentEditText = activeEditText ?: etBody

        val cursorPosition = currentEditText.selectionStart.coerceAtLeast(0)
        val fullText = currentEditText.text.toString()

        val textAfterCursor = fullText.substring(cursorPosition)

        if (cursorPosition < currentEditText.text.length) {
            currentEditText.text.delete(cursorPosition, currentEditText.text.length)
        }
        currentEditText.setSelection(currentEditText.text.length)

        val currentIndex = mainContainer.indexOfChild(currentEditText)

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(16), dp(0), dp(16), dp(4))
            }
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageURI(Uri.fromFile(File(path)))
            tag = path
            setOnLongClickListener {
                confirmRemoveAttachment(path)
                true
            }
        }

        val nextEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(16), dp(0), dp(16), dp(0))
            }
            minHeight = dp(0)
            background = null
            hint = ""
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            gravity = android.view.Gravity.TOP
            textSize = 16f
            setPadding(dp(12), dp(4), dp(12), dp(0))

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activeEditText = this
                    syncFormattingButtonsToCursor(this)
                }
            }

            setOnTouchListener { _, _ ->
                postDelayed({
                    syncFormattingButtonsToCursor(this)
                }, 100)
                false
            }
        }

        mainContainer.addView(imageView, currentIndex + 1)
        mainContainer.addView(nextEditText, currentIndex + 2)

        activeEditText = nextEditText
        attachFormattingWatcher(nextEditText)

        // Pasang watcher auto save ke nextEditText juga
        nextEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateNoteMeta()
                scheduleAutoSave()
            }
        })

        isBold = false
        isItalic = false
        isUnderline = false
        isBullet = false
        updateButtonState(btnBold, false)
        updateButtonState(btnItalic, false)
        updateButtonState(btnUnderline, false)
        updateButtonState(btnList, false)

        nextEditText.setText(textAfterCursor)
        nextEditText.setSelection(0)
        nextEditText.requestFocus()
    }

    private fun spannableToHtml(text: android.text.Spanned): String {
        val sb = StringBuilder()
        val str = text.toString()
        var i = 0
        while (i < str.length) {
            val c = str[i]
            val boldSpans = text.getSpans(i, i + 1, StyleSpan::class.java)
                .filter { it.style == Typeface.BOLD }
            val italicSpans = text.getSpans(i, i + 1, StyleSpan::class.java)
                .filter { it.style == Typeface.ITALIC }
            val underlineSpans = text.getSpans(i, i + 1, UnderlineSpan::class.java)

            val isBoldHere = boldSpans.isNotEmpty()
            val isItalicHere = italicSpans.isNotEmpty()
            val isUnderlineHere = underlineSpans.isNotEmpty()

            if (isBoldHere) sb.append("<b>")
            if (isItalicHere) sb.append("<i>")
            if (isUnderlineHere) sb.append("<u>")

            when (c) {
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '&' -> sb.append("&amp;")
                '\n' -> sb.append("<br>")
                else -> sb.append(c)
            }

            if (isUnderlineHere) sb.append("</u>")
            if (isItalicHere) sb.append("</i>")
            if (isBoldHere) sb.append("</b>")

            i++
        }
        return sb.toString()
    }

    private fun htmlToSpannable(html: String): android.text.Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(html)
        }
    }
}
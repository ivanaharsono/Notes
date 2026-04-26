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

class DetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnBold: MaterialButton
    private lateinit var btnItalic: MaterialButton
    private lateinit var btnUnderline: MaterialButton
    private lateinit var btnList: MaterialButton
    private lateinit var btnToggleFormat: ImageButton
    private lateinit var formatScrollView: HorizontalScrollView

    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var isBullet = false

    private lateinit var colorActive: ColorStateList
    private lateinit var colorInactive: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        colorActive = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.IndigoBlue))
        colorInactive = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_text))

        setupViews()
        setupToolbar()
        setupFormattingButtons()
        setupToggleFormatting()
        setupSaveButton()
        setupTextWatcher()

        val note = intent.getSerializableExtra("EXTRA_NOTE") as? Note

        if (note != null) {
            etTitle.setText(note.judul)
            etBody.setText(note.isi)

            toolbar.title = "Edit Note"
        }
    }

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

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
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
    }

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
    }

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

    private fun setupToggleFormatting() {
        btnToggleFormat.setOnClickListener {
            formatScrollView.visibility = if (formatScrollView.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun setupSaveButton() {
        findViewById<View>(R.id.btnSave).setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
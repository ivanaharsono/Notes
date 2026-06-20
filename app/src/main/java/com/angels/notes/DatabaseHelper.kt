package com.angels.notes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "NotesDB.db"
        private const val TABLE_NOTES = "notes"

        private const val KEY_ID = "id"
        private const val KEY_JUDUL = "judul"
        private const val KEY_ISI = "isi"
        private const val KEY_TANGGAL = "tanggal"
        private const val KEY_IS_ARCHIVED = "is_archived"
        private const val KEY_IS_TRASHED = "is_trashed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNotesTable = ("CREATE TABLE " + TABLE_NOTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_JUDUL + " TEXT,"
                + KEY_ISI + " TEXT,"
                + KEY_TANGGAL + " TEXT,"
                + KEY_IS_ARCHIVED + " INTEGER DEFAULT 0,"
                + KEY_IS_TRASHED + " INTEGER DEFAULT 0" + ")")
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    fun addNote(note: Note): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_JUDUL, note.judul)
        values.put(KEY_ISI, note.isi)
        values.put(KEY_TANGGAL, note.tanggal)
        values.put(KEY_IS_ARCHIVED, note.isArchived)
        values.put(KEY_IS_TRASHED, note.isTrashed)

        val success = db.insert(TABLE_NOTES, null, values)
        db.close()
        return success
    }

    fun getAllNotes(): ArrayList<Note> {
        val noteList = ArrayList<Note>()
        val selectQuery = "SELECT * FROM $TABLE_NOTES WHERE $KEY_IS_ARCHIVED = 0 AND $KEY_IS_TRASHED = 0 ORDER BY $KEY_ID DESC"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val note = Note(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    judul = cursor.getString(cursor.getColumnIndexOrThrow(KEY_JUDUL)),
                    isi = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ISI)),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TANGGAL)),
                    isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ARCHIVED)),
                    isTrashed = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_TRASHED))
                )
                noteList.add(note)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return noteList
    }

    fun getArchivedNotes(): ArrayList<Note> {
        val noteList = ArrayList<Note>()
        val selectQuery = "SELECT * FROM $TABLE_NOTES WHERE $KEY_IS_ARCHIVED = 1 AND $KEY_IS_TRASHED = 0 ORDER BY $KEY_ID DESC"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val note = Note(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    judul = cursor.getString(cursor.getColumnIndexOrThrow(KEY_JUDUL)),
                    isi = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ISI)),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TANGGAL)),
                    isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ARCHIVED)),
                    isTrashed = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_TRASHED))
                )
                noteList.add(note)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return noteList
    }

    fun getTrashedNotes(): ArrayList<Note> {
        val noteList = ArrayList<Note>()
        val selectQuery = "SELECT * FROM $TABLE_NOTES WHERE $KEY_IS_TRASHED = 1 ORDER BY $KEY_ID DESC"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val note = Note(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    judul = cursor.getString(cursor.getColumnIndexOrThrow(KEY_JUDUL)),
                    isi = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ISI)),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TANGGAL)),
                    isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ARCHIVED)),
                    isTrashed = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_TRASHED))
                )
                noteList.add(note)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return noteList
    }

    fun updateNote(note: Note): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_JUDUL, note.judul)
        values.put(KEY_ISI, note.isi)
        values.put(KEY_TANGGAL, note.tanggal)
        values.put(KEY_IS_ARCHIVED, note.isArchived)
        values.put(KEY_IS_TRASHED, note.isTrashed)

        val success = db.update(TABLE_NOTES, values, "$KEY_ID=?", arrayOf(note.id.toString()))
        db.close()
        return success
    }

    fun updateNoteStatus(id: Int, isArchived: Int, isTrashed: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_IS_ARCHIVED, isArchived)
        values.put(KEY_IS_TRASHED, isTrashed)

        val success = db.update(TABLE_NOTES, values, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return success
    }

    fun deleteNotePermanently(id: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_NOTES, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return success
    }

    fun emptyTrash(): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_NOTES, "$KEY_IS_TRASHED=?", arrayOf("1"))
        db.close()
        return success
    }
}
package com.example.tp2apirest

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "GestionEtudiants.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME   = "students"
        const val COLUMN_ID      = "id"
        const val COLUMN_NAME    = "nom"
        const val COLUMN_EMAIL   = "email"
        const val COLUMN_FILIERE = "filiere"
    }

    // ── Appelée une seule fois à la création physique de la base ──────────────
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID      INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME    TEXT NOT NULL,
                $COLUMN_EMAIL   TEXT NOT NULL,
                $COLUMN_FILIERE TEXT NOT NULL
            )
        """
        db?.execSQL(createTableQuery)
    }

    // ── Appelée si DATABASE_VERSION est incrémentée ───────────────────────────
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // ── 1. CREATE ─────────────────────────────────────────────────────────────
    fun insertStudent(nom: String, email: String, filiere: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME,    nom)
            put(COLUMN_EMAIL,   email)
            put(COLUMN_FILIERE, filiere)
        }
        val insertedId = db.insert(TABLE_NAME, null, values)
        db.close()
        return insertedId   // Renvoie l'ID généré, ou -1 en cas d'échec
    }

    // ── 2. READ ───────────────────────────────────────────────────────────────
    fun getAllStudents(): List<Student> {
        val studentList = ArrayList<Student>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_NAME ASC"
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idIndex      = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val nameIndex    = cursor.getColumnIndexOrThrow(COLUMN_NAME)
            val emailIndex   = cursor.getColumnIndexOrThrow(COLUMN_EMAIL)
            val filiereIndex = cursor.getColumnIndexOrThrow(COLUMN_FILIERE)
            do {
                studentList.add(
                    Student(
                        cursor.getInt(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(emailIndex),
                        cursor.getString(filiereIndex)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor?.close()
        db.close()
        return studentList
    }

    // ── 3. UPDATE ─────────────────────────────────────────────────────────────
    fun updateStudent(student: Student): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME,    student.nom)
            put(COLUMN_EMAIL,   student.email)
            put(COLUMN_FILIERE, student.filiere)
        }
        val rowsAffected = db.update(
            TABLE_NAME, values,
            "$COLUMN_ID = ?", arrayOf(student.id.toString())
        )
        db.close()
        return rowsAffected
    }

    // ── 4. DELETE ─────────────────────────────────────────────────────────────
    fun deleteStudent(id: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?", arrayOf(id.toString())
        )
        db.close()
        return rowsDeleted
    }

    // ── 5. SEARCH (clause LIKE) ───────────────────────────────────────────────
    fun searchStudents(query: String): List<Student> {
        val studentList = ArrayList<Student>()
        val db = this.readableDatabase
        val searchQuery = """
            SELECT * FROM $TABLE_NAME
            WHERE $COLUMN_NAME    LIKE ?
               OR $COLUMN_FILIERE LIKE ?
            ORDER BY $COLUMN_NAME ASC
        """
        val pattern = "%$query%"
        val cursor: Cursor? = db.rawQuery(searchQuery, arrayOf(pattern, pattern))

        if (cursor != null && cursor.moveToFirst()) {
            val idIndex      = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val nameIndex    = cursor.getColumnIndexOrThrow(COLUMN_NAME)
            val emailIndex   = cursor.getColumnIndexOrThrow(COLUMN_EMAIL)
            val filiereIndex = cursor.getColumnIndexOrThrow(COLUMN_FILIERE)
            do {
                studentList.add(
                    Student(
                        cursor.getInt(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(emailIndex),
                        cursor.getString(filiereIndex)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor?.close()
        db.close()
        return studentList
    }
}
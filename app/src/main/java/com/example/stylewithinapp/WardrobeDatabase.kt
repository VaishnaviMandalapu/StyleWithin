package com.example.stylewithinapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// This class creates and manages the local wardrobe database.
class WardrobeDatabase(context: Context) :
    SQLiteOpenHelper(context, "stylewithin.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        // Each clothing item gets its own row.
        db.execSQL(
            """
            CREATE TABLE clothing (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                colour TEXT NOT NULL,
                photoPath TEXT,
                style TEXT NOT NULL DEFAULT 'Casual'
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE clothing ADD COLUMN style TEXT NOT NULL DEFAULT 'Casual'"
            )
        }
    }

    // Save one clothing item.
    fun addClothing(
        name: String,
        category: String,
        colour: String,
        photoPath: String?,
        style: String
    ): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("category", category)
            put("colour", colour)
            put("photoPath", photoPath)
            put("style", style)
        }

        return writableDatabase.insert("clothing", null, values)
    }

    // Load all saved clothing items.
    fun getAllClothing(): List<ClothingRecord> {
        val items = mutableListOf<ClothingRecord>()

        val cursor = readableDatabase.rawQuery(
            "SELECT id, name, category, colour, photoPath, style FROM clothing",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    ClothingRecord(
                        id = it.getLong(0),
                        name = it.getString(1),
                        category = it.getString(2),
                        colour = it.getString(3),
                        photoPath = it.getString(4),
                        style = it.getString(5)
                    )
                )
            }
        }

        return items
    }

    // Remove an item from the wardrobe.
    fun deleteClothing(id: Long): Int {
        return writableDatabase.delete(
            "clothing",
            "id = ?",
            arrayOf(id.toString())
        )
    }
}

// Simple data model returned by the database.
data class ClothingRecord(
    val id: Long,
    val name: String,
    val category: String,
    val colour: String,
    val photoPath: String?,
    val style: String
)
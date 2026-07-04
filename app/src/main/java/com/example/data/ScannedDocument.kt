package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_documents")
data class ScannedDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val filePath: String,
    val pageCount: Int,
    val fileSize: Long,
    val timestamp: Long = System.currentTimeMillis()
)

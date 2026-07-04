package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM scanned_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<ScannedDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: ScannedDocument): Long

    @Update
    suspend fun updateDocument(document: ScannedDocument)

    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("SELECT * FROM scanned_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): ScannedDocument?
}

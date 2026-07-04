package com.example.data

import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {
    val allDocuments: Flow<List<ScannedDocument>> = documentDao.getAllDocuments()

    suspend fun insertDocument(document: ScannedDocument): Long {
        return documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: ScannedDocument) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocumentById(id: Long) {
        documentDao.deleteDocumentById(id)
    }

    suspend fun getDocumentById(id: Long): ScannedDocument? {
        return documentDao.getDocumentById(id)
    }
}

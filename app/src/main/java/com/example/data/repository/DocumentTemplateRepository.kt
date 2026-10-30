package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.DocumentTemplateDao
import com.example.data.local.entity.DocumentTemplateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DocumentTemplateRepository(
    private val dao: DocumentTemplateDao
) {
    constructor(context: Context) : this(
        AppDatabase.getInstance(context).documentTemplateDao()
    )

    val allTemplates: Flow<List<DocumentTemplateEntity>> =
        dao.getAllTemplates().flowOn(Dispatchers.IO)

    fun getTemplatesByType(docType: String): Flow<List<DocumentTemplateEntity>> =
        dao.getTemplatesByType(docType).flowOn(Dispatchers.IO)

    fun searchTemplates(query: String): Flow<List<DocumentTemplateEntity>> =
        dao.searchTemplates(query).flowOn(Dispatchers.IO)

    suspend fun getTemplateById(id: Int): DocumentTemplateEntity? = withContext(Dispatchers.IO) {
        dao.getTemplateById(id)
    }

    suspend fun saveTemplate(template: DocumentTemplateEntity): Long = withContext(Dispatchers.IO) {
        dao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: DocumentTemplateEntity) = withContext(Dispatchers.IO) {
        dao.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: DocumentTemplateEntity) = withContext(Dispatchers.IO) {
        dao.deleteTemplate(template)
    }

    suspend fun deleteTemplateById(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun seedDefaultsIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            dao.insertAll(AppDatabase.getPrepopulatedTemplates())
        }
    }
}

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DocumentTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentTemplateDao {

    @Query("SELECT * FROM document_templates ORDER BY isBuiltIn DESC, createdAt DESC")
    fun getAllTemplates(): Flow<List<DocumentTemplateEntity>>

    @Query("SELECT * FROM document_templates WHERE docType = :docType ORDER BY isBuiltIn DESC, createdAt DESC")
    fun getTemplatesByType(docType: String): Flow<List<DocumentTemplateEntity>>

    @Query("SELECT * FROM document_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Int): DocumentTemplateEntity?

    @Query("SELECT * FROM document_templates WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR jurisdiction LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY isBuiltIn DESC, createdAt DESC")
    fun searchTemplates(query: String): Flow<List<DocumentTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: DocumentTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<DocumentTemplateEntity>)

    @Update
    suspend fun updateTemplate(template: DocumentTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: DocumentTemplateEntity)

    @Query("DELETE FROM document_templates WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM document_templates")
    suspend fun getCount(): Int
}

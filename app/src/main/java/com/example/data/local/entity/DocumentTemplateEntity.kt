package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_templates")
data class DocumentTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val docType: String,
    val jurisdiction: String,
    val description: String,
    val content: String,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

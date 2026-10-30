package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AuditRecord
import com.example.data.model.LegalDocument
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LegalDocumentRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(createFirestoreInstance(context))

    companion object {
        private fun createFirestoreInstance(context: Context): FirebaseFirestore {
            val appContext = context.applicationContext
            val app = if (com.google.firebase.FirebaseApp.getApps(appContext).isNotEmpty()) {
                com.google.firebase.FirebaseApp.getInstance()
            } else {
                com.google.firebase.FirebaseApp.initializeApp(appContext)
                    ?: com.google.firebase.FirebaseApp.getInstance()
            }
            return FirebaseFirestore.getInstance(
                app,
                appContext.getString(R.string.firestore_database_id)
            )
        }
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before accessing legal documents.")
    }

    fun observeDocuments(): Flow<List<LegalDocument>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/documents"
        emitAll(
            db.collection("users").document(uid).collection("documents")
                .snapshots()
                .map { snapshot -> snapshot.toObjects<LegalDocument>() }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getDocumentById(documentId: String): Result<LegalDocument?> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("documents").document(documentId)
        return try {
            val snapshot = docRef.get().await()
            Result.success(snapshot.toObject(LegalDocument::class.java))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createDocument(
        title: String,
        docType: String,
        jurisdiction: String,
        matterRef: String,
        content: String
    ): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("documents")
        val docRef = colRef.document()
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "title" to title,
            "docType" to docType,
            "jurisdiction" to jurisdiction,
            "content" to content,
            "status" to "DRAFT",
            "complianceScore" to 0L,
            "lastAuditSummary" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (matterRef.isNotBlank()) {
            payload["matterRef"] = matterRef
        }

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateDocument(document: LegalDocument): Result<Unit> {
        val uid = requireUserId()
        require(document.id.isNotBlank()) { "Document ID must not be blank for update" }
        requireNotNull(document.createdAt) { "Document createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("documents").document(document.id)

        val updates = mutableMapOf<String, Any>(
            "title" to document.title,
            "docType" to document.docType,
            "jurisdiction" to document.jurisdiction,
            "content" to document.content,
            "status" to document.status,
            "complianceScore" to document.complianceScore,
            "lastAuditSummary" to document.lastAuditSummary,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (document.matterRef.isNotBlank()) {
            updates["matterRef"] = document.matterRef
        }

        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(documentId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("documents").document(documentId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveAuditRecord(
        documentId: String,
        complianceScore: Long,
        overallAssessment: String,
        riskLevel: String,
        issues: List<String>,
        citations: List<String>,
        recommendations: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("documents").document(documentId).collection("audits")
        val auditRef = colRef.document()
        val payload = mapOf(
            "userId" to uid,
            "documentId" to documentId,
            "complianceScore" to complianceScore,
            "overallAssessment" to overallAssessment,
            "riskLevel" to riskLevel,
            "issues" to issues,
            "citations" to citations,
            "recommendations" to recommendations,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            auditRef.set(payload).await()
            // Also update parent document with latest score and audit summary
            val docRef = db.collection("users").document(uid).collection("documents").document(documentId)
            val newStatus = if (complianceScore >= 80) "COMPLIANT" else if (riskLevel == "HIGH") "FLAGGED" else "UNDER_REVIEW"
            docRef.update(
                mapOf(
                    "complianceScore" to complianceScore,
                    "lastAuditSummary" to overallAssessment,
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(auditRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    fun observeAudits(documentId: String): Flow<List<AuditRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/documents/$documentId/audits"
        emitAll(
            db.collection("users").document(uid).collection("documents").document(documentId).collection("audits")
                .snapshots()
                .map { snapshot -> snapshot.toObjects<AuditRecord>() }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }
}

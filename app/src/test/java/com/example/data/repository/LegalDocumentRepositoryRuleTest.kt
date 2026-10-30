package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.LegalDocument
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LegalDocumentRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createDocument_authenticated_success() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = LegalDocumentRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createDocument(
                title = "Motion for Preliminary Injunction",
                docType = "MOTION",
                jurisdiction = "US Federal",
                matterRef = "CASE-2026-99",
                content = "Plaintiff moves under Fed. R. Civ. P. 65 for an injunction..."
            )
        }
        assertTrue("Expected creation to succeed", result.isSuccess)
        val docId = result.getOrThrow()
        assertTrue("Expected non-empty docId", docId.isNotBlank())
    }

    @Test
    fun observeDocuments_authenticatedOwner_returnsDocuments() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = LegalDocumentRepository(firestore)

        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createDocument(
                title = "Non-Disclosure Agreement",
                docType = "NDA",
                jurisdiction = "Delaware",
                matterRef = "CORP-01",
                content = "Recitals and definitions of confidential material..."
            ).getOrThrow()
        }

        val docs = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeDocuments().first { list -> list.any { it.id == docId } }
        }
        assertTrue("Expected list to contain created document", docs.any { it.id == docId })
    }

    @Test
    fun getDocumentById_crossUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = LegalDocumentRepository(firestore)
        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createDocument(
                title = "Private Settlement Agreement",
                docType = "CONTRACT",
                jurisdiction = "New York",
                matterRef = "CONF-88",
                content = "Strictly confidential terms of settlement..."
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val exception: Exception? = try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("documents").document(docId).get().await()
            }
            null
        } catch (e: Exception) {
            e
        }
        assertNotNull("Cross user fetch on Alice's document path should throw exception", exception)
        val firestoreException = exception?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    @Test
    fun observeDocuments_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = LegalDocumentRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeDocuments().first()
            }
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            // Expected: IllegalStateException or Permission Denied
            assertTrue(e is IllegalStateException || e.unwrapFirestoreException() != null)
        }
    }

    @Test
    fun saveAuditRecord_authenticated_updatesParentAndAudit() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = LegalDocumentRepository(firestore)

        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createDocument(
                title = "Commercial Lease Agreement",
                docType = "CONTRACT",
                jurisdiction = "New York",
                matterRef = "LEASE-101",
                content = "Tenant shall pay monthly base rent and liquidated damages..."
            ).getOrThrow()
        }

        val auditResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveAuditRecord(
                documentId = docId,
                complianceScore = 88L,
                overallAssessment = "Liquidated damages clause conforms to Truck Rent-A-Center v. Puritan Farms standard.",
                riskLevel = "LOW",
                issues = listOf("Ensure grace period definition aligns with NY Gen Oblig Law"),
                citations = listOf("Truck Rent-A-Center, Inc. v. Puritan Farms 2nd, Inc., 41 N.Y.2d 420 (1977)"),
                recommendations = listOf("Specify pre-estimate proportionality")
            )
        }
        assertTrue("Audit record save should succeed", auditResult.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_lawyer@example.com"
        const val BOB_EMAIL = "bob_lawyer@example.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}

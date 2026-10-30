package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class LegalDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val docType: String = "CONTRACT",
    val jurisdiction: String = "General US",
    val matterRef: String = "",
    val content: String = "",
    val status: String = "DRAFT",
    val complianceScore: Long = 0L,
    val lastAuditSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val documentId: String = "",
    val complianceScore: Long = 0L,
    val overallAssessment: String = "",
    val riskLevel: String = "LOW",
    val issues: List<String> = emptyList(),
    val citations: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CaseCitation(
    val caseName: String = "",
    val citation: String = "",
    val courtAndYear: String = "",
    val keyPrinciple: String = "",
    val relevanceToDraft: String = ""
)

data class ComplianceIssue(
    val title: String = "",
    val severity: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val explanation: String = "",
    val offendingText: String = "",
    val suggestedFix: String = "",
    val supportingPrecedent: String = ""
)

data class ComplianceAuditReport(
    val complianceScore: Int = 0,
    val overallAssessment: String = "",
    val riskLevel: String = "LOW",
    val issues: List<ComplianceIssue> = emptyList(),
    val leadingPrecedents: List<CaseCitation> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class PrecedentAlert(
    val id: String = "",
    val documentId: String = "",
    val documentTitle: String = "",
    val caseName: String = "",
    val citation: String = "",
    val court: String = "",
    val changeType: String = "OVERRULED", // OVERRULED, MODIFIED, QUESTIONED, STATUTORY_SHIFT, DISTINGUISHED
    val summaryOfChange: String = "",
    val impactedClause: String = "",
    val suggestedRevision: String = "",
    val legalRationale: String = "",
    val isApplied: Boolean = false,
    val detectedAt: Long = System.currentTimeMillis()
)

data class WatchedDoctrine(
    val id: String = "",
    val name: String = "",
    val jurisdiction: String = "General US",
    val description: String = "",
    val recentDevelopments: String = "",
    val activeAlerts: Int = 0
)

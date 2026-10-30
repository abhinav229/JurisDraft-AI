package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CaseCitation
import com.example.data.model.ComplianceAuditReport
import com.example.data.model.PrecedentAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LegalPdfExporterTest {

    @Test
    fun exportToStream_generatesValidPdfHeader() {
        val outputStream = ByteArrayOutputStream()

        val sampleContent = """
            ARTICLE 1: DEFINITIONS
            1.1 "Proprietary Information" means all confidential and trade secret records.
            
            ARTICLE 2: OBLIGATIONS OF COUNSEL
            2.1 The parties agree to exercise ordinary care in protecting all disclosed materials.
            
            ARTICLE 3: GOVERNING JURISDICTION
            3.1 This Agreement is governed by Delaware law.
        """.trimIndent()

        val precedentAlerts = listOf(
            PrecedentAlert(
                caseName = "Edwards v. Arthur Andersen LLP",
                citation = "44 Cal. 4th 937 (2008)",
                court = "California Supreme Court",
                changeType = "RESTRICTIVE_COVENANT_BAR",
                summaryOfChange = "Invalidates non-compete agreements under Cal. Bus. & Prof. Code § 16600.",
                legalRationale = "Strict interpretation of employee mobility protections."
            )
        )

        val auditReport = ComplianceAuditReport(
            complianceScore = 95,
            overallAssessment = "High compliance with Delaware and Federal standards.",
            riskLevel = "LOW",
            leadingPrecedents = listOf(
                CaseCitation(
                    caseName = "Kahn v. M&F Worldwide Corp.",
                    citation = "88 A.3d 635 (Del. 2014)",
                    courtAndYear = "Del. 2014",
                    keyPrinciple = "Business judgment standard of review applied."
                )
            )
        )

        LegalPdfExporter.exportToStream(
            outputStream = outputStream,
            title = "Commercial Master Services Agreement",
            docType = "CONTRACT",
            jurisdiction = "Delaware",
            matterRef = "MATTER-2026-401",
            content = sampleContent,
            auditReport = auditReport,
            precedentAlerts = precedentAlerts,
            options = LegalExportOptions(
                includeAuditReport = true,
                includeWatermark = true,
                includeSignatureBlock = true,
                includePrecedentsAppendix = true
            )
        )

        val bytes = outputStream.toByteArray()
        assertTrue("Output PDF should not be empty", bytes.isNotEmpty())
        // PDF files always begin with the %PDF- header magic bytes
        val header = String(bytes.take(5).toByteArray())
        assertEquals("%PDF-", header)
    }

    @Test
    fun exportToCacheFile_createsValidFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = LegalPdfExporter.exportToCacheFile(
            context = context,
            title = "Mutual NDA",
            docType = "NDA",
            jurisdiction = "California",
            matterRef = "CORP-101",
            content = "1. Confidentiality provisions and reciprocal commitments.",
            options = LegalExportOptions(
                includeWatermark = false,
                includeSignatureBlock = true,
                includePrecedentsAppendix = false
            )
        )

        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        file.delete()
    }
}

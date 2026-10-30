package com.example.data.ai

import android.util.Log
import com.example.data.model.CaseCitation
import com.example.data.model.ComplianceAuditReport
import com.example.data.model.ComplianceIssue
import com.example.data.model.PrecedentAlert
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

private const val TAG = "LegalAiService"

class LegalAiService {

    private val auditSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "complianceScore" to Schema.integer(description = "Overall compliance score from 0 to 100"),
                "overallAssessment" to Schema.string(description = "Executive legal summary of findings and compliance posture"),
                "riskLevel" to Schema.enumeration(values = listOf("LOW", "MEDIUM", "HIGH")),
                "issues" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "title" to Schema.string(description = "Short title of the legal issue"),
                            "severity" to Schema.enumeration(values = listOf("HIGH", "MEDIUM", "LOW")),
                            "explanation" to Schema.string(description = "Detailed legal explanation why this risks non-compliance or unenforceability"),
                            "offendingText" to Schema.string(description = "Specific clause or phrase from the draft"),
                            "suggestedFix" to Schema.string(description = "Recommended replacement wording"),
                            "supportingPrecedent" to Schema.string(description = "Controlling case law citation or statute")
                        )
                    ),
                    description = "List of identified compliance issues and enforceability risks"
                ),
                "leadingPrecedents" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "caseName" to Schema.string(description = "Name of case, e.g. Celotex Corp. v. Catrett"),
                            "citation" to Schema.string(description = "Standard Bluebook citation, e.g. 477 U.S. 317 (1986)"),
                            "courtAndYear" to Schema.string(description = "Issuing court and year"),
                            "keyPrinciple" to Schema.string(description = "Core legal rule or doctrine established"),
                            "relevanceToDraft" to Schema.string(description = "How this precedent impacts the drafted text")
                        )
                    ),
                    description = "Controlling or persuasive case law citations relevant to the drafted document"
                ),
                "recommendations" to Schema.array(
                    items = Schema.string(),
                    description = "Strategic legal drafting recommendations for attorney review"
                )
            )
        )
    }

    private val precedentAlertSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "alerts" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "caseName" to Schema.string(description = "New controlling case or overturned authority, e.g. Loper Bright Enterprises v. Raimondo"),
                            "citation" to Schema.string(description = "Official legal citation, e.g. 144 S. Ct. 2244 (2024)"),
                            "court" to Schema.string(description = "Deciding court, e.g. U.S. Supreme Court, Cal. Supreme Court, Del. Ch."),
                            "changeType" to Schema.enumeration(values = listOf("OVERRULED", "MODIFIED", "QUESTIONED", "STATUTORY_SHIFT", "DISTINGUISHED")),
                            "summaryOfChange" to Schema.string(description = "Summary of how current case law shifted or overruled prior authority"),
                            "impactedClause" to Schema.string(description = "Extract of clause from user draft impacted by this precedent shift"),
                            "suggestedRevision" to Schema.string(description = "Enforceable replacement clause aligning with the new precedent"),
                            "legalRationale" to Schema.string(description = "Strategic legal rationale for the revision under the new doctrine")
                        )
                    ),
                    description = "Case law updates that invalidate or require revisions to clauses in the draft"
                )
            )
        )
    }

    private val auditModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = auditSchema
            temperature = 0.2f
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    private val precedentModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = precedentAlertSchema
            temperature = 0.2f
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    private val generalModel by lazy {
        val config = generationConfig {
            temperature = 0.3f
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    suspend fun auditLegalCompliance(
        title: String,
        docType: String,
        jurisdiction: String,
        content: String
    ): Result<ComplianceAuditReport> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a senior appellate attorney and judicial compliance officer specializing in commercial and procedural law.
                Conduct an exhaustive legal compliance and case law audit for the following draft:
                
                DOCUMENT TITLE: $title
                DOCUMENT TYPE: $docType
                JURISDICTION: $jurisdiction
                
                DRAFTED DOCUMENT CONTENT:
                \"\"\"
                $content
                \"\"\"
                
                AUDIT MANDATE:
                1. Verify strict compliance with applicable statutory frameworks, mandatory boilerplate, and procedural rules for $jurisdiction.
                2. Evaluate every clause against binding case law precedents and prevailing judicial doctrines (e.g. unconscionability, contra proferentem, consideration doctrine, specificity standards, penalty clause rules, limitation of liability carveouts like gross negligence/willful misconduct).
                3. Flag any clauses that are vulnerable to being struck down, rendered unenforceable, or exposing the client to undue litigation exposure.
                4. Provide controlling case law citations in proper Bluebook format with their key holdings and application to this text.
                5. Provide concrete, enforceable redrafted clauses to resolve every identified risk.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                auditModel.generateContent(prompt)
            }

            val jsonStr = response.text ?: throw IllegalStateException("Model returned empty response.")
            val root = JSONObject(jsonStr)

            val score = root.optInt("complianceScore", 75)
            val overallAssessment = root.optString("overallAssessment", "Audit complete.")
            val riskLevel = root.optString("riskLevel", "MEDIUM")

            val issuesList = mutableListOf<ComplianceIssue>()
            val issuesArray = root.optJSONArray("issues")
            if (issuesArray != null) {
                for (i in 0 until issuesArray.length()) {
                    val item = issuesArray.getJSONObject(i)
                    issuesList.add(
                        ComplianceIssue(
                            title = item.optString("title", "Issue ${i + 1}"),
                            severity = item.optString("severity", "MEDIUM"),
                            explanation = item.optString("explanation", ""),
                            offendingText = item.optString("offendingText", ""),
                            suggestedFix = item.optString("suggestedFix", ""),
                            supportingPrecedent = item.optString("supportingPrecedent", "")
                        )
                    )
                }
            }

            val precedentsList = mutableListOf<CaseCitation>()
            val precedentsArray = root.optJSONArray("leadingPrecedents")
            if (precedentsArray != null) {
                for (i in 0 until precedentsArray.length()) {
                    val item = precedentsArray.getJSONObject(i)
                    precedentsList.add(
                        CaseCitation(
                            caseName = item.optString("caseName", ""),
                            citation = item.optString("citation", ""),
                            courtAndYear = item.optString("courtAndYear", ""),
                            keyPrinciple = item.optString("keyPrinciple", ""),
                            relevanceToDraft = item.optString("relevanceToDraft", "")
                        )
                    )
                }
            }

            val recommendationsList = mutableListOf<String>()
            val recsArray = root.optJSONArray("recommendations")
            if (recsArray != null) {
                for (i in 0 until recsArray.length()) {
                    recommendationsList.add(recsArray.getString(i))
                }
            }

            Result.success(
                ComplianceAuditReport(
                    complianceScore = score,
                    overallAssessment = overallAssessment,
                    riskLevel = riskLevel,
                    issues = issuesList,
                    leadingPrecedents = precedentsList,
                    recommendations = recommendationsList
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Compliance audit failed", e)
            Result.failure(e)
        }
    }

    suspend fun scanDocumentForPrecedentChanges(
        documentId: String,
        documentTitle: String,
        docType: String,
        jurisdiction: String,
        content: String
    ): Result<List<PrecedentAlert>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are an authoritative legal Shepardizer and judicial intelligence analyst tracking recent case law shifts.
                Analyze the following legal document to detect clauses vulnerable to RECENT CASE LAW CHANGES, OVERRULED PRECEDENTS, or NEW STATUTORY ENACTMENTS in $jurisdiction:
                
                DOCUMENT TITLE: $documentTitle
                DOCUMENT TYPE: $docType
                JURISDICTION: $jurisdiction
                
                DOCUMENT CONTENT:
                \"\"\"
                $content
                \"\"\"
                
                ANALYSIS FOCUS:
                - Check for any reliance on doctrines modified or overturned by landmark recent decisions (e.g., Loper Bright overturning Chevron in administrative/regulatory contexts, Corner Post on APA limitations, recent Supreme Court arbitration decisions like Coinbase, recent Delaware Court of Chancery decisions on stockholder agreements like West Palm Beach Firefighters v. Moelis & Co. or Match Group MFW standards, California SB 699 / AB 1076 non-compete voidance, New York liquidated damages standards, FTC rule developments).
                - For each affected clause, identify the new case, change type, explain the impact, extract the exact impacted clause, and draft an enforceable replacement clause.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                precedentModel.generateContent(prompt)
            }
            val jsonStr = response.text ?: throw IllegalStateException("Model returned empty response.")
            val root = JSONObject(jsonStr)
            val alertsArray = root.optJSONArray("alerts") ?: return@withContext Result.success(emptyList())

            val list = mutableListOf<PrecedentAlert>()
            for (i in 0 until alertsArray.length()) {
                val item = alertsArray.getJSONObject(i)
                list.add(
                    PrecedentAlert(
                        id = UUID.randomUUID().toString(),
                        documentId = documentId,
                        documentTitle = documentTitle,
                        caseName = item.optString("caseName", "Controlling Precedent Shift"),
                        citation = item.optString("citation", ""),
                        court = item.optString("court", ""),
                        changeType = item.optString("changeType", "MODIFIED"),
                        summaryOfChange = item.optString("summaryOfChange", ""),
                        impactedClause = item.optString("impactedClause", ""),
                        suggestedRevision = item.optString("suggestedRevision", ""),
                        legalRationale = item.optString("legalRationale", ""),
                        isApplied = false,
                        detectedAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Precedent scan failed", e)
            Result.failure(e)
        }
    }

    suspend fun draftClause(
        clauseType: String,
        jurisdiction: String,
        favoredParty: String,
        instructions: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a master legal draftsman. Draft a precision, bulletproof contract clause or legal section:
                
                CLAUSE TYPE: $clauseType
                JURISDICTION: $jurisdiction
                FAVORED PARTY BIAS: $favoredParty (e.g. Neutral, Client-Favorable, Counterparty-Favorable)
                SPECIFIC INSTRUCTIONS / PARAMETERS: $instructions
                
                Drafting Requirements:
                - Output formal, modern legal prose ready for inclusion in contracts or court pleadings.
                - Ensure strict compliance with controlling case law precedents in $jurisdiction.
                - Include brief legal commentary below the clause explaining how it satisfies current judicial standards and case law precedents.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                generalModel.generateContent(prompt)
            }
            val text = response.text ?: throw IllegalStateException("Model returned empty draft.")
            Result.success(text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Clause drafting failed", e)
            Result.failure(e)
        }
    }

    suspend fun researchPrecedents(
        doctrineOrTopic: String,
        jurisdiction: String
    ): Result<List<CaseCitation>> = withContext(Dispatchers.IO) {
        try {
            val researchSchema = Schema.obj(
                properties = mapOf(
                    "precedents" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "caseName" to Schema.string(description = "Name of case"),
                                "citation" to Schema.string(description = "Standard Bluebook citation"),
                                "courtAndYear" to Schema.string(description = "Court and year"),
                                "keyPrinciple" to Schema.string(description = "Core doctrine or rule established"),
                                "relevanceToDraft" to Schema.string(description = "Drafting guidance and application")
                            )
                        )
                    )
                )
            )
            val model = Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = researchSchema
                    temperature = 0.2f
                }
            )

            val prompt = """
                Provide authoritative controlling and seminal case law precedents for legal drafting in:
                TOPIC / DOCTRINE: $doctrineOrTopic
                JURISDICTION: $jurisdiction
                
                Include leading Supreme Court, Federal Circuit, or state high court decisions that govern drafting standards and enforceability.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val jsonStr = response.text ?: throw IllegalStateException("Empty response")
            val root = JSONObject(jsonStr)
            val array = root.optJSONArray("precedents") ?: return@withContext Result.success(emptyList())

            val list = mutableListOf<CaseCitation>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    CaseCitation(
                        caseName = item.optString("caseName", ""),
                        citation = item.optString("citation", ""),
                        courtAndYear = item.optString("courtAndYear", ""),
                        keyPrinciple = item.optString("keyPrinciple", ""),
                        relevanceToDraft = item.optString("relevanceToDraft", "")
                    )
                )
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Precedent research failed", e)
            Result.failure(e)
        }
    }
}

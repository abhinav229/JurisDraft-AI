package com.example.ui.document

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.LegalAiService
import com.example.data.model.CaseCitation
import com.example.data.model.ComplianceAuditReport
import com.example.data.model.LegalDocument
import com.example.data.model.PrecedentAlert
import com.example.data.model.WatchedDoctrine
import com.example.data.repository.LegalDocumentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.example.util.LegalExportOptions
import com.example.util.LegalPdfExporter
import java.io.File
import java.util.UUID

private const val TAG = "DocumentViewModel"

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class DocumentViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: LegalDocumentRepository = LegalDocumentRepository(application),
    private val aiService: LegalAiService = LegalAiService()
) : AndroidViewModel(application) {

    // Observable documents from Firestore
    val documents: StateFlow<List<LegalDocument>> = repository.observeDocuments()
        .catch { error ->
            Log.e(TAG, "Error observing legal documents", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _selectedDocument = MutableStateFlow<LegalDocument?>(null)
    val selectedDocument: StateFlow<LegalDocument?> = _selectedDocument.asStateFlow()

    private val _draftTitle = MutableStateFlow("")
    val draftTitle: StateFlow<String> = _draftTitle.asStateFlow()

    private val _draftContent = MutableStateFlow("")
    val draftContent: StateFlow<String> = _draftContent.asStateFlow()

    private val _draftDocType = MutableStateFlow("CONTRACT")
    val draftDocType: StateFlow<String> = _draftDocType.asStateFlow()

    private val _draftJurisdiction = MutableStateFlow("General US")
    val draftJurisdiction: StateFlow<String> = _draftJurisdiction.asStateFlow()

    private val _draftMatterRef = MutableStateFlow("")
    val draftMatterRef: StateFlow<String> = _draftMatterRef.asStateFlow()

    private val _auditState = MutableStateFlow<UiState<ComplianceAuditReport>>(UiState.Idle)
    val auditState: StateFlow<UiState<ComplianceAuditReport>> = _auditState.asStateFlow()

    private val _precedentAlerts = MutableStateFlow<List<PrecedentAlert>>(emptyList())
    val precedentAlerts: StateFlow<List<PrecedentAlert>> = _precedentAlerts.asStateFlow()

    private val _precedentScanState = MutableStateFlow<UiState<List<PrecedentAlert>>>(UiState.Idle)
    val precedentScanState: StateFlow<UiState<List<PrecedentAlert>>> = _precedentScanState.asStateFlow()

    private val _clauseDraftState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val clauseDraftState: StateFlow<UiState<String>> = _clauseDraftState.asStateFlow()

    private val _researchState = MutableStateFlow<UiState<List<CaseCitation>>>(UiState.Idle)
    val researchState: StateFlow<UiState<List<CaseCitation>>> = _researchState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _exportPdfState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val exportPdfState: StateFlow<UiState<String>> = _exportPdfState.asStateFlow()

    private val _watchedDoctrines = MutableStateFlow<List<WatchedDoctrine>>(
        listOf(
            WatchedDoctrine(
                id = "doc_1",
                name = "Non-Compete & Restrictive Covenants",
                jurisdiction = "California & US Federal",
                description = "Monitoring Cal. Bus. & Prof. Code § 16600, SB 699, and judicial enforcement trends.",
                recentDevelopments = "SB 699 and AB 1076 strictly penalize out-of-state non-competes in California.",
                activeAlerts = 2
            ),
            WatchedDoctrine(
                id = "doc_2",
                name = "Administrative Regulatory Deference",
                jurisdiction = "US Federal",
                description = "Post-Chevron standard of judicial review under APA § 706.",
                recentDevelopments = "Loper Bright Enterprises v. Raimondo overrules Chevron deference; courts must exercise independent judgment.",
                activeAlerts = 1
            ),
            WatchedDoctrine(
                id = "doc_3",
                name = "Delaware Controlling Stockholder Transactions",
                jurisdiction = "Delaware",
                description = "MFW cleansing framework and entire fairness standard.",
                recentDevelopments = "Match Group decision confirms MFW framework applies to all conflicted controller transactions.",
                activeAlerts = 1
            ),
            WatchedDoctrine(
                id = "doc_4",
                name = "Liquidated Damages vs. Penalties",
                jurisdiction = "New York",
                description = "Proportionality standard and reasonable pre-estimate of damages.",
                recentDevelopments = "Truck Rent-A-Center v. Puritan Farms standard strictly enforces non-punitive proportionality.",
                activeAlerts = 0
            )
        )
    )
    val watchedDoctrines: StateFlow<List<WatchedDoctrine>> = _watchedDoctrines.asStateFlow()

    init {
        // Initialize default precedent alerts tracking current judicial changes
        _precedentAlerts.value = listOf(
            PrecedentAlert(
                id = "alert_1",
                documentId = "",
                documentTitle = "Executive Employment Agreement",
                caseName = "Edwards v. Arthur Andersen LLP & Cal. SB 699",
                citation = "Cal. Bus. & Prof. Code § 16600.5",
                court = "California Supreme Court / Legislature",
                changeType = "OVERRULED",
                summaryOfChange = "California enacted SB 699 and AB 1076, declaring non-compete agreements void and actionable regardless of where or when signed.",
                impactedClause = "Employee agrees that for twenty-four (24) months post-employment, Employee shall not directly or indirectly compete with the Company.",
                suggestedRevision = "Employee agrees to strictly safeguard Company Confidential Information and Trade Secrets pursuant to the California Uniform Trade Secrets Act (Cal. Civ. Code § 3426 et seq.). No geographic or market trade restriction is imposed.",
                legalRationale = "Traditional non-compete is legally void and exposes the employer to statutory civil penalties and attorney's fees under SB 699.",
                isApplied = false
            ),
            PrecedentAlert(
                id = "alert_2",
                documentId = "",
                documentTitle = "Federal Regulatory Motion & Brief",
                caseName = "Loper Bright Enterprises v. Raimondo",
                citation = "144 S. Ct. 2244 (2024)",
                court = "Supreme Court of the United States",
                changeType = "OVERRULED",
                summaryOfChange = "Overruled Chevron U.S.A. v. NRDC (1984). Federal courts may no longer defer to agency interpretations of ambiguous statutes.",
                impactedClause = "Pursuant to Chevron deference, this Court must uphold the Agency's interpretation as a permissible construction of the statute.",
                suggestedRevision = "Pursuant to the Administrative Procedure Act § 706 and Loper Bright Enterprises v. Raimondo, this Court must exercise independent judicial judgment to decide all relevant questions of law and determine the statute's definitive meaning without deference to agency interpretations.",
                legalRationale = "Citing Chevron deference is now fatal legal error in federal pleadings post-Loper Bright.",
                isApplied = false
            ),
            PrecedentAlert(
                id = "alert_3",
                documentId = "",
                documentTitle = "Merger & Acquisition Agreement",
                caseName = "In re Match Group, Inc. Derivative Litigation",
                citation = "315 A.3d 446 (Del. 2024)",
                court = "Delaware Supreme Court",
                changeType = "MODIFIED",
                summaryOfChange = "Delaware Supreme Court clarified that MFW framework applies to any transaction where a controlling stockholder receives a non-ratable benefit.",
                impactedClause = "The Special Committee approval shall conclusively establish the business judgment standard of review.",
                suggestedRevision = "The Transaction is conditioned ab initio upon both: (1) the negotiation and recommendation of a fully empowered, independent Special Committee of directors; and (2) the uncoerced, fully informed affirmative vote of a majority of the minority stockholders unaffiliated with Controller, satisfying In re MFW and Match Group.",
                legalRationale = "Single-prong special committee approval is insufficient to avoid entire fairness review under Match Group.",
                isApplied = false
            )
        )
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun selectDocument(document: LegalDocument?) {
        _selectedDocument.value = document
        if (document != null) {
            _draftTitle.value = document.title
            _draftDocType.value = document.docType
            _draftJurisdiction.value = document.jurisdiction
            _draftMatterRef.value = document.matterRef
            _draftContent.value = document.content
            _auditState.value = UiState.Idle
        } else {
            // New draft
            _draftTitle.value = ""
            _draftDocType.value = "CONTRACT"
            _draftJurisdiction.value = "General US"
            _draftMatterRef.value = ""
            _draftContent.value = ""
            _auditState.value = UiState.Idle
        }
    }

    fun updateDraftFields(
        title: String? = null,
        docType: String? = null,
        jurisdiction: String? = null,
        matterRef: String? = null,
        content: String? = null
    ) {
        title?.let { _draftTitle.value = it }
        docType?.let { _draftDocType.value = it }
        jurisdiction?.let { _draftJurisdiction.value = it }
        matterRef?.let { _draftMatterRef.value = it }
        content?.let { _draftContent.value = it }
    }

    fun saveDraft(onSaved: (String) -> Unit = {}) {
        val current = _selectedDocument.value
        val title = _draftTitle.value.ifBlank { "Untitled Legal Draft" }
        val docType = _draftDocType.value
        val jurisdiction = _draftJurisdiction.value
        val matterRef = _draftMatterRef.value
        val content = _draftContent.value

        viewModelScope.launch {
            if (current == null) {
                val result = repository.createDocument(title, docType, jurisdiction, matterRef, content)
                result.onSuccess { docId ->
                    _userMessage.value = "Document created successfully"
                    val created = LegalDocument(
                        id = docId,
                        title = title,
                        docType = docType,
                        jurisdiction = jurisdiction,
                        matterRef = matterRef,
                        content = content
                    )
                    _selectedDocument.value = created
                    onSaved(docId)
                }.onFailure { e ->
                    _userMessage.value = "Failed to create document: ${e.message}"
                }
            } else {
                val updated = current.copy(
                    title = title,
                    docType = docType,
                    jurisdiction = jurisdiction,
                    matterRef = matterRef,
                    content = content
                )
                val result = repository.updateDocument(updated)
                result.onSuccess {
                    _userMessage.value = "Draft saved successfully"
                    _selectedDocument.value = updated
                    onSaved(current.id)
                }.onFailure { e ->
                    _userMessage.value = "Failed to update draft: ${e.message}"
                }
            }
        }
    }

    fun deleteDocument(documentId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteDocument(documentId)
            result.onSuccess {
                if (_selectedDocument.value?.id == documentId) {
                    selectDocument(null)
                }
                _userMessage.value = "Document deleted"
                onDeleted()
            }.onFailure { e ->
                _userMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun runComplianceAudit() {
        val title = _draftTitle.value.ifBlank { "Legal Document" }
        val docType = _draftDocType.value
        val jurisdiction = _draftJurisdiction.value
        val content = _draftContent.value

        if (content.isBlank()) {
            _userMessage.value = "Please enter document text before running an audit."
            return
        }

        _auditState.value = UiState.Loading
        viewModelScope.launch {
            val result = aiService.auditLegalCompliance(title, docType, jurisdiction, content)
            result.onSuccess { report ->
                _auditState.value = UiState.Success(report)
                val current = _selectedDocument.value
                if (current != null && current.id.isNotBlank()) {
                    repository.saveAuditRecord(
                        documentId = current.id,
                        complianceScore = report.complianceScore.toLong(),
                        overallAssessment = report.overallAssessment,
                        riskLevel = report.riskLevel,
                        issues = report.issues.map { "${it.title}: ${it.explanation}" },
                        citations = report.leadingPrecedents.map { "${it.caseName}, ${it.citation} (${it.courtAndYear})" },
                        recommendations = report.recommendations
                    )
                }
            }.onFailure { e ->
                _auditState.value = UiState.Error(e.localizedMessage ?: "Compliance audit failed")
            }
        }
    }

    fun scanForPrecedentUpdates() {
        val current = _selectedDocument.value
        val docId = current?.id ?: ""
        val title = _draftTitle.value.ifBlank { "Legal Document" }
        val docType = _draftDocType.value
        val jurisdiction = _draftJurisdiction.value
        val content = _draftContent.value

        if (content.isBlank()) {
            _userMessage.value = "Please draft content to scan against recent case law precedents."
            return
        }

        _precedentScanState.value = UiState.Loading
        viewModelScope.launch {
            val result = aiService.scanDocumentForPrecedentChanges(docId, title, docType, jurisdiction, content)
            result.onSuccess { alerts ->
                _precedentScanState.value = UiState.Success(alerts)
                if (alerts.isNotEmpty()) {
                    val updatedList = (alerts + _precedentAlerts.value).distinctBy { it.caseName + it.impactedClause }
                    _precedentAlerts.value = updatedList
                    _userMessage.value = "Detected ${alerts.size} precedent shift(s) affecting this draft!"
                } else {
                    _userMessage.value = "All cited authorities and doctrines remain current under prevailing case law."
                }
            }.onFailure { e ->
                _precedentScanState.value = UiState.Error(e.localizedMessage ?: "Precedent scan failed")
            }
        }
    }

    fun applyPrecedentRevision(alert: PrecedentAlert) {
        val currentContent = _draftContent.value
        val impacted = alert.impactedClause.trim()
        val fix = alert.suggestedRevision.trim()

        val newContent = if (impacted.isNotBlank() && currentContent.contains(impacted)) {
            currentContent.replace(impacted, fix)
        } else {
            // Append or prepend amendment note
            "$currentContent\n\n/* REVISED PURSUANT TO ${alert.caseName}: */\n$fix"
        }

        _draftContent.value = newContent
        // Mark alert as applied
        _precedentAlerts.value = _precedentAlerts.value.map {
            if (it.id == alert.id || (it.caseName == alert.caseName && it.impactedClause == alert.impactedClause)) {
                it.copy(isApplied = true)
            } else {
                it
            }
        }
        _userMessage.value = "Applied precedent revision from ${alert.caseName} to draft!"
        saveDraft()
    }

    fun applyComplianceFix(offendingText: String, suggestedFix: String, supportingPrecedent: String = "") {
        val currentContent = _draftContent.value
        val target = offendingText.trim()
        val replacement = suggestedFix.trim()

        val newContent = if (target.isNotBlank() && currentContent.contains(target)) {
            currentContent.replace(target, replacement)
        } else {
            val authorityNote = if (supportingPrecedent.isNotBlank()) "/* REVISED PURSUANT TO $supportingPrecedent: */\n" else ""
            "$currentContent\n\n$authorityNote$replacement"
        }

        _draftContent.value = newContent
        _userMessage.value = "Applied compliance revision to legal draft"
        saveDraft()
    }

    fun draftClauseWithAi(clauseType: String, jurisdiction: String, favoredParty: String, instructions: String) {
        _clauseDraftState.value = UiState.Loading
        viewModelScope.launch {
            val result = aiService.draftClause(clauseType, jurisdiction, favoredParty, instructions)
            result.onSuccess { clauseText ->
                _clauseDraftState.value = UiState.Success(clauseText)
            }.onFailure { e ->
                _clauseDraftState.value = UiState.Error(e.localizedMessage ?: "Clause drafting failed")
            }
        }
    }

    fun insertDraftedClauseIntoContent(clauseText: String) {
        val separator = if (_draftContent.value.isBlank()) "" else "\n\n"
        _draftContent.value = _draftContent.value + separator + clauseText
        _userMessage.value = "Clause inserted into draft"
    }

    fun researchPrecedents(doctrine: String, jurisdiction: String) {
        _researchState.value = UiState.Loading
        viewModelScope.launch {
            val result = aiService.researchPrecedents(doctrine, jurisdiction)
            result.onSuccess { precedents ->
                _researchState.value = UiState.Success(precedents)
            }.onFailure { e ->
                _researchState.value = UiState.Error(e.localizedMessage ?: "Precedent research failed")
            }
        }
    }

    fun seedSampleDocumentIfEmpty() {
        if (documents.value.isEmpty()) {
            viewModelScope.launch {
                val sampleTitle = "Executive Confidentiality & Non-Disclosure Agreement"
                val sampleContent = """
THIS NON-DISCLOSURE AND RESTRICTIVE COVENANT AGREEMENT (the "Agreement") is entered into as of October 2026, by and between Apex Tech Innovations Inc. ("Company") and Jane Doe ("Executive").

1. Confidential Information.
"Confidential Information" shall include all proprietary technical, financial, and business data disclosed by the Company to Executive. Executive shall hold all Confidential Information in strict trust and confidence.

2. Restrictive Covenants and Non-Compete.
Executive agrees that during the term of employment and for a period of eighteen (18) months following termination of employment for any reason, Executive shall not, directly or indirectly, engage in, perform services for, or establish any business entity that competes with Company in any jurisdiction where Company operates.

3. Standard of Judicial Review and Severability.
Pursuant to prevailing commercial doctrine, if any provision of this Agreement is held to be invalid or unenforceable, such provision shall be severed, and the remaining provisions shall continue in full force and effect. The parties agree that the liquidated damages of ${'$'}500,000 for any breach represent a reasonable pre-estimate of loss.

4. Governing Law and Jurisdiction.
This Agreement shall be governed by and construed in accordance with the laws of the State of California, without regard to conflict of laws principles.
                """.trimIndent()

                repository.createDocument(
                    title = sampleTitle,
                    docType = "NDA",
                    jurisdiction = "California",
                    matterRef = "MATTER-702",
                    content = sampleContent
                )
            }
        }
    }

    fun getSuggestedExportFileName(): String {
        val raw = _draftTitle.value.ifBlank { "Legal_Draft" }
        val sanitized = raw.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(32)
        return "${sanitized}.pdf"
    }

    fun exportDraftToUri(
        context: Context,
        uri: Uri,
        options: LegalExportOptions = LegalExportOptions()
    ) {
        _exportPdfState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    _exportPdfState.value = UiState.Error("Unable to open destination file for writing.")
                    return@launch
                }

                outputStream.use { out ->
                    val auditReport = (_auditState.value as? UiState.Success)?.data
                    LegalPdfExporter.exportToStream(
                        outputStream = out,
                        title = _draftTitle.value,
                        docType = _draftDocType.value,
                        jurisdiction = _draftJurisdiction.value,
                        matterRef = _draftMatterRef.value,
                        content = _draftContent.value,
                        auditReport = auditReport,
                        precedentAlerts = _precedentAlerts.value,
                        options = options
                    )
                }

                _exportPdfState.value = UiState.Success("PDF exported successfully to device storage")
                _userMessage.value = "Draft exported as formatted legal PDF"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export draft to PDF URI", e)
                _exportPdfState.value = UiState.Error(e.localizedMessage ?: "Failed to export PDF")
            }
        }
    }

    fun exportDraftToCacheFile(
        context: Context,
        options: LegalExportOptions = LegalExportOptions()
    ): File? {
        return try {
            val auditReport = (_auditState.value as? UiState.Success)?.data
            val file = LegalPdfExporter.exportToCacheFile(
                context = context,
                title = _draftTitle.value,
                docType = _draftDocType.value,
                jurisdiction = _draftJurisdiction.value,
                matterRef = _draftMatterRef.value,
                content = _draftContent.value,
                auditReport = auditReport,
                precedentAlerts = _precedentAlerts.value,
                options = options
            )
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate cache PDF", e)
            _userMessage.value = "PDF generation failed: ${e.message}"
            null
        }
    }

    fun resetExportState() {
        _exportPdfState.value = UiState.Idle
    }
}

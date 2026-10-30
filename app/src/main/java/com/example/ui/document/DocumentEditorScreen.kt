package com.example.ui.document

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.ComplianceAuditReport
import com.example.data.model.ComplianceIssue
import com.example.data.model.LegalDocument
import com.example.data.model.PrecedentAlert
import com.example.util.LegalExportOptions
import com.example.ui.theme.ComplianceBlue
import com.example.ui.theme.ComplianceGreen
import com.example.ui.theme.ComplianceRiskRed
import com.example.ui.theme.ComplianceWarningYellow
import com.example.ui.theme.LegalCardBorder
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark
import com.example.ui.theme.LegalNavyPrimary
import com.example.ui.theme.LegalSlate
import com.example.ui.theme.LegalSlateLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    viewModel: DocumentViewModel,
    onNavigateToPrecedents: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draftTitle by viewModel.draftTitle.collectAsState()
    val draftDocType by viewModel.draftDocType.collectAsState()
    val draftJurisdiction by viewModel.draftJurisdiction.collectAsState()
    val draftMatterRef by viewModel.draftMatterRef.collectAsState()
    val draftContent by viewModel.draftContent.collectAsState()
    val selectedDoc by viewModel.selectedDocument.collectAsState()
    val auditState by viewModel.auditState.collectAsState()
    val precedentAlerts by viewModel.precedentAlerts.collectAsState()
    val precedentScanState by viewModel.precedentScanState.collectAsState()
    val clauseState by viewModel.clauseDraftState.collectAsState()
    val exportPdfState by viewModel.exportPdfState.collectAsState()
    val context = LocalContext.current

    var showAuditDialog by remember { mutableStateOf(false) }
    var showClauseDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var mobileSelectedTab by remember { mutableIntStateOf(0) } // 0: Editor, 1: AI Revisions Sidebar
    var pendingExportOptions by remember { mutableStateOf(LegalExportOptions()) }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportDraftToUri(context, uri, pendingExportOptions)
        }
    }

    val wordCount = remember(draftContent) {
        if (draftContent.isBlank()) 0 else draftContent.trim().split("\\s+".toRegex()).size
    }
    val charCount = remember(draftContent) { draftContent.length }

    val pendingAlertCount = remember(precedentAlerts) {
        precedentAlerts.count { !it.isApplied }
    }

    if (showAuditDialog) {
        ComplianceAuditDialog(
            auditState = auditState,
            onDismiss = { showAuditDialog = false }
        )
    }

    if (showClauseDialog) {
        ClauseDraftingDialog(
            clauseState = clauseState,
            initialJurisdiction = draftJurisdiction,
            onDraftClause = { type, jur, bias, inst ->
                viewModel.draftClauseWithAi(type, jur, bias, inst)
            },
            onInsertClause = { clause ->
                viewModel.insertDraftedClauseIntoContent(clause)
            },
            onDismiss = { showClauseDialog = false }
        )
    }

    if (showExportDialog) {
        ExportPdfDialog(
            title = draftTitle,
            docType = draftDocType,
            jurisdiction = draftJurisdiction,
            matterRef = draftMatterRef,
            wordCount = wordCount,
            charCount = charCount,
            exportState = exportPdfState,
            onDismiss = {
                showExportDialog = false
                viewModel.resetExportState()
            },
            onExportToDevice = { options ->
                pendingExportOptions = options
                val fileName = viewModel.getSuggestedExportFileName()
                createPdfLauncher.launch(fileName)
            },
            onSharePdf = { options ->
                val file = viewModel.exportDraftToCacheFile(context, options)
                if (file != null) {
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, draftTitle.ifBlank { "Legal Draft" })
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Attached legal document: '${draftTitle.ifBlank { "Legal Instrument" }}' (Matter: ${draftMatterRef.ifBlank { "Unassigned" }}), formatted via JurisDraft AI."
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Formatted Legal PDF"))
                    } catch (e: Exception) {
                        Log.e("DocumentEditorScreen", "Error sharing PDF", e)
                    }
                }
            },
            onPrintPdf = { options ->
                val file = viewModel.exportDraftToCacheFile(context, options)
                if (file != null) {
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(viewIntent, "Preview / Print PDF"))
                    } catch (e: Exception) {
                        Log.e("DocumentEditorScreen", "Error opening PDF", e)
                    }
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LegalNavyDark)
    ) {
        val isWideScreen = maxWidth >= 768.dp

        if (isWideScreen) {
            // Tablet / Desktop / Wide Screen: Side-by-side Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Pane: Document Metadata, Compliance Status Indicator & Legal Draft Text Area
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ComplianceStatusIndicator(
                        selectedDoc = selectedDoc,
                        auditState = auditState,
                        pendingAlertCount = pendingAlertCount,
                        onRunAudit = { viewModel.runComplianceAudit() },
                        onViewDetails = { showAuditDialog = true }
                    )

                    DocumentMetadataSection(
                        title = draftTitle,
                        docType = draftDocType,
                        jurisdiction = draftJurisdiction,
                        matterRef = draftMatterRef,
                        onTitleChange = { viewModel.updateDraftFields(title = it) },
                        onDocTypeChange = { viewModel.updateDraftFields(docType = it) },
                        onJurisdictionChange = { viewModel.updateDraftFields(jurisdiction = it) },
                        onMatterRefChange = { viewModel.updateDraftFields(matterRef = it) }
                    )

                    QuickClauseToolbar(
                        jurisdiction = draftJurisdiction,
                        onInsertClause = { viewModel.insertDraftedClauseIntoContent(it) },
                        onOpenAiClauseDrafter = { showClauseDialog = true },
                        onExportPdf = { showExportDialog = true }
                    )

                    LegalDraftTextArea(
                        content = draftContent,
                        wordCount = wordCount,
                        charCount = charCount,
                        onContentChange = { viewModel.updateDraftFields(content = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    )

                    EditorActionsBar(
                        onSave = { viewModel.saveDraft() },
                        onRunAudit = {
                            viewModel.runComplianceAudit()
                            showAuditDialog = true
                        },
                        onScanPrecedents = { viewModel.scanForPrecedentUpdates() },
                        onExportPdf = { showExportDialog = true },
                        onDelete = if (selectedDoc != null && selectedDoc!!.id.isNotBlank()) {
                            { viewModel.deleteDocument(selectedDoc!!.id) }
                        } else null,
                        isAuditLoading = auditState is UiState.Loading,
                        isPrecedentLoading = precedentScanState is UiState.Loading
                    )
                }

                // Right Pane: Dedicated AI-Suggested Case Law Revisions Sidebar
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("case_law_revisions_sidebar"),
                    colors = CardDefaults.cardColors(containerColor = LegalNavyPrimary),
                    border = BorderStroke(1.dp, LegalCardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AiCaseLawRevisionsSidebar(
                        precedentAlerts = precedentAlerts,
                        auditState = auditState,
                        isScanning = precedentScanState is UiState.Loading,
                        onScanPrecedents = { viewModel.scanForPrecedentUpdates() },
                        onApplyRevision = { alert -> viewModel.applyPrecedentRevision(alert) },
                        onApplyComplianceFix = { offending, fix, precedent ->
                            viewModel.applyComplianceFix(offending, fix, precedent)
                        }
                    )
                }
            }
        } else {
            // Compact Mobile: Header with Compliance Indicator, Segmented Switcher, and Editor / Sidebar Panes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Top Status Indicator for Compliance
                ComplianceStatusIndicator(
                    selectedDoc = selectedDoc,
                    auditState = auditState,
                    pendingAlertCount = pendingAlertCount,
                    onRunAudit = { viewModel.runComplianceAudit() },
                    onViewDetails = { showAuditDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mobile Segmented Switcher
                TabRow(
                    selectedTabIndex = mobileSelectedTab,
                    containerColor = LegalCardDark,
                    contentColor = LegalGoldAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[mobileSelectedTab]),
                            color = LegalGoldAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = mobileSelectedTab == 0,
                        onClick = { mobileSelectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Legal Draft", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_editor_mobile")
                    )

                    Tab(
                        selected = mobileSelectedTab == 1,
                        onClick = { mobileSelectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (pendingAlertCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = ComplianceRiskRed) {
                                                Text("$pendingAlertCount")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Revisions ($pendingAlertCount)", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_revisions_sidebar_mobile")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Mobile View
                if (mobileSelectedTab == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DocumentMetadataSection(
                            title = draftTitle,
                            docType = draftDocType,
                            jurisdiction = draftJurisdiction,
                            matterRef = draftMatterRef,
                            onTitleChange = { viewModel.updateDraftFields(title = it) },
                            onDocTypeChange = { viewModel.updateDraftFields(docType = it) },
                            onJurisdictionChange = { viewModel.updateDraftFields(jurisdiction = it) },
                            onMatterRefChange = { viewModel.updateDraftFields(matterRef = it) }
                        )

                        QuickClauseToolbar(
                            jurisdiction = draftJurisdiction,
                            onInsertClause = { viewModel.insertDraftedClauseIntoContent(it) },
                            onOpenAiClauseDrafter = { showClauseDialog = true },
                            onExportPdf = { showExportDialog = true }
                        )

                        LegalDraftTextArea(
                            content = draftContent,
                            wordCount = wordCount,
                            charCount = charCount,
                            onContentChange = { viewModel.updateDraftFields(content = it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        )

                        // Mobile dock alert if revisions pending
                        if (pendingAlertCount > 0) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ComplianceRiskRed.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, ComplianceRiskRed.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = ComplianceRiskRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$pendingAlertCount case law revision(s) require review",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { mobileSelectedTab = 1 },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LegalGoldAccent),
                                        border = BorderStroke(1.dp, LegalGoldAccent),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Review", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        EditorActionsBar(
                            onSave = { viewModel.saveDraft() },
                            onRunAudit = {
                                viewModel.runComplianceAudit()
                                showAuditDialog = true
                            },
                            onScanPrecedents = { viewModel.scanForPrecedentUpdates() },
                            onExportPdf = { showExportDialog = true },
                            onDelete = if (selectedDoc != null && selectedDoc!!.id.isNotBlank()) {
                                { viewModel.deleteDocument(selectedDoc!!.id) }
                            } else null,
                            isAuditLoading = auditState is UiState.Loading,
                            isPrecedentLoading = precedentScanState is UiState.Loading
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    // Mobile Sidebar Pane view
                    AiCaseLawRevisionsSidebar(
                        precedentAlerts = precedentAlerts,
                        auditState = auditState,
                        isScanning = precedentScanState is UiState.Loading,
                        onScanPrecedents = { viewModel.scanForPrecedentUpdates() },
                        onApplyRevision = { alert ->
                            viewModel.applyPrecedentRevision(alert)
                            mobileSelectedTab = 0 // Switch back to editor to show applied change
                        },
                        onApplyComplianceFix = { offending, fix, precedent ->
                            viewModel.applyComplianceFix(offending, fix, precedent)
                            mobileSelectedTab = 0
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Status Indicator for Legal Compliance
 * Displays real-time compliance score, risk level badge, precedent shift alerts, and quick audit trigger.
 */
@Composable
fun ComplianceStatusIndicator(
    selectedDoc: LegalDocument?,
    auditState: UiState<ComplianceAuditReport>,
    pendingAlertCount: Int,
    onRunAudit: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val score = when (auditState) {
        is UiState.Success -> auditState.data.complianceScore
        else -> selectedDoc?.complianceScore?.toInt() ?: 0
    }

    val hasAudited = (auditState is UiState.Success) || (selectedDoc != null && selectedDoc.complianceScore > 0)

    val (statusColor, statusTitle, statusSubtitle) = when {
        !hasAudited -> Triple(
            LegalSlateLight,
            "UNAUDITED DRAFT",
            "Run an AI case law audit to verify compliance and judicial enforceability."
        )
        score >= 80 -> Triple(
            ComplianceGreen,
            "HIGH COMPLIANCE ($score/100)",
            "Draft satisfies prevailing case law precedents and statutory boundaries."
        )
        score >= 60 -> Triple(
            ComplianceWarningYellow,
            "MODERATE RISK ($score/100)",
            "Potential ambiguities or minority doctrine exposure detected."
        )
        else -> Triple(
            ComplianceRiskRed,
            "CRITICAL PRECEDENT RISK ($score/100)",
            "Cites overruled precedents or unenforceable restrictive covenants."
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compliance_status_indicator"),
        colors = CardDefaults.cardColors(containerColor = LegalCardDark),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing or solid status badge icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                !hasAudited -> Icons.Default.Shield
                                score >= 80 -> Icons.Default.CheckCircle
                                score >= 60 -> Icons.Default.Warning
                                else -> Icons.Default.Warning
                            },
                            contentDescription = "Compliance Status Badge",
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            if (pendingAlertCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = ComplianceRiskRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "$pendingAlertCount PRECEDENT ALERTS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1)),
                            maxLines = 1
                        )
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasAudited) {
                        OutlinedButton(
                            onClick = onViewDetails,
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LegalGoldAccent),
                            border = BorderStroke(1.dp, LegalGoldAccent.copy(alpha = 0.5f))
                        ) {
                            Text("Report", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onRunAudit,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("run_audit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalGoldAccent,
                            contentColor = LegalNavyDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        enabled = auditState !is UiState.Loading
                    ) {
                        if (auditState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = LegalNavyDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Balance, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (hasAudited) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (score / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = LegalSlate.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Text Area Component for Legal Drafts
 */
@Composable
fun LegalDraftTextArea(
    content: String,
    wordCount: Int,
    charCount: Int,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Legal Draft Text Area", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "$wordCount words • $charCount chars",
                        color = LegalGoldAccent,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            placeholder = {
                Text(
                    text = "Draft contracts, clauses, briefs, or motions here...\n\nExample:\n\"1. Restrictive Covenants: Employee agrees not to compete in California...\"\n\"2. Standard of Review: The Court should defer to the agency under Chevron...\"",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("document_content_editor")
                .testTag("legal_draft_text_area"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                lineHeight = 22.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LegalGoldAccent,
                unfocusedBorderColor = LegalCardBorder,
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                cursorColor = LegalGoldAccent
            )
        )
    }
}

/**
 * Sidebar for AI-Suggested Case Law Revisions
 * Displays all precedent shifts, statutory changes, and audit-suggested clause modifications with 1-tap apply.
 */
@Composable
fun AiCaseLawRevisionsSidebar(
    precedentAlerts: List<PrecedentAlert>,
    auditState: UiState<ComplianceAuditReport>,
    isScanning: Boolean,
    onScanPrecedents: () -> Unit,
    onApplyRevision: (PrecedentAlert) -> Unit,
    onApplyComplianceFix: (offending: String, fix: String, precedent: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, APPLIED

    val auditIssues = when (auditState) {
        is UiState.Success -> auditState.data.issues
        else -> emptyList()
    }

    val filteredAlerts = remember(precedentAlerts, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> precedentAlerts.filter { !it.isApplied }
            "APPLIED" -> precedentAlerts.filter { it.isApplied }
            else -> precedentAlerts
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Sidebar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LegalGoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Case Law Revisions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Serif
                        )
                    )
                }
                Text(
                    text = "Live Precedent Alignment & Suggested Fixes",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
            }

            IconButton(
                onClick = onScanPrecedents,
                modifier = Modifier.testTag("scan_precedents_sidebar_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = LegalGoldAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Case Law Updates",
                        tint = LegalGoldAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${precedentAlerts.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LegalGoldAccent,
                    selectedLabelColor = LegalNavyDark,
                    labelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedFilter == "PENDING",
                onClick = { selectedFilter = "PENDING" },
                label = {
                    Text("Pending (${precedentAlerts.count { !it.isApplied }})", fontSize = 11.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ComplianceRiskRed,
                    selectedLabelColor = Color.White,
                    labelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedFilter == "APPLIED",
                onClick = { selectedFilter = "APPLIED" },
                label = {
                    Text("Applied (${precedentAlerts.count { it.isApplied }})", fontSize = 11.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ComplianceGreen,
                    selectedLabelColor = Color.White,
                    labelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = LegalCardBorder)
        Spacer(modifier = Modifier.height(10.dp))

        // Revisions List
        if (filteredAlerts.isEmpty() && auditIssues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ComplianceGreen,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Precedent Discrepancies",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All provisions comply with current case law precedents. Tap refresh to run a deep scan against current federal and state dockets.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onScanPrecedents,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LegalGoldAccent),
                        border = BorderStroke(1.dp, LegalGoldAccent)
                    ) {
                        Icon(Icons.Default.Balance, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Case Law Precedents")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Precedent Overrule & Case Law Shift Revisions
                items(filteredAlerts, key = { it.id.ifBlank { it.caseName + it.impactedClause } }) { alert ->
                    PrecedentRevisionCard(
                        alert = alert,
                        onApply = { onApplyRevision(alert) }
                    )
                }

                // Section 2: Audit Compliance Issues (if available and not filtered to applied)
                if (selectedFilter != "APPLIED" && auditIssues.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Statutory & Enforceability Fixes (${auditIssues.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LegalGoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    items(auditIssues) { issue ->
                        AuditIssueFixCard(
                            issue = issue,
                            onApplyFix = {
                                onApplyComplianceFix(issue.offendingText, issue.suggestedFix, issue.supportingPrecedent)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Case Law Revision Card
 */
@Composable
fun PrecedentRevisionCard(
    alert: PrecedentAlert,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    val (badgeBg, badgeText) = when (alert.changeType) {
        "OVERRULED" -> Pair(ComplianceRiskRed, "OVERRULED PRECEDENT")
        "STATUTORY_SHIFT" -> Pair(Color(0xFF8B5CF6), "STATUTORY OVERHAUL")
        "MODIFIED" -> Pair(ComplianceWarningYellow, "MODIFIED DOCTRINE")
        "QUESTIONED" -> Pair(Color(0xFFEC4899), "QUESTIONED AUTHORITY")
        else -> Pair(ComplianceBlue, alert.changeType)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("revision_card_${alert.id}"),
        colors = CardDefaults.cardColors(containerColor = LegalCardDark),
        border = BorderStroke(
            1.dp,
            if (alert.isApplied) ComplianceGreen.copy(alpha = 0.5f) else badgeBg.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Badge & Applied Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (alert.isApplied) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ComplianceGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Applied to Draft",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ComplianceGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Controlling Case Authority
            Text(
                text = alert.caseName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Serif
                )
            )
            Text(
                text = "${alert.citation} • ${alert.court}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = LegalGoldAccent,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Precedent Summary
            Text(
                text = alert.summaryOfChange,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Impacted Clause (Original)
            if (alert.impactedClause.isNotBlank()) {
                Text(
                    text = "IMPACTED DRAFT CLAUSE:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ComplianceRiskRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ComplianceRiskRed.copy(alpha = 0.12f))
                        .border(1.dp, ComplianceRiskRed.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = alert.impactedClause,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFCA5A5),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Suggested Replacement Clause
            Text(
                text = "AI-SUGGESTED ENFORCEABLE REVISION:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ComplianceGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ComplianceGreen.copy(alpha = 0.12f))
                    .border(1.dp, ComplianceGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = alert.suggestedRevision,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF86EFAC),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Strategic Legal Rationale
            Text(
                text = "Rationale: ${alert.legalRationale}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(alert.suggestedRevision))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Suggested Revision",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alert.isApplied) Color(0xFF1E293B) else ComplianceGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("apply_revision_button_${alert.id}")
                ) {
                    Icon(
                        imageVector = if (alert.isApplied) Icons.Default.Refresh else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alert.isApplied) "Re-apply" else "Apply Revision",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Card for Audit Compliance Fixes
 */
@Composable
fun AuditIssueFixCard(
    issue: ComplianceIssue,
    onApplyFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LegalCardDark),
        border = BorderStroke(1.dp, LegalCardBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = issue.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Surface(
                    color = when (issue.severity) {
                        "HIGH" -> ComplianceRiskRed
                        "MEDIUM" -> ComplianceWarningYellow
                        else -> ComplianceBlue
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${issue.severity} RISK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = issue.explanation,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp)
            )

            if (issue.suggestedFix.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ComplianceGreen.copy(alpha = 0.1f))
                        .border(1.dp, ComplianceGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = issue.suggestedFix,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF86EFAC),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onApplyFix,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalGoldAccent,
                            contentColor = LegalNavyDark
                        ),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Apply Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Document Metadata Section (Title, Type, Jurisdiction, Matter Ref)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentMetadataSection(
    title: String,
    docType: String,
    jurisdiction: String,
    matterRef: String,
    onTitleChange: (String) -> Unit,
    onDocTypeChange: (String) -> Unit,
    onJurisdictionChange: (String) -> Unit,
    onMatterRefChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var docTypeExpanded by remember { mutableStateOf(false) }
    var jurExpanded by remember { mutableStateOf(false) }

    val docTypes = listOf("CONTRACT", "MOTION", "BRIEF", "MEMO", "DEMAND_LETTER", "NDA", "OTHER")
    val jurisdictions = listOf("General US", "Delaware", "California", "New York", "Texas", "US Federal Court")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Document Title", color = Color(0xFF94A3B8)) },
            placeholder = { Text("e.g. Master Employment Agreement or Motion for Summary Judgment", color = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("draft_title_input"),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LegalGoldAccent,
                unfocusedBorderColor = LegalCardBorder
            ),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Type dropdown
            Column(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = docTypeExpanded,
                    onExpandedChange = { docTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = docType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Doc Type", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("doc_type_dropdown"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LegalGoldAccent,
                            unfocusedBorderColor = LegalCardBorder
                        ),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = docTypeExpanded,
                        onDismissRequest = { docTypeExpanded = false }
                    ) {
                        docTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onDocTypeChange(type)
                                    docTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Jurisdiction dropdown
            Column(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = jurExpanded,
                    onExpandedChange = { jurExpanded = it }
                ) {
                    OutlinedTextField(
                        value = jurisdiction,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jurisdiction", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jurExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("jurisdiction_dropdown"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LegalGoldAccent,
                            unfocusedBorderColor = LegalCardBorder
                        ),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = jurExpanded,
                        onDismissRequest = { jurExpanded = false }
                    ) {
                        jurisdictions.forEach { jur ->
                            DropdownMenuItem(
                                text = { Text(jur) },
                                onClick = {
                                    onJurisdictionChange(jur)
                                    jurExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Matter Ref
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = matterRef,
                    onValueChange = onMatterRefChange,
                    label = { Text("Matter #", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                    placeholder = { Text("e.g. 2026-904", color = Color(0xFF64748B), fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("matter_ref_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LegalGoldAccent,
                        unfocusedBorderColor = LegalCardBorder
                    ),
                    singleLine = true
                )
            }
        }
    }
}

/**
 * Quick Standard Clause Toolbar
 */
@Composable
fun QuickClauseToolbar(
    jurisdiction: String,
    onInsertClause: (String) -> Unit,
    onOpenAiClauseDrafter: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Standard Clauses:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = onExportPdf,
                    label = { Text("Export PDF", color = LegalGoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = LegalGoldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = LegalCardDark),
                    border = BorderStroke(1.dp, LegalGoldAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("quick_export_pdf_chip")
                )
                AssistChip(
                    onClick = onOpenAiClauseDrafter,
                    label = { Text("AI Clause Studio", color = LegalGoldAccent, fontSize = 10.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = LegalGoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = LegalCardDark),
                    border = BorderStroke(1.dp, LegalGoldAccent.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = {
                    onInsertClause("SEVERABILITY: If any provision of this Agreement is held to be invalid, illegal, or unenforceable under controlling statutory authority or precedent, such provision shall be severed, and the remaining provisions shall remain in full force and effect.")
                },
                label = { Text("Severability", color = Color.White, fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, LegalCardBorder)
            )
            AssistChip(
                onClick = {
                    onInsertClause("GOVERNING LAW & JURISDICTION: This Agreement shall be governed by and construed in accordance with the laws of $jurisdiction, without regard to conflict of laws principles. The parties consent to exclusive jurisdiction in the courts located therein.")
                },
                label = { Text("Governing Law", color = Color.White, fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, LegalCardBorder)
            )
            AssistChip(
                onClick = {
                    onInsertClause("LIMITATION OF LIABILITY: Neither party shall be liable for indirect, incidental, punitive, or consequential damages. Aggregate liability shall not exceed the amounts paid under this Agreement in the twelve (12) months preceding the claim.")
                },
                label = { Text("Limitation of Liability", color = Color.White, fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, LegalCardBorder)
            )
            AssistChip(
                onClick = {
                    onInsertClause("TRADE SECRET CARVEOUT (Cal. Civ. Code § 3426): In lieu of non-competition restraints, Employee agrees to preserve proprietary Trade Secrets in accordance with the Uniform Trade Secrets Act.")
                },
                label = { Text("Trade Secret Carveout", color = Color.White, fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, LegalCardBorder)
            )
        }
    }
}

/**
 * Editor Actions Bar (Save, Run Audit, Scan Precedents, Delete)
 */
@Composable
fun EditorActionsBar(
    onSave: () -> Unit,
    onRunAudit: () -> Unit,
    onScanPrecedents: () -> Unit,
    onExportPdf: () -> Unit,
    onDelete: (() -> Unit)?,
    isAuditLoading: Boolean,
    isPrecedentLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("save_draft_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ComplianceGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Draft", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onExportPdf,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("export_pdf_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LegalGoldAccent,
                    contentColor = LegalNavyDark
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRunAudit,
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .testTag("run_audit_button_bottom"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LegalGoldAccent),
                border = BorderStroke(1.dp, LegalGoldAccent),
                shape = RoundedCornerShape(8.dp),
                enabled = !isAuditLoading
            ) {
                if (isAuditLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = LegalGoldAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Balance, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audit Case Law", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                }
            }

            OutlinedButton(
                onClick = onScanPrecedents,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("scan_precedents_bottom_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, LegalCardBorder),
                shape = RoundedCornerShape(8.dp),
                enabled = !isPrecedentLoading
            ) {
                if (isPrecedentLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = LegalGoldAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = LegalGoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Radar", fontSize = 12.sp)
                }
            }
        }

        if (onDelete != null) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("delete_draft_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ComplianceRiskRed),
                border = BorderStroke(1.dp, ComplianceRiskRed.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Legal Draft")
            }
        }
    }
}

/**
 * Professional Legal PDF Export Dialog
 */
@Composable
fun ExportPdfDialog(
    title: String,
    docType: String,
    jurisdiction: String,
    matterRef: String,
    wordCount: Int,
    charCount: Int,
    exportState: UiState<String>,
    onDismiss: () -> Unit,
    onExportToDevice: (LegalExportOptions) -> Unit,
    onSharePdf: (LegalExportOptions) -> Unit,
    onPrintPdf: (LegalExportOptions) -> Unit
) {
    var includePrecedents by remember { mutableStateOf(true) }
    var includeWatermark by remember { mutableStateOf(true) }
    var includeSignatureBlock by remember { mutableStateOf(true) }

    val currentOptions = remember(includePrecedents, includeWatermark, includeSignatureBlock) {
        LegalExportOptions(
            includePrecedentsAppendix = includePrecedents,
            includeWatermark = includeWatermark,
            includeSignatureBlock = includeSignatureBlock
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LegalCardDark,
        titleContentColor = LegalGoldAccent,
        textContentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LegalGoldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = LegalGoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Export Formatted PDF",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = LegalGoldAccent
                        )
                    )
                    Text(
                        text = "Judicial format • Case law verified",
                        style = MaterialTheme.typography.bodySmall.copy(color = LegalSlate)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Document Info Summary Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = LegalNavyPrimary),
                    border = BorderStroke(1.dp, LegalCardBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title.ifBlank { "Untitled Legal Instrument" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 2
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Matter: ${matterRef.ifBlank { "UNASSIGNED" }}",
                                style = MaterialTheme.typography.labelSmall.copy(color = LegalSlateLight)
                            )
                            Text("•", color = LegalSlate)
                            Text(
                                text = jurisdiction.ifBlank { "US General" },
                                style = MaterialTheme.typography.labelSmall.copy(color = LegalSlateLight)
                            )
                        }
                        Text(
                            text = "$wordCount words • $charCount characters • US Letter (612x792 pt)",
                            style = MaterialTheme.typography.labelSmall.copy(color = LegalGoldAccent)
                        )
                    }
                }

                Text(
                    text = "Publication Options:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LegalGoldAccent
                    )
                )

                // Option 1: Precedent Appendix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includePrecedents,
                        onCheckedChange = { includePrecedents = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LegalGoldAccent,
                            uncheckedColor = LegalSlate
                        ),
                        modifier = Modifier.testTag("checkbox_precedents_appendix")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Exhibit A: Precedent Compliance Schedule",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color.White)
                        )
                        Text(
                            text = "Appends verified citations and case law audit records",
                            style = MaterialTheme.typography.labelSmall.copy(color = LegalSlate)
                        )
                    }
                }

                // Option 2: Confidential Watermark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeWatermark,
                        onCheckedChange = { includeWatermark = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LegalGoldAccent,
                            uncheckedColor = LegalSlate
                        ),
                        modifier = Modifier.testTag("checkbox_watermark")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Privileged Work Product Watermark",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color.White)
                        )
                        Text(
                            text = "Faint 'CONFIDENTIAL & PRIVILEGED' seal across pages",
                            style = MaterialTheme.typography.labelSmall.copy(color = LegalSlate)
                        )
                    }
                }

                // Option 3: Signature Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeSignatureBlock,
                        onCheckedChange = { includeSignatureBlock = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LegalGoldAccent,
                            uncheckedColor = LegalSlate
                        ),
                        modifier = Modifier.testTag("checkbox_signature_block")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Formal Execution & Signature Block",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color.White)
                        )
                        Text(
                            text = "Standard dual-party signature lines and date spaces",
                            style = MaterialTheme.typography.labelSmall.copy(color = LegalSlate)
                        )
                    }
                }

                // State feedback
                when (exportState) {
                    is UiState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LegalGoldAccent,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Compiling legal PDF document...",
                                style = MaterialTheme.typography.labelMedium.copy(color = LegalGoldAccent)
                            )
                        }
                    }
                    is UiState.Success -> {
                        Text(
                            text = "✓ ${exportState.data}",
                            style = MaterialTheme.typography.bodySmall.copy(color = ComplianceGreen)
                        )
                    }
                    is UiState.Error -> {
                        Text(
                            text = "⚠ Error: ${exportState.message}",
                            style = MaterialTheme.typography.bodySmall.copy(color = ComplianceRiskRed)
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onExportToDevice(currentOptions) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalGoldAccent,
                        contentColor = LegalNavyDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_confirm_export_pdf")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save PDF to Device", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSharePdf(currentOptions) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, LegalCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_share_pdf")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onPrintPdf(currentOptions) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, LegalCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print_pdf")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview", fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LegalSlate),
                border = BorderStroke(1.dp, LegalCardBorder.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    )
}

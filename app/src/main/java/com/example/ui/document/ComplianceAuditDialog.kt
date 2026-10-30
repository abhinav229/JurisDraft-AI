package com.example.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ComplianceAuditReport
import com.example.ui.theme.ComplianceGreen
import com.example.ui.theme.ComplianceRiskRed
import com.example.ui.theme.ComplianceWarningYellow
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark
import com.example.ui.theme.LegalNavyPrimary

@Composable
fun ComplianceAuditDialog(
    auditState: UiState<ComplianceAuditReport>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = LegalCardDark,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = LegalGoldAccent,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Case Law Compliance Audit",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_audit_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Divider(
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                when (auditState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = LegalGoldAccent,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Analyzing clauses against prevailing case law...",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1))
                                )
                            }
                        }
                    }

                    is UiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ComplianceRiskRed.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = ComplianceRiskRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = auditState.message,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    is UiState.Success -> {
                        val report = auditState.data
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Score Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = LegalNavyPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "COMPLIANCE POSTURE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = report.overallAssessment,
                                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Circular Score Gauge
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .background(
                                                    color = when {
                                                        report.complianceScore >= 80 -> ComplianceGreen.copy(alpha = 0.2f)
                                                        report.complianceScore >= 60 -> ComplianceWarningYellow.copy(alpha = 0.2f)
                                                        else -> ComplianceRiskRed.copy(alpha = 0.2f)
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "${report.complianceScore}",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            report.complianceScore >= 80 -> ComplianceGreen
                                                            report.complianceScore >= 60 -> ComplianceWarningYellow
                                                            else -> ComplianceRiskRed
                                                        }
                                                    )
                                                )
                                                Text(
                                                    text = "/100",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Issues Section
                            if (report.issues.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Identified Legal Risks & Clause Defects (${report.issues.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = LegalGoldAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                items(report.issues) { issue ->
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (issue.severity == "HIGH") ComplianceRiskRed.copy(alpha = 0.6f)
                                                else ComplianceWarningYellow.copy(alpha = 0.6f)
                                            )
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = issue.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Surface(
                                                    color = if (issue.severity == "HIGH") ComplianceRiskRed.copy(alpha = 0.2f) else ComplianceWarningYellow.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = issue.severity,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (issue.severity == "HIGH") ComplianceRiskRed else ComplianceWarningYellow,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = issue.explanation,
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                            )

                                            if (issue.offendingText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Surface(
                                                    color = Color(0xFF0F172A),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Defective Text: \"${issue.offendingText}\"",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = Color(0xFFF87171),
                                                            fontFamily = FontFamily.Monospace
                                                        ),
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }

                                            if (issue.suggestedFix.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Surface(
                                                    color = ComplianceGreen.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Recommended Redraft: \"${issue.suggestedFix}\"",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = ComplianceGreen,
                                                            fontFamily = FontFamily.Monospace
                                                        ),
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }

                                            if (issue.supportingPrecedent.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Authority: ${issue.supportingPrecedent}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = LegalGoldAccent,
                                                        fontFamily = FontFamily.Serif
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Controlling Precedents
                            if (report.leadingPrecedents.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Controlling Case Law Precedents",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = LegalGoldAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                items(report.leadingPrecedents) { precedent ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = precedent.caseName,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Serif,
                                                    color = LegalGoldAccent
                                                )
                                            )
                                            Text(
                                                text = "${precedent.citation} • ${precedent.courtAndYear}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Rule: ${precedent.keyPrinciple}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                            )
                                            if (precedent.relevanceToDraft.isNotBlank()) {
                                                Text(
                                                    text = "Application: ${precedent.relevanceToDraft}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Recommendations
                            if (report.recommendations.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Strategic Recommendations",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = LegalGoldAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                items(report.recommendations) { rec ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "•", color = LegalGoldAccent, modifier = Modifier.padding(end = 8.dp))
                                        Text(
                                            text = rec,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is UiState.Idle -> {}
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_audit_dialog_action"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalGoldAccent,
                        contentColor = LegalNavyDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Audit Report", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

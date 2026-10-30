package com.example.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PrecedentAlert
import com.example.data.model.WatchedDoctrine
import com.example.ui.theme.ComplianceGreen
import com.example.ui.theme.ComplianceRiskRed
import com.example.ui.theme.ComplianceWarningYellow
import com.example.ui.theme.LegalCardBorder
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark
import com.example.ui.theme.LegalNavyPrimary

@Composable
fun PrecedentRadarScreen(
    viewModel: DocumentViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.precedentAlerts.collectAsState()
    val watchedDoctrines by viewModel.watchedDoctrines.collectAsState()
    val scanState by viewModel.precedentScanState.collectAsState()
    val selectedDoc by viewModel.selectedDocument.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Precedent Alerts (${alerts.size})", "Doctrine Watchlist (${watchedDoctrines.size})")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LegalNavyDark)
            .padding(16.dp)
    ) {
        // Hero Radar Header
        Card(
            colors = CardDefaults.cardColors(containerColor = LegalNavyPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = LegalGoldAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = LegalGoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Precedent Radar & Citator",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Text(
                                text = "Continuous monitoring of binding authority shifts",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scan Button
                Button(
                    onClick = { viewModel.scanForPrecedentUpdates() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("scan_precedents_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalGoldAccent,
                        contentColor = LegalNavyDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (scanState is UiState.Loading) {
                        CircularProgressIndicator(
                            color = LegalNavyDark,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning judicial database...", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDoc != null) "Scan Active Draft for Precedent Shifts" else "Scan Recent Case Law Shifts",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = LegalNavyDark,
            contentColor = LegalGoldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = LegalGoldAccent
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) LegalGoldAccent else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTabIndex == 0) {
            // Precedent Alerts List
            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ComplianceGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Precedent Discrepancies",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All authorities and doctrines in your active drafts conform to current case law.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(alerts, key = { it.id + it.caseName }) { alert ->
                        PrecedentAlertCard(
                            alert = alert,
                            onApplyRevision = {
                                viewModel.applyPrecedentRevision(alert)
                                onNavigateToEditor()
                            }
                        )
                    }
                }
            }
        } else {
            // Watched Doctrines List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(watchedDoctrines, key = { it.id }) { doctrine ->
                    WatchedDoctrineCard(doctrine = doctrine)
                }
            }
        }
    }
}

@Composable
fun PrecedentAlertCard(
    alert: PrecedentAlert,
    onApplyRevision: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (alert.changeType) {
        "OVERRULED", "STATUTORY_SHIFT" -> ComplianceRiskRed
        "MODIFIED", "QUESTIONED" -> ComplianceWarningYellow
        else -> LegalGoldAccent
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LegalCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(badgeColor.copy(alpha = 0.5f))
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badge & Court
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = alert.changeType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = alert.court,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Case Name & Citation
            Text(
                text = alert.caseName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Serif
                )
            )
            if (alert.citation.isNotBlank()) {
                Text(
                    text = alert.citation,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = LegalGoldAccent,
                        fontFamily = FontFamily.Serif
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary of shift
            Text(
                text = alert.summaryOfChange,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
            )

            // Impacted Clause
            if (alert.impactedClause.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Outdated Clause in Draft:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ComplianceRiskRed,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = alert.impactedClause,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFCA5A5),
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Suggested Revision
            if (alert.suggestedRevision.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Recommended Enforceable Replacement:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ComplianceGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Surface(
                    color = ComplianceGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = alert.suggestedRevision,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ComplianceGreen,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Legal Rationale
            if (alert.legalRationale.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rationale: ${alert.legalRationale}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Apply AI Revision
            if (alert.isApplied) {
                Surface(
                    color = ComplianceGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ComplianceGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Revision Applied to Draft",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ComplianceGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                Button(
                    onClick = onApplyRevision,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("apply_precedent_revision_button_${alert.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComplianceGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply AI Revision to Draft",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WatchedDoctrineCard(
    doctrine: WatchedDoctrine,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = LegalCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(LegalCardBorder)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doctrine.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = LegalNavyPrimary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = doctrine.jurisdiction,
                        style = MaterialTheme.typography.labelSmall.copy(color = LegalGoldAccent),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = doctrine.description,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Latest Judicial Movement:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = LegalGoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = doctrine.recentDevelopments,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

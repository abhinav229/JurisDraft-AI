package com.example.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LegalDocument
import com.example.ui.theme.ComplianceGreen
import com.example.ui.theme.ComplianceRiskRed
import com.example.ui.theme.ComplianceWarningYellow
import com.example.ui.theme.LegalCardBorder
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark
import com.example.ui.theme.LegalNavyPrimary

@Composable
fun DocumentListScreen(
    viewModel: DocumentViewModel,
    onOpenEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.documents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "CONTRACT", "MOTION", "BRIEF", "NDA")

    val filteredDocuments = remember(documents, searchQuery, selectedFilter) {
        documents.filter { doc ->
            val matchesFilter = if (selectedFilter == "ALL") true else doc.docType.equals(selectedFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.matterRef.contains(searchQuery, ignoreCase = true) ||
                doc.jurisdiction.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.selectDocument(null)
                    onOpenEditor()
                },
                containerColor = LegalGoldAccent,
                contentColor = LegalNavyDark,
                modifier = Modifier.testTag("create_document_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Legal Draft")
            }
        },
        containerColor = LegalNavyDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search drafts, matters, jurisdictions...", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LegalGoldAccent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_documents_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = LegalGoldAccent,
                    unfocusedBorderColor = LegalCardBorder,
                    focusedContainerColor = LegalCardDark,
                    unfocusedContainerColor = LegalCardDark
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                color = if (selectedFilter == filter) LegalNavyDark else Color(0xFFCBD5E1),
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LegalGoldAccent,
                            containerColor = LegalCardDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedFilter == filter) LegalGoldAccent else LegalCardBorder,
                            enabled = true,
                            selected = selectedFilter == filter
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredDocuments.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LegalGoldAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = LegalGoldAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (documents.isEmpty()) "No Legal Drafts Yet" else "No Matching Drafts Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (documents.isEmpty())
                                "Start drafting a court brief, agreement, or memorandum with automated case law compliance."
                            else "Try searching for a different term or clearing filters.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        if (documents.isEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            androidx.compose.material3.Button(
                                onClick = { viewModel.seedSampleDocumentIfEmpty() },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = LegalGoldAccent,
                                    contentColor = LegalNavyDark
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Load Sample NDA with Precedent Scan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDocuments, key = { it.id }) { document ->
                        DocumentItemCard(
                            document = document,
                            onClick = {
                                viewModel.selectDocument(document)
                                onOpenEditor()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItemCard(
    document: LegalDocument,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LegalCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(LegalCardBorder)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("document_card_${document.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: DocType & Jurisdiction Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = LegalNavyPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = document.docType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LegalGoldAccent,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${document.jurisdiction}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                }

                // Compliance Score Pill
                if (document.complianceScore > 0) {
                    val scoreColor = when {
                        document.complianceScore >= 80 -> ComplianceGreen
                        document.complianceScore >= 60 -> ComplianceWarningYellow
                        else -> ComplianceRiskRed
                    }
                    Surface(
                        color = scoreColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = scoreColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${document.complianceScore}% Score",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = scoreColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "UNAUDITED",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Document Title
            Text(
                text = document.title.ifBlank { "Untitled Legal Document" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White
                )
            )

            if (document.matterRef.isNotBlank()) {
                Text(
                    text = "Ref: ${document.matterRef}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
            }

            // Excerpt
            if (document.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = document.content.take(160).replace("\n", " ") + "...",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1)),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Latest audit summary if present
            if (document.lastAuditSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Audit: ${document.lastAuditSummary}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFE2E8F0)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

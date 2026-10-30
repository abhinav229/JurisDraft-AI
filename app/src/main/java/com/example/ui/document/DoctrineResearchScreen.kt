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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LegalCardBorder
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark
import com.example.ui.theme.LegalNavyPrimary

@Composable
fun DoctrineResearchScreen(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val researchState by viewModel.researchState.collectAsState()
    var doctrineQuery by remember { mutableStateOf("") }
    var selectedJurisdiction by remember { mutableStateOf("US Federal Court") }

    val jurisdictions = listOf("US Federal Court", "Delaware", "California", "New York", "Texas")
    val suggestedTopics = listOf(
        "Summary Judgment Burden (Celotex)",
        "Chevron Deference Overruled (Loper Bright)",
        "Delaware Controlling Stockholder MFW (Match Group)",
        "Non-Compete Enforceability (Cal. SB 699)",
        "Liquidated Damages vs Penalty (Truck Rent-A-Center)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LegalNavyDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Case Law Precedent Explorer",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            )
        )
        Text(
            text = "Query seminal holdings, controlling precedents, and drafting doctrines",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = doctrineQuery,
            onValueChange = { doctrineQuery = it },
            placeholder = { Text("Search legal doctrine or case topic...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LegalGoldAccent) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("doctrine_search_input"),
            singleLine = true,
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

        // Jurisdiction Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(jurisdictions) { jur ->
                FilterChip(
                    selected = selectedJurisdiction == jur,
                    onClick = { selectedJurisdiction = jur },
                    label = {
                        Text(
                            text = jur,
                            color = if (selectedJurisdiction == jur) LegalNavyDark else Color(0xFFCBD5E1),
                            fontWeight = if (selectedJurisdiction == jur) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LegalGoldAccent,
                        containerColor = LegalCardDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Button
        Button(
            onClick = {
                if (doctrineQuery.isNotBlank()) {
                    viewModel.researchPrecedents(doctrineQuery, selectedJurisdiction)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("research_doctrine_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = LegalGoldAccent,
                contentColor = LegalNavyDark
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find Controlling Precedents", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggested Queries
        if (researchState is UiState.Idle) {
            Text(
                text = "Key Precedent Inquiries:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestedTopics.forEach { topic ->
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    doctrineQuery = topic
                                    viewModel.researchPrecedents(topic, selectedJurisdiction)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LegalNavyPrimary),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Explore", color = LegalGoldAccent, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        when (val state = researchState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LegalGoldAccent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Retrieving controlling appellate authorities...", color = Color(0xFFCBD5E1))
                    }
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { precedent ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = LegalCardDark),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LegalGoldAccent.copy(alpha = 0.4f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = precedent.caseName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = LegalGoldAccent
                                    )
                                )
                                Text(
                                    text = "${precedent.citation} • ${precedent.courtAndYear}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Holding: ${precedent.keyPrinciple}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                                if (precedent.relevanceToDraft.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Drafting Application: ${precedent.relevanceToDraft}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1)),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is UiState.Error -> {
                Text(text = state.message, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
            }

            is UiState.Idle -> {}
        }
    }
}

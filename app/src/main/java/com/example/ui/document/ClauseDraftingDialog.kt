package com.example.ui.document

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ComplianceGreen
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClauseDraftingDialog(
    clauseState: UiState<String>,
    initialJurisdiction: String,
    onDraftClause: (clauseType: String, jurisdiction: String, favoredParty: String, instructions: String) -> Unit,
    onInsertClause: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clauseTypes = listOf(
        "Indemnification & Defense",
        "Limitation of Liability & Carveouts",
        "Governing Law & Exclusive Forum",
        "Severability & Judicial Reformation",
        "Non-Disclosure & Trade Secrets",
        "Liquidated Damages & Reasonableness",
        "Force Majeure & Allocation of Risk",
        "Dispute Resolution & Arbitration",
        "Termination & Post-Termination Rights"
    )

    val jurisdictions = listOf(
        "General US",
        "Delaware",
        "California",
        "New York",
        "Texas",
        "US Federal Court"
    )

    val partyBiases = listOf(
        "Neutral / Balanced",
        "Client-Favorable (Aggressive Protection)",
        "Counterparty-Favorable (Limited Scope)"
    )

    var selectedType by remember { mutableStateOf(clauseTypes[0]) }
    var selectedJurisdiction by remember { mutableStateOf(initialJurisdiction.ifBlank { jurisdictions[0] }) }
    var selectedBias by remember { mutableStateOf(partyBiases[0]) }
    var customInstructions by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var jurExpanded by remember { mutableStateOf(false) }
    var biasExpanded by remember { mutableStateOf(false) }

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
                    .verticalScroll(rememberScrollState())
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Clause Drafter",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_clause_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 12.dp))

                // Clause Type Dropdown
                Text(
                    text = "Clause Type",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF94A3B8))
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LegalGoldAccent,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        clauseTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Jurisdiction & Bias in Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Jurisdiction
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Jurisdiction",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF94A3B8))
                        )
                        ExposedDropdownMenuBox(
                            expanded = jurExpanded,
                            onExpandedChange = { jurExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedJurisdiction,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jurExpanded) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LegalGoldAccent,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = jurExpanded,
                                onDismissRequest = { jurExpanded = false }
                            ) {
                                jurisdictions.forEach { jur ->
                                    DropdownMenuItem(
                                        text = { Text(jur) },
                                        onClick = {
                                            selectedJurisdiction = jur
                                            jurExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bias
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Favored Party",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF94A3B8))
                        )
                        ExposedDropdownMenuBox(
                            expanded = biasExpanded,
                            onExpandedChange = { biasExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedBias,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = biasExpanded) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LegalGoldAccent,
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = biasExpanded,
                                onDismissRequest = { biasExpanded = false }
                            ) {
                                partyBiases.forEach { bias ->
                                    DropdownMenuItem(
                                        text = { Text(bias) },
                                        onClick = {
                                            selectedBias = bias
                                            biasExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom parameters / instructions
                Text(
                    text = "Special Drafting Instructions (Optional)",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF94A3B8))
                )
                OutlinedTextField(
                    value = customInstructions,
                    onValueChange = { customInstructions = it },
                    placeholder = { Text("e.g. Include 30-day notice cure period, exclude gross negligence from liability cap", color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LegalGoldAccent,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Generate button
                Button(
                    onClick = {
                        onDraftClause(selectedType, selectedJurisdiction, selectedBias, customInstructions)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_clause_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalGoldAccent,
                        contentColor = LegalNavyDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Generate Precision Clause with AI", fontWeight = FontWeight.Bold)
                }

                // Result Preview
                when (clauseState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = LegalGoldAccent)
                        }
                    }

                    is UiState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Generated Clause Output",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LegalGoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = clauseState.data,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onInsertClause(clauseState.data)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ComplianceGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("insert_clause_into_draft_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Insert Clause into Document")
                                }
                            }
                        }
                    }

                    is UiState.Error -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = clauseState.message,
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    is UiState.Idle -> {}
                }
            }
        }
    }
}

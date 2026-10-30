package com.example.ui.template

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LegalCardBorder
import com.example.ui.theme.LegalCardDark
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAsTemplateDialog(
    initialTitle: String,
    initialDocType: String,
    initialJurisdiction: String,
    onDismiss: () -> Unit,
    onConfirmSave: (title: String, docType: String, jurisdiction: String, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(initialTitle.ifBlank { "Custom Legal Template" }) }
    var docType by remember { mutableStateOf(initialDocType.ifBlank { "CONTRACT" }) }
    var jurisdiction by remember { mutableStateOf(initialJurisdiction.ifBlank { "General US" }) }
    var description by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = LegalGoldAccent
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Save as Legal Template",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color.White
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Save the current draft structure and clauses into your local Room database for reuse across matters.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Template Name", color = LegalGoldAccent) },
                    isError = isError,
                    supportingText = if (isError) { { Text("Title cannot be empty", color = Color(0xFFEF4444)) } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_template_title_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LegalGoldAccent,
                        unfocusedBorderColor = LegalCardBorder,
                        focusedContainerColor = LegalNavyDark,
                        unfocusedContainerColor = LegalNavyDark
                    )
                )

                OutlinedTextField(
                    value = docType,
                    onValueChange = { docType = it },
                    label = { Text("Document Type (e.g. CONTRACT, NDA, MOTION)", color = LegalGoldAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_template_doctype_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LegalGoldAccent,
                        unfocusedBorderColor = LegalCardBorder,
                        focusedContainerColor = LegalNavyDark,
                        unfocusedContainerColor = LegalNavyDark
                    )
                )

                OutlinedTextField(
                    value = jurisdiction,
                    onValueChange = { jurisdiction = it },
                    label = { Text("Jurisdiction (e.g. California, Delaware, Federal)", color = LegalGoldAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_template_jurisdiction_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LegalGoldAccent,
                        unfocusedBorderColor = LegalCardBorder,
                        focusedContainerColor = LegalNavyDark,
                        unfocusedContainerColor = LegalNavyDark
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Description / Intended Use Case", color = LegalGoldAccent) },
                    placeholder = { Text("e.g. Mutual confidentiality clause with trade secret carve-outs", color = Color(0xFF64748B)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_template_desc_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LegalGoldAccent,
                        unfocusedBorderColor = LegalCardBorder,
                        focusedContainerColor = LegalNavyDark,
                        unfocusedContainerColor = LegalNavyDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        onConfirmSave(title, docType, jurisdiction, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LegalGoldAccent,
                    contentColor = LegalNavyDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_save_template_btn")
            ) {
                Text("Save Template", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = LegalCardDark,
        modifier = modifier.border(1.dp, LegalCardBorder, RoundedCornerShape(16.dp))
    )
}

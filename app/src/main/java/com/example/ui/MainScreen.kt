package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.auth.AuthViewModel
import com.example.ui.document.DoctrineResearchScreen
import com.example.ui.document.DocumentEditorScreen
import com.example.ui.document.DocumentListScreen
import com.example.ui.document.DocumentViewModel
import com.example.ui.document.PrecedentRadarScreen
import com.example.ui.theme.ComplianceRiskRed
import com.example.ui.theme.LegalGoldAccent
import com.example.ui.theme.LegalNavyDark
import com.example.ui.theme.LegalNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    documentViewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val precedentAlerts by documentViewModel.precedentAlerts.collectAsState()
    val userMessage by documentViewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        documentViewModel.seedSampleDocumentIfEmpty()
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            documentViewModel.clearUserMessage()
        }
    }

    val unappliedAlertCount = remember(precedentAlerts) {
        precedentAlerts.count { !it.isApplied }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_juris_logo),
                            contentDescription = "JurisDraft Emblem",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "JurisDraft AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = LegalGoldAccent,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            if (currentUser?.email != null) {
                                Text(
                                    text = currentUser?.email ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { authViewModel.signOut(context) },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LegalNavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LegalNavyPrimary,
                contentColor = LegalGoldAccent
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Drafts") },
                    label = { Text("Drafts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LegalNavyDark,
                        selectedTextColor = LegalGoldAccent,
                        indicatorColor = LegalGoldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_drafts")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Editor") },
                    label = { Text("Editor") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LegalNavyDark,
                        selectedTextColor = LegalGoldAccent,
                        indicatorColor = LegalGoldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_editor")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        if (unappliedAlertCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = ComplianceRiskRed) {
                                        Text("$unappliedAlertCount")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = "Precedent Radar")
                            }
                        } else {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Precedent Radar")
                        }
                    },
                    label = { Text("Radar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LegalNavyDark,
                        selectedTextColor = LegalGoldAccent,
                        indicatorColor = LegalGoldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_radar")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Case Law Explorer") },
                    label = { Text("Doctrine") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LegalNavyDark,
                        selectedTextColor = LegalGoldAccent,
                        indicatorColor = LegalGoldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_doctrine")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DocumentListScreen(
                    viewModel = documentViewModel,
                    onOpenEditor = { selectedTab = 1 }
                )
                1 -> DocumentEditorScreen(
                    viewModel = documentViewModel,
                    onNavigateToPrecedents = { selectedTab = 2 }
                )
                2 -> PrecedentRadarScreen(
                    viewModel = documentViewModel,
                    onNavigateToEditor = { selectedTab = 1 }
                )
                3 -> DoctrineResearchScreen(
                    viewModel = documentViewModel
                )
            }
        }
    }
}

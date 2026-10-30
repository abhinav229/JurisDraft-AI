package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.SignInScreen
import com.example.ui.document.DocumentViewModel
import com.example.ui.theme.JurisDraftTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            JurisDraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        SignInScreen(authViewModel = authViewModel)
    } else {
        val documentViewModel: DocumentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        MainScreen(
            authViewModel = authViewModel,
            documentViewModel = documentViewModel
        )
    }
}

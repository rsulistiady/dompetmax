package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.Translate
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeChoice by viewModel.themeSelection.collectAsState()
            val currentLanguage by viewModel.currentLanguage.collectAsState()

            // Handle dynamic light/dark mode overrides
            val useDarkTheme = when (themeChoice) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "DompetMax",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.testTag("app_top_bar")
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("app_bottom_bar")
                        ) {
                            val items = listOf(
                                Triple(0, Translate.getString("dashboard", currentLanguage), Icons.Default.Dashboard),
                                Triple(1, Translate.getString("transactions", currentLanguage), Icons.Default.ReceiptLong),
                                Triple(2, Translate.getString("subscriptions", currentLanguage), Icons.Default.Subscriptions),
                                Triple(3, Translate.getString("investments", currentLanguage), Icons.Default.Analytics),
                                Triple(4, Translate.getString("settings", currentLanguage), Icons.Default.Settings)
                            )

                            items.forEach { (index, label, icon) ->
                                NavigationBarItem(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_item_$index"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                                        unselectedTextColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Root display routing
                    val contentModifier = Modifier.padding(innerPadding)
                    when (selectedTab) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { selectedTab = it },
                            modifier = contentModifier
                        )
                        1 -> TransactionScreen(
                            viewModel = viewModel,
                            modifier = contentModifier
                        )
                        2 -> SubscriptionScreen(
                            viewModel = viewModel,
                            modifier = contentModifier
                        )
                        3 -> InvestmentScreen(
                            viewModel = viewModel,
                            modifier = contentModifier
                        )
                        4 -> SettingsScreen(
                            viewModel = viewModel,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}

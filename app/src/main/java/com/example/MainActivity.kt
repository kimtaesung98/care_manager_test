package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.MyFirebaseMessagingService
import com.example.domain.model.AlertType
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ElderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Let content flow seamlessly behind system bars (enableedge-to-edge)
        enableEdgeToEdge()

        // Setup emergency and care warning native channels on Android startup
        MyFirebaseMessagingService.setupNotificationChannels(applicationContext)

        setContent {
            MyApplicationTheme {
                MainAppLayout(
                    onCallAction = { phoneNumber ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// Sealed class represents Bottom Navigation Tabs for Social Workers
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Warning)
    object Visits : Screen("visits", "Visits", Icons.Default.FactCheck)
    object Contacts : Screen("contacts", "Contacts", Icons.Default.ContactPhone)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainAppLayout(
    onCallAction: (String) -> Unit,
    viewModel: ElderViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Direct Reactive Observables of SQLite Offline Cached State Flows
    val elders by viewModel.elders.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val visits by viewModel.visits.collectAsStateWithLifecycle()
    val thresholds by viewModel.thresholds.collectAsStateWithLifecycle()
    val biometrics by viewModel.selectedElderBiometrics.collectAsStateWithLifecycle()
    val cachedChecklistItems by viewModel.cachedChecklistItems.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    
    // Live alarm overrides
    val activeOverlay by viewModel.activeCriticalOverlayAlert.collectAsStateWithLifecycle()
    val unreadAlertCount = remember(alerts) { alerts.count { !it.isRead } }

    val bottomNavigationItems = listOf(
        Screen.Dashboard,
        Screen.Alerts,
        Screen.Visits,
        Screen.Contacts,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only paint bottom navigation bar if we are on primary tabs
            val showBottomBar = bottomNavigationItems.any { it.route == currentRoute } || currentRoute?.startsWith("visit") == true
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_bar")
                ) {
                    bottomNavigationItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.Alerts && unreadAlertCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text("$unreadAlertCount") }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.testTag("nav_tab_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // NavHost managing clinical screen state transitions
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen 1: Home Dashboard Grid
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    elders = elders,
                    alertCount = unreadAlertCount,
                    onElderSelected = { id ->
                        viewModel.selectElderForDetail(id)
                        navController.navigate("detail/$id")
                    },
                    onNavigateToAlerts = {
                        navController.navigate(Screen.Alerts.route)
                    },
                    onSimulateAlert = { type ->
                        viewModel.simulateIncomingFcmCriticalAlert(type)
                    },
                    onAddClientClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Screen 2: Detailed Wear OS Telemetries
            composable(
                route = "detail/{elderId}",
                arguments = listOf(navArgument("elderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("elderId") ?: ""
                
                // Track detail selection
                LaunchedEffect(id) {
                    viewModel.selectElderForDetail(id)
                }

                val currentSelectedElder = elders.find { it.id == id }

                DetailScreen(
                    elder = currentSelectedElder,
                    biometrics = biometrics,
                    limitHrMin = thresholds.hrMin,
                    limitHrMax = thresholds.hrMax,
                    limitSpo2Min = thresholds.spo2Min,
                    onNavigateToVisit = { elderId ->
                        navController.navigate("visit?elderId=$elderId")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Screen 3: Connected Warn Center Feed
            composable(Screen.Alerts.route) {
                AlertCenterScreen(
                    alerts = alerts,
                    onAlertTap = { elderId ->
                        // Tapping deep-links straight into patient metrics graphs!
                        viewModel.selectElderForDetail(elderId)
                        navController.navigate("detail/$elderId")
                    },
                    onMarkAsRead = { alertId ->
                        viewModel.markWarningAsRead(alertId)
                    }
                )
            }

            // Screen 4: Call Direct Directories
            composable(Screen.Contacts.route) {
                ContactScreen(elders = elders)
            }

            // Screen 5: Home Visit Register and checklists
            composable(
                route = "visits?elderId={elderId}",
                arguments = listOf(navArgument("elderId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val elderIdParam = backStackEntry.arguments?.getString("elderId")
                VisitChecklistScreen(
                    initialElderId = elderIdParam,
                    elders = elders,
                    visits = visits,
                    cachedTasks = cachedChecklistItems,
                    isSyncing = isSyncing,
                    syncStatus = syncStatus,
                    onSyncOffline = { viewModel.syncOfflineData() },
                    onSubmitRecord = { idValue, nameValue, tasksSelected, notesValue ->
                        viewModel.submitVisitChecklist(
                            idValue, nameValue, tasksSelected, notesValue
                        )
                    }
                )
            }

            // Screen 6: Supervisor Configurations Panel
            composable(Screen.Settings.route) {
                SettingsScreen(
                    thresholds = thresholds,
                    assignedElders = elders,
                    onSaveThresholds = { minHr, maxHr, minSat, isFall ->
                        viewModel.saveConfiguredThresholds(minHr, maxHr, minSat, isFall)
                    },
                    onAddNewClient = { name, age, phone, guard, guardPhone ->
                        viewModel.addNewClient(name, age, phone, guard, guardPhone)
                    },
                    onRemoveClient = { elderId ->
                        viewModel.removeClient(elderId)
                    }
                )
            }
        }

        // Full Screen Critical Overlay takes precedence if an active Wear OS emergency triggers
        activeOverlay?.let { fcmAlert ->
            CriticalAlertOverlay(
                alert = fcmAlert,
                onDismiss = { viewModel.clearActiveCriticalOverlay() },
                onCallGuardian = { elderId ->
                    val patient = elders.find { it.id == elderId }
                    if (patient != null) {
                        onCallAction(patient.guardianPhone)
                    } else {
                        onCallAction("+15555555555")
                    }
                }
            )
        }
    }
}

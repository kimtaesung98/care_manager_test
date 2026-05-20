package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AlertType
import com.example.domain.model.Elder
import com.example.domain.model.RiskLevel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    elders: List<Elder>,
    alertCount: Int,
    onElderSelected: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onSimulateAlert: (AlertType) -> Unit,
    onAddClientClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClientClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_elder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assigned Elder")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Elegant Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Senior Care Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Assigned Social Work Client Tracker",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.testTag("alerts_notification_bell")
                    ) {
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge(containerColor = CriticalRed) {
                                        Text("$alertCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alert Center", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Anomaly Alert Banner
            item {
                AlertSummaryBanner(
                    alertCount = alertCount,
                    onBannerClick = onNavigateToAlerts
                )
            }

            // Wear OS Real-Time Simulator Control Panel (Incredibly useful for direct screen verification)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("simulator_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wear OS FCM Live Simulator", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Text(
                            "Tap trigger inputs to simulate WebSocket / FCM alarms and test overlay behavior:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSimulateAlert(AlertType.FallDetected) },
                                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                                modifier = Modifier.weight(1f).testTag("simulate_fall_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text("Simulate Fall", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onSimulateAlert(AlertType.AbnormalHeartRate) },
                                colors = ButtonDefaults.buttonColors(containerColor = CautionYellow),
                                modifier = Modifier.weight(1f).testTag("simulate_heart_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text("High Heart Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Assigned Elderships (${elders.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Client Cards
            if (elders.isEmpty()) {
                item {
                    EmptyEldersState()
                }
            } else {
                items(elders) { elder ->
                    ElderCard(
                        elder = elder,
                        onCardClick = { onElderSelected(elder.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertSummaryBanner(alertCount: Int, onBannerClick: () -> Unit) {
    val containerColor = if (alertCount > 0) CriticalRedBG else SafeGreenBG
    val borderColor = if (alertCount > 0) CriticalRedBorder else SafeGreenBorder
    val contentColor = if (alertCount > 0) CriticalRed else SafeGreen
    val textColor = if (alertCount > 0) CriticalRedText else SafeGreenText
    val iconBgColor = if (alertCount > 0) CriticalRed else SafeGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onBannerClick)
            .padding(16.dp)
            .testTag("summary_alert_banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alertCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (alertCount > 0) "$alertCount Critical Anomalies" else "All Seniors Safe",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = if (alertCount > 0) "Detected in Heart Rate or Fall Sensors within the last 15 mins." else "All smartwatch biometrics are normal.",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.85f)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ElderCard(elder: Elder, onCardClick: () -> Unit) {
    data class StatusStyle(
        val label: String,
        val bg: Color,
        val text: Color,
        val border: Color
    )
    val statusStyle = when (elder.riskLevel) {
        RiskLevel.Critical -> StatusStyle("CRITICAL", CriticalRedBG, CriticalRed, CriticalRedBorder)
        RiskLevel.Caution -> StatusStyle("CAUTION", CautionYellowBG, CautionYellow, CautionYellowBorder)
        RiskLevel.Normal -> StatusStyle("NORMAL", SafeGreenBG, SafeGreen, SafeGreenBorder)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick)
            .testTag("elder_card_${elder.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Avatar placeholder representing individuals
            val initials = elder.name.split(" ").map { it.firstOrNull() ?: "" }.joinToString("")
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(statusStyle.text, shape = CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = elder.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${elder.age})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Battery indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val batteryIcon = when {
                            elder.batteryPercent > 80 -> Icons.Default.BatteryFull
                            elder.batteryPercent > 30 -> Icons.Default.BatteryChargingFull
                            else -> Icons.Default.BatteryAlert
                        }
                        val batteryColor = if (elder.batteryPercent < 20) CriticalRed else MaterialTheme.colorScheme.onSurfaceVariant
                        Icon(
                            imageVector = batteryIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = batteryColor
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${elder.batteryPercent}%", fontSize = 12.sp, color = batteryColor)
                    }
                    
                    // Sync stamp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Synced 2m ago", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Priority High-contrast Risk Level Badge in Rounded pill format
            Surface(
                color = statusStyle.bg,
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .border(1.dp, statusStyle.border, RoundedCornerShape(100.dp))
                    .testTag("risk_badge_${elder.id}")
            ) {
                Text(
                    text = statusStyle.label,
                    color = statusStyle.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyEldersState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "No client assignments loaded",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Tap '+' to assign care subjects.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

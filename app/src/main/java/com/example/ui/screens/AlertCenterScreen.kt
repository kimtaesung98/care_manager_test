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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Alert
import com.example.domain.model.AlertType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertCenterScreen(
    alerts: List<Alert>,
    onAlertTap: (String) -> Unit, // Navigation callback to elder detail
    onMarkAsRead: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var filterType by remember { mutableStateOf<AlertType?>(null) }
    var searchClientName by remember { mutableStateOf("") }
    var selectedDateRange by remember { mutableStateOf("ALL") } // "ALL", "TODAY", "WEEK"
    var showAdvancedFilters by remember { mutableStateOf(false) }

    val filteredAlerts = remember(alerts, selectedFilter, filterType, searchClientName, selectedDateRange) {
        alerts.filter { alert ->
            // 1. Segmented tabs filter
            val matchesTab = when (selectedFilter) {
                "CRITICAL" -> alert.isCritical
                "FALLS" -> alert.type == AlertType.FallDetected
                "HEART" -> alert.type == AlertType.AbnormalHeartRate
                "UNWORN" -> alert.type == AlertType.DeviceNotWorn
                else -> true
            }

            // 2. Advanced category type filter
            val matchesType = filterType == null || alert.type == filterType

            // 3. Client Name text search filter
            val matchesName = searchClientName.isEmpty() || alert.elderName.contains(searchClientName, ignoreCase = true)

            // 4. Date Range filter (using timestamp age boundary)
            val matchesDate = when (selectedDateRange) {
                "TODAY" -> {
                    val oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                    alert.timestamp >= oneDayAgo
                }
                "WEEK" -> {
                    val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                    alert.timestamp >= sevenDaysAgo
                }
                else -> true
            }

            matchesTab && matchesType && matchesName && matchesDate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Care Alert Center", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Horizontal Segmented Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "CRITICAL", "FALLS", "HEART", "UNWORN").forEach { tab ->
                    FilterTabButton(
                        label = tab,
                        isSelected = selectedFilter == tab,
                        onClick = { selectedFilter = tab }
                    )
                }
            }

            // Advanced Filters Accordion Row Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Refining ${filteredAlerts.size} Active Warnings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAdvancedFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_toggle_advanced_filters")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (showAdvancedFilters) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAdvancedFilters) "Hide Filters" else "Advanced Filters",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showAdvancedFilters) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Advanced Filters Expanding Body Box Panel
            AnimatedVisibility(visible = showAdvancedFilters) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Search Client Name",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = searchClientName,
                            onValueChange = { searchClientName = it },
                            placeholder = { Text("e.g. Eleanor", fontSize = 13.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("filters_search_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date Range choices
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Date limits",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("ALL", "TODAY", "WEEK").forEach { range ->
                                        val label = when (range) {
                                            "TODAY" -> "24h"
                                            "WEEK" -> "7d"
                                            else -> "All"
                                        }
                                        FilterTabButton(
                                            label = label,
                                            isSelected = selectedDateRange == range,
                                            onClick = { selectedDateRange = range }
                                        )
                                    }
                                }
                            }

                            // Alert Category picker
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "Category Type",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "All" to null,
                                        "Fall" to AlertType.FallDetected,
                                        "HR" to AlertType.AbnormalHeartRate
                                    ).forEach { (label, type) ->
                                        FilterTabButton(
                                            label = label,
                                            isSelected = filterType == type,
                                            onClick = { filterType = type }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clear resets action
                        TextButton(
                            onClick = {
                                searchClientName = ""
                                selectedDateRange = "ALL"
                                filterType = null
                                selectedFilter = "ALL"
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("btn_reset_filters")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset All Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alert List View
            if (filteredAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No alerts found under parameters",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Adjust active filters or search terms.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("alerts_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAlerts) { alert ->
                        AlertItemRow(
                            alert = alert,
                            onItemClick = { onAlertTap(alert.elderId) },
                            onMarkRead = { onMarkAsRead(alert.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("filter_tab_$label")
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun AlertItemRow(
    alert: Alert,
    onItemClick: () -> Unit,
    onMarkRead: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val formattedTime = formatter.format(Date(alert.timestamp))

    val alertTint = if (alert.isCritical) CriticalRed else CautionYellow
    val badgeBg = if (alert.isCritical) CriticalRedBG else CautionYellowBG
    val badgeText = if (alert.isCritical) CriticalRedText else CautionYellowText

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (alert.isRead) Color(0xFFE2E8F0).copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onItemClick)
            .testTag("alert_row_${alert.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Code Signifier Badge
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(alertTint, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Elder Identity
                Text(
                    text = alert.elderName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // High-visibility priority tag
                Box(
                    modifier = Modifier
                        .background(badgeBg, shape = RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (alert.isCritical) "Critical" else "Caution",
                        color = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Timestamp
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Warning Description Detail text
            Text(
                text = alert.message,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, end = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action link and dismiss controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint indicator to trigger navigation deep link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Show biometrics graph",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quickly dismiss/read warnings offline
                if (!alert.isRead) {
                    IconButton(
                        onClick = onMarkRead,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("mark_read_${alert.id}")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Acknowledge Warning",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

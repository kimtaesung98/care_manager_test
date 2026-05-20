package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Elder
import com.example.domain.model.VisitRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitChecklistScreen(
    initialElderId: String?,
    elders: List<Elder>,
    visits: List<VisitRecord>,
    cachedTasks: List<String> = emptyList(),
    isSyncing: Boolean = false,
    syncStatus: String? = null,
    onSyncOffline: () -> Unit = {},
    onSubmitRecord: (String, String, List<String>, String) -> Unit
) {
    val context = LocalContext.current

    // Automatically select the elder if specified by navigation parameter, or pick the first
    var selectedElder by remember {
        mutableStateOf(elders.find { it.id == initialElderId } ?: elders.firstOrNull())
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    // Core checklist items populated dynamically from Room cache
    val checklistItems = remember { mutableStateListOf<Pair<String, Boolean>>() }

    LaunchedEffect(cachedTasks) {
        checklistItems.clear()
        if (cachedTasks.isNotEmpty()) {
            checklistItems.addAll(cachedTasks.map { it to false })
        } else {
            checklistItems.addAll(listOf(
                "Verify blood pressure baseline vitals" to false,
                "Check prescription medication dispenser sync" to false,
                "Confirm household nutrition / dietary pantry" to false,
                "Assess cognitive / emotional wellness" to false,
                "Clean and recharge Wear OS smartwatch sensor" to false
            ))
        }
    }

    var memoInput by remember { mutableStateOf("") }

    val completedCount = checklistItems.count { it.second }
    val progress = if (checklistItems.isNotEmpty()) completedCount.toFloat() / checklistItems.size else 0f

    val unsyncedCount = remember(visits) { visits.count { !it.isSynced } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Visit Records", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = onSyncOffline,
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("action_sync_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            BadgedBox(
                                badge = {
                                    if (unsyncedCount > 0) {
                                        Badge { Text("$unsyncedCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = "Sync Offline Logs")
                            }
                        }
                    }
                }
            )
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
            // Optional sync status alert / message banner
            if (syncStatus != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_status_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                tint = MaterialTheme.colorScheme.secondary,
                                contentDescription = "Sync Info"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = syncStatus,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            // Dropdown patient selector
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Initiate New Visit Registry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Choose an elderly client and log visit tasks completed in person:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                if (elders.isEmpty()) {
                    Text("No clients assigned. Assign clients in Settings Screen first.", color = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.fillMaxWidth().testTag("visit_elder_dropdown")
                    ) {
                        OutlinedTextField(
                            value = selectedElder?.name ?: "Select Client",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            elders.forEach { elder ->
                                DropdownMenuItem(
                                    text = { Text(elder.name) },
                                    onClick = {
                                        selectedElder = elder
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Interactive checkboxes card
            if (selectedElder != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                            .testTag("visit_checklist_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Task Progress Checklist",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$completedCount / ${checklistItems.size} Done",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .testTag("visit_progress_bar")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // List tasks
                            checklistItems.forEachIndexed { index, pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .toggleable(
                                            value = pair.second,
                                            onValueChange = {
                                                checklistItems[index] = pair.first to it
                                            },
                                            role = Role.Checkbox
                                        )
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = pair.second,
                                        onCheckedChange = null, // Handled by toggleable Row modifier
                                        modifier = Modifier.testTag("visit_task_checkbox_$index")
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = pair.first,
                                        fontSize = 14.sp,
                                        color = if (pair.second) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Memo observation notes
                            OutlinedTextField(
                                value = memoInput,
                                onValueChange = { memoInput = it },
                                label = { Text("Worker's Clinical Observation Memo") },
                                placeholder = { Text("Write symptoms, mental states, or safety cautions...") },
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth().testTag("visit_memo_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val checkedList = checklistItems.filter { it.second }.map { it.first }
                                    onSubmitRecord(
                                        selectedElder!!.id,
                                        selectedElder!!.name,
                                        checkedList,
                                        memoInput
                                    )
                                    Toast.makeText(context, "Visit Log Synced & Posted to Go backend", Toast.LENGTH_SHORT).show()
                                    
                                    // Reset state
                                    memoInput = ""
                                    checklistItems.forEachIndexed { idx, p ->
                                        checklistItems[idx] = p.first to false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("submit_visit_log_btn")
                            ) {
                                Icon(Icons.Default.CloudUpload, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Session to Go Server", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Historial visit logs title
            item {
                Text(
                    text = "Historical Care Records Timeline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // Timeline cards List
            if (visits.isEmpty()) {
                item {
                    Text(
                        "No historic home visits completed on this terminal yet.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(visits) { visit ->
                    VisitHistoryItemCard(record = visit)
                }
            }
        }
    }
}

@Composable
fun VisitHistoryItemCard(record: VisitRecord) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("visit_history_card_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.elderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (record.isSynced) {
                        Surface(
                            color = Color(0xFFE6F4EA),
                            contentColor = Color(0xFF137333),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sync_badge_synced_${record.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = "Synced", modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Synced", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFEF7E0),
                            contentColor = Color(0xFFB06000),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sync_badge_pending_${record.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = "Pending Sync", modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Pending Sync", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Text(
                    text = record.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tasks executed summary
            Text(
                text = "Tasks Executed: " + if (record.completedTasks.isEmpty()) "Observation only" else record.completedTasks.joinToString(", "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            if (record.memo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${record.memo}\"",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentInd, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Logged by: ${record.workerName}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

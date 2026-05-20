package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AlertThresholds
import com.example.domain.model.Elder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    thresholds: AlertThresholds,
    assignedElders: List<Elder>,
    onSaveThresholds: (Int, Int, Int, Boolean) -> Unit,
    onAddNewClient: (String, Int, String, String, String) -> Unit,
    onRemoveClient: (String) -> Unit
) {
    val context = LocalContext.current

    // Internal states initialized with stored constants
    var hrMin by remember(thresholds) { mutableStateOf(thresholds.hrMin.toFloat()) }
    var hrMax by remember(thresholds) { mutableStateOf(thresholds.hrMax.toFloat()) }
    var spo2Min by remember(thresholds) { mutableStateOf(thresholds.spo2Min.toFloat()) }
    var fallDetectEnabled by remember(thresholds) { mutableStateOf(thresholds.isFallDetectionEnabled) }

    // Forms for adding/removing elders
    var newName by remember { mutableStateOf("") }
    var newAge by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }

    var isAddingActive by remember { mutableStateOf(false) }
    var isRemovingActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Care Control Settings", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Social Worker Identity Card
            WorkerIdentityCard()

            // 1. Wear OS Dynamic Alert Thresholds Config
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .testTag("thresholds_config_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wear OS Alert Thresholds", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        "Configure medical boundaries for all connected patient smartwatches:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // HR Min Slider
                    Text("Lower Pulse Bound: ${hrMin.toInt()} BPM", fontSize = 14.sp)
                    Slider(
                        value = hrMin,
                        onValueChange = { hrMin = it },
                        valueRange = 40f..80f,
                        steps = 8,
                        modifier = Modifier.testTag("hr_min_slider")
                    )

                    // HR Max Slider
                    Text("Upper Pulse Bound: ${hrMax.toInt()} BPM", fontSize = 14.sp)
                    Slider(
                        value = hrMax,
                        onValueChange = { hrMax = it },
                        valueRange = 90f..140f,
                        steps = 10,
                        modifier = Modifier.testTag("hr_max_slider")
                    )

                    // SpO2 Min Slider
                    Text("Hypoxia Oxygen Floor: ${spo2Min.toInt()}% SpO2", fontSize = 14.sp)
                    Slider(
                        value = spo2Min,
                        onValueChange = { spo2Min = it },
                        valueRange = 80f..95f,
                        steps = 15,
                        modifier = Modifier.testTag("spo2_min_slider")
                    )

                    // Fall detection toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Smartwatch Fall Detection", fontWeight = FontWeight.SemiBold)
                            Text("Enables high-G crash sensors on Wear OS 3", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = fallDetectEnabled,
                            onCheckedChange = { fallDetectEnabled = it },
                            modifier = Modifier.testTag("fall_detect_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onSaveThresholds(hrMin.toInt(), hrMax.toInt(), spo2Min.toInt(), fallDetectEnabled)
                            Toast.makeText(context, "Configurations pushed to Go backend and Wear OS devices.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_thresholds_btn")
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Sync Configurations", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Client Management Block (Add Assignments)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .testTag("patient_manager_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GroupAdd, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage assigned clients", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { isAddingActive = !isAddingActive }) {
                            Icon(if (isAddingActive) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                    }

                    AnimatedVisibility(visible = isAddingActive) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Elderly Patient Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("add_name_field")
                            )

                            OutlinedTextField(
                                value = newAge,
                                onValueChange = { if (it.all { c -> c.isDigit() }) newAge = it },
                                label = { Text("Age (years)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("add_age_field")
                            )

                            OutlinedTextField(
                                value = newPhone,
                                onValueChange = { newPhone = it },
                                label = { Text("Patient Phone Number (SMS)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("add_phone_field")
                            )

                            OutlinedTextField(
                                value = guardianName,
                                onValueChange = { guardianName = it },
                                label = { Text("Primary Guardian Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("add_guardian_field")
                            )

                            OutlinedTextField(
                                value = guardianPhone,
                                onValueChange = { guardianPhone = it },
                                label = { Text("Guardian Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("add_guardian_phone_field")
                            )

                            Button(
                                onClick = {
                                    val ageInt = newAge.toIntOrNull() ?: 70
                                    onAddNewClient(newName, ageInt, newPhone, guardianName, guardianPhone)
                                    Toast.makeText(context, "Added Client $newName successfully", Toast.LENGTH_SHORT).show()
                                    // Reset Fields
                                    newName = ""
                                    newAge = ""
                                    newPhone = ""
                                    guardianName = ""
                                    guardianPhone = ""
                                    isAddingActive = false
                                },
                                enabled = newName.isNotEmpty() && newAge.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().testTag("confirm_add_client_btn")
                            ) {
                                Text("Assign Client to my ID")
                            }
                        }
                    }
                }
            }

            // 3. Client Assignment Removals
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unassign Client Registry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { isRemovingActive = !isRemovingActive }) {
                            Icon(if (isRemovingActive) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                    }

                    AnimatedVisibility(visible = isRemovingActive) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            assignedElders.forEach { elder ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(elder.name, fontWeight = FontWeight.SemiBold)
                                    IconButton(
                                        onClick = {
                                            onRemoveClient(elder.id)
                                            Toast.makeText(context, "Unassigned ${elder.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("remove_btn_${elder.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete Assignment", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sign out Button
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Closing terminal session. Access key deleted.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("social_logout_btn")
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log out / Switch Facility ID")
            }
        }
    }
}

@Composable
fun WorkerIdentityCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AssignmentInd, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Sarah Jenkins",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Senior Geriatric Social Care Worker",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    "Terminal ID: SocialCare-#7391-East",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

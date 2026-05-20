package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Elder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    elders: List<Elder>
) {
    val context = LocalContext.current
    var selectedElderForSchedule by remember { mutableStateOf<Elder?>(null) }
    var visitDate by remember { mutableStateOf("") }
    var memoInput by remember { mutableStateOf("") }
    var isScheduleSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Contacts & Registration", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
            ) {
                item {
                    Text(
                        text = "Assigned Emergency Direct Directories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "One-tap telephone dials and SMS triggers to elderly and their guardians:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }

                if (elders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            Text("No assigned care subject directories.")
                        }
                    }
                } else {
                    items(elders) { elder ->
                        ContactCardItem(
                            elder = elder,
                            onDial = { targetLabel, num ->
                                Toast.makeText(context, "Initiating Voice Call to $targetLabel: $num", Toast.LENGTH_SHORT).show()
                            },
                            onSms = { targetLabel, num ->
                                Toast.makeText(context, "Drafting secure SMS to $targetLabel: $num", Toast.LENGTH_SHORT).show()
                            },
                            onVideo = { targetLabel, num ->
                                Toast.makeText(context, "Starting encrypted Video Consultation with $targetLabel: $num", Toast.LENGTH_SHORT).show()
                            },
                            onScheduleVisit = {
                                selectedElderForSchedule = elder
                                isScheduleSheetOpen = true
                            }
                        )
                    }
                }
            }

            // Simple In-App Schedule Sheet/Dialog (Material 3 Adaptive Overlay)
            if (isScheduleSheetOpen && selectedElderForSchedule != null) {
                AlertDialog(
                    onDismissRequest = { isScheduleSheetOpen = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Registered Visit for ${selectedElderForSchedule?.name} on $visitDate",
                                    Toast.LENGTH_LONG
                                ).show()
                                isScheduleSheetOpen = false
                                visitDate = ""
                                memoInput = ""
                            },
                            enabled = visitDate.isNotEmpty(),
                            modifier = Modifier.testTag("schedule_confirm_btn")
                        ) {
                            Text("Register Program")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isScheduleSheetOpen = false }) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Register Care Visit Session", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Client: ${selectedElderForSchedule?.name}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            OutlinedTextField(
                                value = visitDate,
                                onValueChange = { visitDate = it },
                                label = { Text("Target Schedule Date (e.g. 2026-05-25)") },
                                placeholder = { Text("YYYY-MM-DD") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                                modifier = Modifier.fillMaxWidth().testTag("schedule_date_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = memoInput,
                                onValueChange = { memoInput = it },
                                label = { Text("Internal Agenda / Memo Key") },
                                placeholder = { Text("Vitals followup, bring medical prescription...") },
                                leadingIcon = { Icon(Icons.Default.ModeEdit, null) },
                                modifier = Modifier.fillMaxWidth().testTag("schedule_memo_input"),
                                maxLines = 3
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ContactCardItem(
    elder: Elder,
    onDial: (String, String) -> Unit,
    onSms: (String, String) -> Unit,
    onVideo: (String, String) -> Unit,
    onScheduleVisit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("contact_card_${elder.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Elder row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = elder.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Phone: ${elder.phone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Elder direct action targets
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onDial(elder.name, elder.phone) },
                        modifier = Modifier.testTag("dial_elder_${elder.id}")
                    ) {
                        Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { onSms(elder.name, elder.phone) },
                        modifier = Modifier.testTag("sms_elder_${elder.id}")
                    ) {
                        Icon(Icons.Default.Sms, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(
                        onClick = { onVideo(elder.name, elder.phone) },
                        modifier = Modifier.testTag("video_elder_${elder.id}")
                    ) {
                        Icon(Icons.Default.VideoCall, null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Guardian row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = elder.guardianName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Guardian Direct: ${elder.guardianPhone}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Guardian direct action targets
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onDial("${elder.name}'s Guardian", elder.guardianPhone) },
                        modifier = Modifier.testTag("dial_guardian_${elder.id}")
                    ) {
                        Icon(Icons.Default.PhoneCallback, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { onSms("${elder.name}'s Guardian", elder.guardianPhone) },
                        modifier = Modifier.testTag("sms_guardian_${elder.id}")
                    ) {
                        Icon(Icons.Default.Sms, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visit registration trigger
            Button(
                onClick = onScheduleVisit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("schedule_visit_btn_${elder.id}")
            ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Care visit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

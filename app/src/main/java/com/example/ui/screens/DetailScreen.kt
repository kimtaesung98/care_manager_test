package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BiometricData
import com.example.domain.model.Elder
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    elder: Elder?,
    biometrics: BiometricData?,
    limitHrMin: Int,
    limitHrMax: Int,
    limitSpo2Min: Int,
    onNavigateToVisit: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    if (elder == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(elder.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { onNavigateToVisit(elder.id) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("detail_start_visit_btn")
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Visit", color = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patient Quick Attributes Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CLIENT STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = elder.riskLevel.name.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = when (elder.riskLevel) {
                            com.example.domain.model.RiskLevel.Critical -> CriticalRed
                            com.example.domain.model.RiskLevel.Caution -> CautionYellow
                            com.example.domain.model.RiskLevel.Normal -> SafeGreen
                        }
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("SMARTWATCH BATS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (elder.batteryPercent < 20) Icons.Default.BatteryAlert else Icons.Default.Battery4Bar,
                            contentDescription = null,
                            tint = if (elder.batteryPercent < 20) CriticalRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${elder.batteryPercent}% battery", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Realtime Heart Rate Section with Canvas line chart
            HeartRateCard(
                biometrics = biometrics,
                minLimit = limitHrMin,
                maxLimit = limitHrMax
            )

            // Blood Oxygen Section with saturation gauge
            BloodOxygenSpO2Card(
                biometrics = biometrics,
                minLimit = limitSpo2Min
            )

            // Physical Activity Section (Steps and Goal)
            ActivityStepsCard(biometrics = biometrics)

            // Sleep Pattern tracker
            SleepTrackerCard(biometrics = biometrics)
        }
    }
}

@Composable
fun HeartRateCard(biometrics: BiometricData?, minLimit: Int, maxLimit: Int) {
    val heartRates = biometrics?.heartRateSeries ?: emptyList()
    val lastHr = heartRates.lastOrNull()?.bpm ?: 0
    val isAbnormal = lastHr > maxLimit || (lastHr < minLimit && lastHr > 0)

    val alarmColor = if (isAbnormal) CriticalRed else SafeGreen

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("heart_rate_detail_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Heart Rate Monitoring", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (lastHr > 0) "$lastHr bpm" else "--",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = alarmColor
                    )
                }
            }

            if (isAbnormal && lastHr > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CriticalRed.copy(alpha = 0.12f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lastHr > maxLimit) "Exceeds upper limit ($maxLimit)" else "Below lower limit ($minLimit)",
                        color = CriticalRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Canvas Drawing for heart rate line chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (heartRates.size < 2) {
                    Text("Sensing biometrics stream...", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxBpm = 150
                        val minBpm = 50
                        val bpmRange = maxBpm - minBpm

                        val width = size.width
                        val height = size.height

                        val stepX = width / (heartRates.size - 1)
                        val points = heartRates.mapIndexed { idx, pt ->
                            val normalizedY = (pt.bpm - minBpm).toFloat() / bpmRange
                            val x = idx * stepX
                            val y = height - (normalizedY * height)
                            Offset(x, y)
                        }

                        // Draw abnormal background boundary
                        val limitUpperY = height - (((maxLimit - minBpm).toFloat() / bpmRange) * height)
                        val limitLowerY = height - (((minLimit - minBpm).toFloat() / bpmRange) * height)
                        
                        // Upper bounds line
                        drawLine(
                            color = CriticalRed.copy(alpha = 0.4f),
                            start = Offset(0f, limitUpperY),
                            end = Offset(width, limitUpperY),
                            strokeWidth = 2f,
                            pathEffect = null
                        )

                        // Base connecting graph lines
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = alarmColor,
                            style = Stroke(width = 4.dp.toPx())
                        )

                        // Draw endpoint nodes
                        points.forEachIndexed { i, p ->
                            val bpmVal = heartRates[i].bpm
                            val isNodeAbnormal = bpmVal > maxLimit || bpmVal < minLimit
                            drawCircle(
                                color = if (isNodeAbnormal) CriticalRed else SafeGreen,
                                radius = 4.dp.toPx(),
                                center = p
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Historical measurements (Last 24 hrs)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Limits: $minLimit - $maxLimit BPM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BloodOxygenSpO2Card(biometrics: BiometricData?, minLimit: Int) {
    val spo2Series = biometrics?.spO2Series ?: emptyList()
    val lastSpo2 = spo2Series.lastOrNull()?.percentage ?: 96
    val isHypoxia = lastSpo2 < minLimit
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("spo2_detail_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Oxygen Saturation SpO2", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Safe breathing is the baseline of elderly health monitoring.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isHypoxia) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CriticalRed.copy(alpha = 0.15f))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HYPOXIA WARNING", color = CriticalRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Oxygen circular gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    Canvas(modifier = Modifier.size(70.dp)) {
                        // Empty background arc
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx())
                        )
                        // Full arc is 260 deg max
                        val sweep = (lastSpo2 / 100f) * 260f
                        drawArc(
                            color = if (isHypoxia) CriticalRed else primaryColor,
                            startAngle = -220f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$lastSpo2%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isHypoxia) CriticalRed else primaryColor)
                        Text("SpO2", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic live-updating trend line chart for SpO2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (spo2Series.size < 2) {
                    Text("Sensing SpO2 biometrics stream...", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxSpo2 = 100
                        val minSpo2 = 80
                        val range = maxSpo2 - minSpo2

                        val width = size.width
                        val height = size.height

                        val stepX = width / (spo2Series.size - 1)
                        val points = spo2Series.mapIndexed { idx, pt ->
                            val normalizedY = ((pt.percentage.coerceIn(minSpo2, maxSpo2)) - minSpo2).toFloat() / range
                            val x = idx * stepX
                            val y = height - (normalizedY * height)
                            Offset(x, y)
                        }

                        // Draw abnormal hypoxia boundary line (e.g., 90%)
                        val limitY = height - (((minLimit - minSpo2).toFloat() / range) * height)
                        drawLine(
                            color = CriticalRed.copy(alpha = 0.4f),
                            start = Offset(0f, limitY),
                            end = Offset(width, limitY),
                            strokeWidth = 2f,
                            pathEffect = null
                        )

                        // Draw continuous SpO2 baseline path
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 4.dp.toPx())
                        )

                        // Draw connection nodes
                        points.forEachIndexed { i, p ->
                            val percentageVal = spo2Series[i].percentage
                            val isNodeAbnormal = percentageVal < minLimit
                            drawCircle(
                                color = if (isNodeAbnormal) CriticalRed else primaryColor,
                                radius = 4.dp.toPx(),
                                center = p
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Oxygen timeline (Last 24 hrs)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Critical Limit: < $minLimit%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActivityStepsCard(biometrics: BiometricData?) {
    val steps = biometrics?.stepCount ?: 0
    val progress = (steps / 10000f).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = SafeGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Smartwatch Step Count", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$steps steps", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Daily Goal: 10,000", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                color = SafeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun SleepTrackerCard(biometrics: BiometricData?) {
    val sleepHours = biometrics?.sleepHours ?: 7.0
    val sleepQuality = biometrics?.sleepQuality ?: "Normal"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Snooze, contentDescription = null, tint = CautionYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sleep Structure", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Quality: $sleepQuality", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("$sleepHours hrs", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CautionYellow)
                Text("Wear OS logged", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

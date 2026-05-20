package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Alert
import com.example.ui.theme.CriticalRed

@Composable
fun CriticalAlertOverlay(
    alert: Alert,
    onDismiss: () -> Unit,
    onCallGuardian: (String) -> Unit
) {
    // Elegant pulsing animation for eye safety & urgent action
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(24.dp)
            .testTag("critical_alert_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsing Alarm Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .background(CriticalRed.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "Alarm Indicator",
                    tint = CriticalRed,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CRITICAL ALERT",
                color = CriticalRed,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Patient Card Details
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = alert.elderName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Device Anomaly: ${alert.type}",
                        color = CriticalRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = alert.message,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Targets
            Button(
                onClick = { onCallGuardian(alert.elderId) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("critical_call_guardian_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Contact Guardian", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Simulated 911 Dial */ },
                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("critical_call_911_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Emergency, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dial Emergency Services (911)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("critical_ack_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Acknowledge & Mute Tracker", fontSize = 16.sp)
            }
        }
    }
}

package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.domain.model.AlertType
import java.util.UUID

/**
 * Service to handle incoming Google Firebase Cloud Messages (FCM).
 * Implements high-importance notifications and full-screen overlay intents
 * for responding in real time to senior health critical events.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Notification Received: ${remoteMessage.messageId}")

        // Retrieve biometric parameters from FCM message payload data
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val elderId = data["elder_id"] ?: "elder_1"
            val elderName = data["elder_name"] ?: "George Henderson"
            val alertTypeStr = data["alert_type"] ?: "FallDetected"
            val messageText = data["message"] ?: "CRITICAL: Fall registered by Wear OS."
            val isCritical = data["is_critical"]?.toBoolean() ?: true

            sendHighImportanceNotification(
                context = this,
                elderId = elderId,
                elderName = elderName,
                title = "HEALTH RISK DETECTED: $elderName",
                messageText = messageText,
                isCritical = isCritical
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Assigned Device FCM Token: $token")
        // In real app, push this token to Go API service:
        // apiService.updateWorkerFcmToken(workerId, token)
    }

    companion object {
        private const val TAG = "ElderFcmService"
        const val EMERGENCY_CHANNEL_ID = "emergency_anomalies_alerts"
        const val GENERAL_CHANNEL_ID = "general_sync_notifications"

        fun sendHighImportanceNotification(
            context: Context,
            elderId: String,
            elderName: String,
            title: String,
            messageText: String,
            isCritical: Boolean
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            // 1. Create High-Priority Channels for Android Oreo (API 26) and newer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = if (isCritical) EMERGENCY_CHANNEL_ID else GENERAL_CHANNEL_ID
                val channelName = if (isCritical) {
                    "Critical Elderly Emergency Anomalies"
                } else {
                    "General Social Worker Updates"
                }
                
                val importance = if (isCritical) {
                    NotificationManager.IMPORTANCE_HIGH
                } else {
                    NotificationManager.IMPORTANCE_DEFAULT
                }

                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = "FCM push-channels alerting social worker about critical care events"
                    enableLights(true)
                    enableVibration(true)
                    if (isCritical) {
                        setSound(
                            soundUri,
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setShowBadge(true)
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }

            // 2. Prep Deep link Intent to open detail screen for affected elder
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "elder_detail")
                putExtra("ELDER_ID", elderId)
            }

            // Flag immutable support for API 31+ Android models
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                UUID.randomUUID().hashCode(),
                openIntent,
                flags
            )

            // 3. Build Push Notification properties
            val builder = NotificationCompat.Builder(
                context,
                if (isCritical) EMERGENCY_CHANNEL_ID else GENERAL_CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.stat_sys_warning) // fallback
                .setContentTitle(title)
                .setContentText(messageText)
                .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(if (isCritical) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            if (isCritical) {
                builder.setSound(soundUri)
                builder.setFullScreenIntent(pendingIntent, true) // Launches immediately overlay
            }

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
            
        // Setup channels proactively during application startup
        fun setupNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                val emergencyChannel = NotificationChannel(
                    EMERGENCY_CHANNEL_ID,
                    "Elderly Emergency Warnings",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "FCM push-channels alerting social worker about critical care events"
                    enableVibration(true)
                    enableLights(true)
                }
                
                val generalChannel = NotificationChannel(
                    GENERAL_CHANNEL_ID,
                    "Social Care Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General task and visit timing updates"
                }

                notificationManager.createNotificationChannel(emergencyChannel)
                notificationManager.createNotificationChannel(generalChannel)
            }
        }
    }
}

// --- Dynamic Jetpack FCM Compilers Mock Stubs ---
open class FirebaseMessagingService : android.app.Service() {
    override fun onBind(intent: Intent?): android.os.IBinder? = null
    open fun onMessageReceived(remoteMessage: RemoteMessage) {}
    open fun onNewToken(token: String) {}
}

class RemoteMessage {
    val messageId: String? = "mock_fcm_message_id"
    val data: Map<String, String> = emptyMap()
}

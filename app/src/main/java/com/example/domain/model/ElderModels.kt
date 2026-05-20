package com.example.domain.model

data class Elder(
    val id: String,
    val name: String,
    val age: Int,
    val lastSyncTime: Long,
    val riskLevel: RiskLevel,
    val batteryPercent: Int,
    val phone: String,
    val guardianName: String,
    val guardianPhone: String,
    val imageIndex: Int = 0 // Used to render unique graphic avatars offline
)

enum class RiskLevel {
    Normal, Caution, Critical
}

data class BiometricData(
    val elderId: String,
    val heartRateSeries: List<HeartRatePoint>,
    val spO2Series: List<SpO2Point>,
    val stepCount: Int,
    val sleepHours: Double,
    val sleepQuality: String,
    val timestamp: Long,
    val batteryPercent: Int
)

data class HeartRatePoint(
    val timeLabel: String,
    val bpm: Int
)

data class SpO2Point(
    val timeLabel: String,
    val percentage: Int
)

data class Alert(
    val id: String,
    val elderId: String,
    val elderName: String,
    val type: AlertType,
    val message: String,
    val timestamp: Long,
    val isCritical: Boolean,
    val isRead: Boolean = false
)

enum class AlertType {
    AbnormalHeartRate, FallDetected, DeviceNotWorn, LowSpO2
}

data class VisitRecord(
    val id: String,
    val elderId: String,
    val elderName: String,
    val date: String,
    val completedTasks: List<String>,
    val memo: String,
    val workerName: String,
    val isSynced: Boolean = true
)

data class AlertThresholds(
    val hrMin: Int = 60,
    val hrMax: Int = 110,
    val spo2Min: Int = 90,
    val isFallDetectionEnabled: Boolean = true
)

package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.domain.model.Alert
import com.example.domain.model.AlertType
import com.example.domain.model.Elder
import com.example.domain.model.RiskLevel
import com.example.domain.model.VisitRecord

@Entity(tableName = "elders")
data class ElderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val lastSyncTime: Long,
    val riskLevel: String, // Normal, Caution, Critical
    val batteryPercent: Int,
    val phone: String,
    val guardianName: String,
    val guardianPhone: String,
    val imageIndex: Int
) {
    fun toDomain(): Elder = Elder(
        id = id,
        name = name,
        age = age,
        lastSyncTime = lastSyncTime,
        riskLevel = RiskLevel.valueOf(riskLevel),
        batteryPercent = batteryPercent,
        phone = phone,
        guardianName = guardianName,
        guardianPhone = guardianPhone,
        imageIndex = imageIndex
    )

    companion object {
        fun fromDomain(elder: Elder) = ElderEntity(
            id = elder.id,
            name = elder.name,
            age = elder.age,
            lastSyncTime = elder.lastSyncTime,
            riskLevel = elder.riskLevel.name,
            batteryPercent = elder.batteryPercent,
            phone = elder.phone,
            guardianName = elder.guardianName,
            guardianPhone = elder.guardianPhone,
            imageIndex = elder.imageIndex
        )
    }
}

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val elderId: String,
    val elderName: String,
    val type: String, // AbnormalHeartRate, FallDetected, DeviceNotWorn, LowSpO2
    val message: String,
    val timestamp: Long,
    val isCritical: Boolean,
    val isRead: Boolean
) {
    fun toDomain(): Alert = Alert(
        id = id,
        elderId = elderId,
        elderName = elderName,
        type = AlertType.valueOf(type),
        message = message,
        timestamp = timestamp,
        isCritical = isCritical,
        isRead = isRead
    )

    companion object {
        fun fromDomain(alert: Alert) = AlertEntity(
            id = alert.id,
            elderId = alert.elderId,
            elderName = alert.elderName,
            type = alert.type.name,
            message = alert.message,
            timestamp = alert.timestamp,
            isCritical = alert.isCritical,
            isRead = alert.isRead
        )
    }
}

@Entity(tableName = "visits")
data class VisitRecordEntity(
    @PrimaryKey val id: String,
    val elderId: String,
    val elderName: String,
    val date: String,
    val completedTasks: String, // Comma separated list of tasks
    val memo: String,
    val workerName: String,
    val isSynced: Boolean = true
) {
    fun toDomain(): VisitRecord = VisitRecord(
        id = id,
        elderId = elderId,
        elderName = elderName,
        date = date,
        completedTasks = if (completedTasks.isEmpty()) emptyList() else completedTasks.split("&&&"),
        memo = memo,
        workerName = workerName,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(record: VisitRecord) = VisitRecordEntity(
            id = record.id,
            elderId = record.elderId,
            elderName = record.elderName,
            date = record.date,
            completedTasks = record.completedTasks.joinToString("&&&"),
            memo = record.memo,
            workerName = record.workerName,
            isSynced = record.isSynced
        )
    }
}

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val taskName: String,
    val isDefault: Boolean = true
)

@Entity(tableName = "thresholds")
data class ThresholdsEntity(
    @PrimaryKey val id: Int = 0,
    val hrMin: Int,
    val hrMax: Int,
    val spo2Min: Int,
    val isFallDetectionEnabled: Boolean
)

class DatabaseConverters {
    @TypeConverter
    fun stringToList(value: String?): List<String>? {
        return value?.split("&&&")?.map { it.trim() }
    }

    @TypeConverter
    fun listToString(value: List<String>?): String? {
        return value?.joinToString("&&&")
    }
}

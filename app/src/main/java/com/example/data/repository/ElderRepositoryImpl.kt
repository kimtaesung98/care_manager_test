package com.example.data.repository
import com.example.data.api.RetrofitClient
import com.example.data.api.VisitRecordRequest
import com.example.data.database.*
import com.example.domain.model.*
import com.example.domain.repository.ElderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ElderRepositoryImpl(
    private val dao: ElderDao
) : ElderRepository {

    init {
        // Run database initialization on startup
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfEmpty()
        }
    }

    override fun getElders(): Flow<List<Elder>> {
        return dao.getAllElders().map { list -> list.map { it.toDomain() } }
    }

    override fun getElderById(id: String): Flow<Elder?> {
        return dao.getElderById(id).map { it?.toDomain() }
    }

    override fun getAlerts(): Flow<List<Alert>> {
        return dao.getAllAlerts().map { list -> list.map { it.toDomain() } }
    }

    override fun getVisitRecords(): Flow<List<VisitRecord>> {
        return dao.getAllVisits().map { list -> list.map { it.toDomain() } }
    }

    override fun getThresholds(): Flow<AlertThresholds> {
        return dao.getThresholds().map { entity ->
            if (entity == null) {
                AlertThresholds()
            } else {
                AlertThresholds(
                    hrMin = entity.hrMin,
                    hrMax = entity.hrMax,
                    spo2Min = entity.spo2Min,
                    isFallDetectionEnabled = entity.isFallDetectionEnabled
                )
            }
        }
    }

    override suspend fun markAlertAsRead(alertId: String) {
        dao.markAlertAsRead(alertId)
    }

    override suspend fun insertVisitRecord(record: VisitRecord): Boolean {
        var isUploaded = false
        try {
            val response = RetrofitClient.apiService.submitVisitRecord(
                VisitRecordRequest(
                    recordId = record.id,
                    elderId = record.elderId,
                    date = record.date,
                    tasksFinished = record.completedTasks,
                    workerNotes = record.memo,
                    workerId = "Sarah Jenkins"
                )
            )
            if (response.success) {
                isUploaded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val entity = VisitRecordEntity.fromDomain(record.copy(isSynced = isUploaded))
        dao.insertVisit(entity)
        return isUploaded
    }

    override fun getChecklistItems(): Flow<List<String>> {
        return dao.getAllChecklistItems().map { list -> list.map { it.taskName } }
    }

    override suspend fun syncPendingVisits(): Int {
        var syncedCount = 0
        try {
            val unsynced = dao.getUnsyncedVisits()
            for (visit in unsynced) {
                val response = RetrofitClient.apiService.submitVisitRecord(
                    VisitRecordRequest(
                        recordId = visit.id,
                        elderId = visit.elderId,
                        date = visit.date,
                        tasksFinished = if (visit.completedTasks.isEmpty()) emptyList() else visit.completedTasks.split("&&&"),
                        workerNotes = visit.memo,
                        workerId = "Sarah Jenkins"
                    )
                )
                if (response.success) {
                    dao.markVisitSynced(visit.id)
                    syncedCount++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return syncedCount
    }

    override suspend fun updateThresholds(thresholds: AlertThresholds): Boolean {
        // try {
        //   apiService.updateElderThresholds(...)
        // } catch (e: Exception) { ... }
        dao.insertThresholds(
            ThresholdsEntity(
                id = 0,
                hrMin = thresholds.hrMin,
                hrMax = thresholds.hrMax,
                spo2Min = thresholds.spo2Min,
                isFallDetectionEnabled = thresholds.isFallDetectionEnabled
            )
        )
        return true
    }

    override suspend fun addElder(elder: Elder) {
        dao.insertElder(ElderEntity.fromDomain(elder))
    }

    override suspend fun removeElder(id: String) {
        dao.deleteElderById(id)
    }

    override suspend fun triggerMockServerAlert(alert: Alert) {
        dao.insertAlert(AlertEntity.fromDomain(alert))
        
        // Also update the elder's risk status internally if the alert is critical
        val elderFlow = dao.getElderById(alert.elderId)
        // Since we are inside a suspend function, we can take a quick look or fetch once
        CoroutineScope(Dispatchers.IO).launch {
            elderFlow.collect { entity ->
                if (entity != null) {
                    val currentRisk = RiskLevel.valueOf(entity.riskLevel)
                    val newRisk = if (alert.isCritical) RiskLevel.Critical else RiskLevel.Caution
                    if (currentRisk != newRisk) {
                        dao.insertElder(entity.copy(riskLevel = newRisk.name))
                    }
                }
            }
        }
    }

    // Prepopulate some rich demo data on fresh startup
    private suspend fun prepopulateIfEmpty() {
        // Note: we can use a small flow read to check if empty
        // In practice, since Room reads are flows, we can query raw
        val randomElders = listOf(
            ElderEntity(
                id = "elder_1",
                name = "George Henderson",
                age = 82,
                lastSyncTime = System.currentTimeMillis() - 2 * 60 * 1000, // 2 mins ago
                riskLevel = "Critical", // Critical anomaly detected!
                batteryPercent = 74,
                phone = "+1 (555) 019-2831",
                guardianName = "Emma Henderson (Daughter)",
                guardianPhone = "+1 (555) 019-2832",
                imageIndex = 0
            ),
            ElderEntity(
                id = "elder_2",
                name = "Margaret Vance",
                age = 79,
                lastSyncTime = System.currentTimeMillis() - 15 * 60 * 1000, // 15 mins ago
                riskLevel = "Caution", // Caution warning!
                batteryPercent = 42,
                phone = "+1 (555) 034-7290",
                guardianName = "Robert Vance (Son)",
                guardianPhone = "+1 (555) 034-7291",
                imageIndex = 1
            ),
            ElderEntity(
                id = "elder_3",
                name = "Arthur Pendelton",
                age = 85,
                lastSyncTime = System.currentTimeMillis() - 5 * 60 * 1000, // 5 mins ago
                riskLevel = "Normal",
                batteryPercent = 95,
                phone = "+1 (555) 091-1188",
                guardianName = "William Pendelton (Son)",
                guardianPhone = "+1 (555) 091-1189",
                imageIndex = 2
            ),
            ElderEntity(
                id = "elder_4",
                name = "Eleanor Rigby",
                age = 88,
                lastSyncTime = System.currentTimeMillis() - 120 * 60 * 1000, // 2h ago (device unworn maybe?)
                riskLevel = "Caution",
                batteryPercent = 12, // Critically low battery or not worn
                phone = "+1 (555) 042-3392",
                guardianName = "Father McKenzie (Pastor)",
                guardianPhone = "+1 (555) 042-3393",
                imageIndex = 3
            )
        )

        dao.insertElders(randomElders)

        // Prepopulate Alerts
        val initialAlerts = listOf(
            AlertEntity(
                id = "alert_101",
                elderId = "elder_1",
                elderName = "George Henderson",
                type = "FallDetected",
                message = "CRITICAL: Heavy Fall impact registered by Wear OS 3. Emergency broadcast sent.",
                timestamp = System.currentTimeMillis() - 10 * 60 * 1000, // 10m ago
                isCritical = true,
                isRead = false
            ),
            AlertEntity(
                id = "alert_102",
                elderId = "elder_1",
                elderName = "George Henderson",
                type = "AbnormalHeartRate",
                message = "Heart rate spiked to 142 bpm (higher limit 110bpm threshold exceeded).",
                timestamp = System.currentTimeMillis() - 9 * 60 * 1000, // 9m ago
                isCritical = true,
                isRead = false
            ),
            AlertEntity(
                id = "alert_103",
                elderId = "elder_2",
                elderName = "Margaret Vance",
                type = "LowSpO2",
                message = "Blood Oxygen drops below safe levels (87% SpO2 registered).",
                timestamp = System.currentTimeMillis() - 35 * 60 * 1000,
                isCritical = false,
                isRead = true
            ),
            AlertEntity(
                id = "alert_104",
                elderId = "elder_4",
                elderName = "Eleanor Rigby",
                type = "DeviceNotWorn",
                message = "Wear OS Sensor disconnected. Device not worn for >2 hours.",
                timestamp = System.currentTimeMillis() - 120 * 60 * 1000,
                isCritical = false,
                isRead = false
            )
        )
        dao.insertAlerts(initialAlerts)

        // Prepopulate visit checklists/records
        val initialVisits = listOf(
            VisitRecordEntity(
                id = "visit_v1",
                elderId = "elder_3",
                elderName = "Arthur Pendelton",
                date = "2026-05-18",
                completedTasks = "Vitals Check&&&Medication Refill&&&Housework Assistance",
                memo = "Arthur was in excellent spirits. Blood pressure was normal. He completed his daily physical therapy walks.",
                workerName = "Sarah Jenkins"
            ),
            VisitRecordEntity(
                id = "visit_v2",
                elderId = "elder_2",
                elderName = "Margaret Vance",
                date = "2026-05-15",
                completedTasks = "Vitals Check&&&Cognitive Exercise",
                memo = "Margaret showed early signs of fatigue. Reminded her son Robert to keep water bottles handy near her reading spot.",
                workerName = "Sarah Jenkins"
            )
        )
        for (v in initialVisits) {
            dao.insertVisit(v)
        }

        // Set default thresholds
        dao.insertThresholds(
            ThresholdsEntity(
                id = 0,
                hrMin = 55,
                hrMax = 115,
                spo2Min = 89,
                isFallDetectionEnabled = true
            )
        )

        // Prepopulate checklist items from database cache
        val defaultChecklist = listOf(
            ChecklistItemEntity("item_1", "Verify blood pressure baseline vitals"),
            ChecklistItemEntity("item_2", "Check prescription medication dispenser sync"),
            ChecklistItemEntity("item_3", "Confirm household nutrition / dietary pantry"),
            ChecklistItemEntity("item_4", "Assess cognitive / emotional wellness"),
            ChecklistItemEntity("item_5", "Clean and recharge Wear OS smartwatch sensor")
        )
        dao.insertChecklistItems(defaultChecklist)
    }
}

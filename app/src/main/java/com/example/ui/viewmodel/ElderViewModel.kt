package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ElderDatabase
import com.example.data.repository.ElderRepositoryImpl
import com.example.domain.model.*
import com.example.domain.repository.ElderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ElderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ElderRepository

    // UI States
    val elders: StateFlow<List<Elder>>
    val alerts: StateFlow<List<Alert>>
    val visits: StateFlow<List<VisitRecord>>
    val thresholds: StateFlow<AlertThresholds>
    val cachedChecklistItems: StateFlow<List<String>>

    // Detail biometric states
    private val _selectedElderBiometrics = MutableStateFlow<BiometricData?>(null)
    val selectedElderBiometrics: StateFlow<BiometricData?> = _selectedElderBiometrics.asStateFlow()

    // Sync statuses
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    // Simulator or active critical alarms
    private val _activeCriticalOverlayAlert = MutableStateFlow<Alert?>(null)
    val activeCriticalOverlayAlert: StateFlow<Alert?> = _activeCriticalOverlayAlert.asStateFlow()

    init {
        val database = ElderDatabase.getDatabase(application)
        repository = ElderRepositoryImpl(database.elderDao())

        // Connect flows directly from Room reactive cache
        elders = repository.getElders()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        alerts = repository.getAlerts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        visits = repository.getVisitRecords()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        thresholds = repository.getThresholds()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertThresholds())

        cachedChecklistItems = repository.getChecklistItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Start dynamic Wear OS streaming simulations in background
        startWebSocketWearSimulation()

        // Auto-sync offline logs on startup
        syncOfflineData()
    }

    // Load detailed biometric graph state for elder
    fun selectElderForDetail(elderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Read threshold limit to highlighting abnormal spikes
            val limits = thresholds.value
            
            // Build historical series data
            val heartRateSeries = mutableListOf<HeartRatePoint>()
            val spO2Series = mutableListOf<SpO2Point>()
            
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            // Seed with historical points
            for (i in 10 downTo 1) {
                calendar.add(Calendar.MINUTE, -15)
                val timeStr = format.format(calendar.time)
                
                // Introduce natural fluctuation
                val baseHr = when (elderId) {
                    "elder_1" -> if (i == 1 || i == 2) 130 else 85 // high spike
                    "elder_2" -> 72
                    else -> 68
                }
                heartRateSeries.add(HeartRatePoint(timeStr, baseHr + (-3..3).random()))

                val baseSpo2 = when (elderId) {
                    "elder_2" -> if (i == 4) 87 else 94 // abnormal dip
                    else -> 98
                }
                spO2Series.add(SpO2Point(timeStr, baseSpo2 + (-1..1).random()))
            }

            _selectedElderBiometrics.update {
                BiometricData(
                    elderId = elderId,
                    heartRateSeries = heartRateSeries,
                    spO2Series = spO2Series,
                    stepCount = if (elderId == "elder_3") 8421 else 3120,
                    sleepHours = if (elderId == "elder_1") 5.2 else 7.4,
                    sleepQuality = if (elderId == "elder_1") "Restless (Deep 1.1h)" else "Good (Deep 2.4h)",
                    timestamp = System.currentTimeMillis(),
                    batteryPercent = if (elderId == "elder_4") 12 else 88
                )
            }
        }
    }

    fun syncOfflineData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Synchronizing with Go backend..."
            val syncedCount = repository.syncPendingVisits()
            _isSyncing.value = false
            _syncStatus.value = if (syncedCount > 0) {
                "Successfully uploaded $syncedCount pending visit logs to Go backend!"
            } else {
                "Sync complete. All logs are up to date."
            }
            delay(3000)
            _syncStatus.value = null
        }
    }

    // Submit a completed home checklist
    fun submitVisitChecklist(
        elderId: String,
        elderName: String,
        tasks: List<String>,
        memoInput: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val record = VisitRecord(
                id = "visit_v_${UUID.randomUUID()}",
                elderId = elderId,
                elderName = elderName,
                date = simpleDate,
                completedTasks = tasks,
                memo = memoInput,
                workerName = "Sarah Jenkins" // Authenticated Worker
            )
            repository.insertVisitRecord(record)
        }
    }

    fun saveConfiguredThresholds(minHr: Int, maxHr: Int, minSpO2: Int, fallEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = AlertThresholds(
                hrMin = minHr,
                hrMax = maxHr,
                spo2Min = minSpO2,
                isFallDetectionEnabled = fallEnabled
            )
            repository.updateThresholds(config)
        }
    }

    fun addNewClient(name: String, age: Int, phone: String, guardian: String, guardianPhone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newElder = Elder(
                id = "elder_${UUID.randomUUID()}",
                name = name,
                age = age,
                lastSyncTime = System.currentTimeMillis(),
                riskLevel = RiskLevel.Normal,
                batteryPercent = 100,
                phone = phone,
                guardianName = guardian,
                guardianPhone = guardianPhone,
                imageIndex = (0..3).random()
            )
            repository.addElder(newElder)
        }
    }

    fun removeClient(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeElder(id)
        }
    }

    fun markWarningAsRead(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAlertAsRead(id)
        }
    }

    fun clearActiveCriticalOverlay() {
        _activeCriticalOverlayAlert.value = null
    }

    // Simulates an FCM critical payload received at runtime (social worker demo button)
    fun simulateIncomingFcmCriticalAlert(type: AlertType) {
        viewModelScope.launch(Dispatchers.IO) {
            val sampleElder = elders.value.firstOrNull() ?: Elder(
                id = "elder_1", name = "George Henderson", age = 82,
                lastSyncTime = System.currentTimeMillis(), riskLevel = RiskLevel.Critical,
                batteryPercent = 74, phone = "+1 (555) 019-2831",
                guardianName = "Emma Henderson", guardianPhone = "+1 (555) 019-2832"
            )

            val alertMsg = when (type) {
                AlertType.FallDetected -> "EMERGENCY: Extreme acceleration registered! Sudden shock indicating an elderly FALL EVENT."
                AlertType.AbnormalHeartRate -> "CRITICAL HEART RATE: Tachycardia alert. Heart rate spike at 154 BPM."
                AlertType.LowSpO2 -> "CRITICAL SATURATION: Hypoxia alert. SpO2 falls to 84%."
                AlertType.DeviceNotWorn -> "WARNING: Device sensor detached. Active monitoring lost."
            }

            val mockAlert = Alert(
                id = "fcm_alert_${UUID.randomUUID()}",
                elderId = sampleElder.id,
                elderName = sampleElder.name,
                type = type,
                message = alertMsg,
                timestamp = System.currentTimeMillis(),
                isCritical = type == AlertType.FallDetected || type == AlertType.AbnormalHeartRate
            )

            // Dynamic full screen popup overlay triggers immediately inside app UI!
            if (mockAlert.isCritical) {
                _activeCriticalOverlayAlert.value = mockAlert
            }
            
            // Insert alerts chronically to Room so list displays it offline
            repository.triggerMockServerAlert(mockAlert)
        }
    }

    // Simulates live biometric streaming from Wear OS via continuous flow updates (WebSocket-like)
    private fun startWebSocketWearSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(12000) // update streaming biometrics every 12 seconds
                val activeDbElders = elders.value
                val activeDetail = _selectedElderBiometrics.value
                
                if (activeDetail != null) {
                    // Update only if selected details are being viewed
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val newPointLabel = timeFormat.format(Date())
                    
                    val fluctuatingHr = when (activeDetail.elderId) {
                        "elder_1" -> (80..135).random() // highly dynamic
                        else -> (65..85).random()
                    }
                    val fluctuatingSpO = when (activeDetail.elderId) {
                        "elder_2" -> (86..95).random()
                        else -> (96..99).random()
                    }

                    _selectedElderBiometrics.update { current ->
                        current?.let {
                            val newHrList = (it.heartRateSeries.takeLast(9) + HeartRatePoint(newPointLabel, fluctuatingHr))
                            val newSpo2List = (it.spO2Series.takeLast(9) + SpO2Point(newPointLabel, fluctuatingSpO))
                            it.copy(
                                heartRateSeries = newHrList,
                                spO2Series = newSpo2List,
                                stepCount = it.stepCount + (0..15).random(),
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
        }
    }
}

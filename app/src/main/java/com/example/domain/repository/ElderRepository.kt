package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ElderRepository {
    fun getElders(): Flow<List<Elder>>
    fun getElderById(id: String): Flow<Elder?>
    fun getAlerts(): Flow<List<Alert>>
    fun getVisitRecords(): Flow<List<VisitRecord>>
    fun getThresholds(): Flow<AlertThresholds>
    
    suspend fun markAlertAsRead(alertId: String)
    suspend fun insertVisitRecord(record: VisitRecord): Boolean
    suspend fun updateThresholds(thresholds: AlertThresholds): Boolean
    suspend fun addElder(elder: Elder)
    suspend fun removeElder(id: String)
    suspend fun triggerMockServerAlert(alert: Alert)
    fun getChecklistItems(): Flow<List<String>>
    suspend fun syncPendingVisits(): Int
}

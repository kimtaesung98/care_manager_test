package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ElderDao {

    @Query("SELECT * FROM elders ORDER BY name ASC")
    fun getAllElders(): Flow<List<ElderEntity>>

    @Query("SELECT * FROM elders WHERE id = :id LIMIT 1")
    fun getElderById(id: String): Flow<ElderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElder(elder: ElderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElders(elders: List<ElderEntity>)

    @Query("DELETE FROM elders WHERE id = :id")
    suspend fun deleteElderById(id: String)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: String)

    @Query("SELECT * FROM visits ORDER BY id DESC")
    fun getAllVisits(): Flow<List<VisitRecordEntity>>

    @Query("SELECT * FROM visits WHERE isSynced = 0")
    suspend fun getUnsyncedVisits(): List<VisitRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitRecordEntity)

    @Query("UPDATE visits SET isSynced = 1 WHERE id = :visitId")
    suspend fun markVisitSynced(visitId: String)

    @Query("SELECT * FROM checklist_items")
    fun getAllChecklistItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Query("SELECT * FROM thresholds WHERE id = 0")
    fun getThresholds(): Flow<ThresholdsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThresholds(thresholds: ThresholdsEntity)
}

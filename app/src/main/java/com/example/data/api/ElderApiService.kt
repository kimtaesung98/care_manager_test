package com.example.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit API service representing Go backend endpoints
interface ElderApiService {

    @GET("api/workers/elders")
    suspend fun getAssignedElders(
        @Query("worker_id") workerId: String
    ): List<ElderResponse>

    @GET("api/elders/{id}/biometrics")
    suspend fun getBiometricSummary(
        @Path("id") elderId: String,
        @Query("duration_hours") duration: Int = 24
    ): BiometricsResponse

    @POST("api/visits/records")
    suspend fun submitVisitRecord(
        @Body record: VisitRecordRequest
    ): VisitSubmitResponse

    @POST("api/elders/{id}/thresholds")
    suspend fun updateElderThresholds(
        @Path("id") elderId: String,
        @Body thresholds: ThresholdsRequest
    ): StatusResponse
}

// REST Network DTO models
data class ElderResponse(
    val id: String,
    val name: String,
    val age: Int,
    val lastSyncTime: Long,
    val riskLevel: String,
    val batteryPercent: Int,
    val phone: String,
    val guardianName: String,
    val guardianPhone: String,
    val imageIndex: Int
)

data class BiometricsResponse(
    val elderId: String,
    val heartRates: List<HeartRatePointDto>,
    val spO2Points: List<SpO2PointDto>,
    val stepCount: Int,
    val sleepHours: Double,
    val sleepQuality: String,
    val lastBattery: Int,
    val lastSync: Long
)

data class HeartRatePointDto(
    val timeLabel: String,
    val bpm: Int
)

data class SpO2PointDto(
    val timeLabel: String,
    val percentage: Int
)

data class VisitRecordRequest(
    val recordId: String,
    val elderId: String,
    val date: String,
    val tasksFinished: List<String>,
    val workerNotes: String,
    val workerId: String
)

data class VisitSubmitResponse(
    val success: Boolean,
    val serverMessage: String,
    val syncedId: String
)

data class ThresholdsRequest(
    val hrMin: Int,
    val hrMax: Int,
    val spo2Min: Int,
    val fallDetect: Boolean
)

data class StatusResponse(
    val code: Int,
    val message: String
)

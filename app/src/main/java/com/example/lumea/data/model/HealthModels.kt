package com.example.lumea.data.model

import com.google.gson.annotations.SerializedName

data class HealthCheckInput(
    @SerializedName("bpm")
    val heartRate: Int,
    
    @SerializedName("spo2")
    val bloodOxygen: Float,
    
    @SerializedName("resp_rate")
    val respiratoryRate: Float,
    
    @SerializedName("status")
    val status: String
)

data class HealthResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: HealthData? = null
)

data class HealthData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("bpm")
    val heartRate: Int,
    
    @SerializedName("spo2")
    val bloodOxygen: Float,
    
    @SerializedName("resp_rate")
    val respiratoryRate: Float,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("date")
    val date: String
)
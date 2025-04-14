package com.example.lumea.data.model

import com.google.gson.annotations.SerializedName

data class UserDataComplete(
    val profile: UserProfile?,
    val health: UserHealth?,
    val location: UserLocation?
)


data class UserLocation(
    val latitude: Float,
    val longitude: Float
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int
)

data class UserHealth(
    @SerializedName("bpm") val heartRate: Int?,
    @SerializedName("spo2") val bloodOxygen: Float?,
    @SerializedName("resp_rate") val respiratoryRate: Float?,
    val status: String?
)

data class UserResponse(
    val data: UserDataComplete,
    val success: Boolean,
    val message: String
)

package com.example.lumea.data.model

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("lastCheckedDate")
    val lastCheckedDate: String? = null
)
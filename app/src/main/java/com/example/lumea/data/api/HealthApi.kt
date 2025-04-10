package com.example.lumea.data.api

import com.example.lumea.data.model.HealthCheckInput
import com.example.lumea.data.model.HealthResponse
import retrofit2.Response
import retrofit2.http.*

interface HealthApi {
    @GET("api/health")
    suspend fun getHealthData(
        @Header("Authorization") authHeader: String,
        @Query("userId") userId: Int
    ): Response<HealthResponse>
    
    @POST("api/health")
    suspend fun createHealthData(
        @Header("Authorization") authHeader: String,
        @Body healthData: HealthCheckInput
    ): Response<HealthResponse>
    
    @PUT("api/health")
    suspend fun updateHealthData(
        @Header("Authorization") authHeader: String,
        @Body healthData: HealthCheckInput
    ): Response<HealthResponse>
    
    @DELETE("api/health")
    suspend fun deleteHealthData(
        @Header("Authorization") authHeader: String
    ): Response<HealthResponse>
}
package com.example.lumea.data.api

import com.example.lumea.data.model.LoginRequest
import com.example.lumea.data.model.LoginResponse
import com.example.lumea.data.model.RefreshTokenRequest
import com.example.lumea.data.model.RefreshTokenResponse
import com.example.lumea.data.model.RegisterRequest
import com.example.lumea.data.model.RegisterResponse
import com.example.lumea.data.model.VerifyTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("api/auth/verify")
    suspend fun verifyToken(@Header("Authorization") authHeader: String): Response<VerifyTokenResponse>

    @POST("api/auth/register")
    suspend fun register(@Body RegisterRequest : RegisterRequest): Response<RegisterResponse>
}
package com.example.lumea.data.api
import com.example.lumea.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserApi {
    @GET("api/users/data/{id}")
    suspend fun getUserData(
        @Header("Authorization") authHeader: String,
        @Path("id") userId: String
    ): Response<UserResponse>
}
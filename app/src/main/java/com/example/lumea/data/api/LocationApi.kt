package com.example.lumea.data.api
import com.example.lumea.data.model.Location
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LocationApi {
    @POST("api/location")
    suspend fun sendLocation(@Header("Authorization") authHeader: String, @Body location: Location): Response<Void>

    @GET("api/location")
    suspend fun getLocations(@Header("Authorization") authHeader: String): Response<List<Location>>


}
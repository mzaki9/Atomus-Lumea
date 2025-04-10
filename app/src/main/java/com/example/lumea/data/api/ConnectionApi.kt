package com.example.lumea.data.api
import com.example.lumea.data.model.ApiResponse
import com.example.lumea.data.model.ConnectionRequest
import com.example.lumea.data.model.ConnectionResponse
import com.example.lumea.data.model.ConnectionsResponse
import com.example.lumea.data.model.SearchRequest
import com.example.lumea.data.model.SearchResponse
import com.example.lumea.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ConnectionApi {
    @POST("api/connections/request")
    suspend fun sendConnectionRequest(
        @Header("Authorization") authHeader: String, 
        @Body request: ConnectionRequest
    ): Response<ApiResponse>

    @POST("api/connections/search")
    suspend fun searchUsers(
        @Header("Authorization") authHeader: String,
        @Body request: SearchRequest
    ): SearchResponse

    @POST("api/connections/respond")
    suspend fun respondToConnectionRequest(
        @Header("Authorization") authHeader: String, 
        @Body response: ConnectionResponse
    ): Response<ApiResponse>

    @GET("api/connections")
    suspend fun getConnections(
        @Header("Authorization") authHeader: String,
        @Query("userId") userId: Int
    ): Response<ConnectionsResponse>
    
    @GET("api/connections/requests")
    suspend fun getConnectionRequests(
        @Header("Authorization") authHeader: String
    ): Response<List<ConnectionRequest>>
}
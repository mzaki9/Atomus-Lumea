package com.example.lumea.data.model

import com.google.gson.annotations.SerializedName

data class ConnectionRequest(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("senderId")
    val senderId: Int,
    
    @SerializedName("receiverId")
    val receiverId: Int,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class SearchRequest(val name: String)


data class ConnectionResponse(
    @SerializedName("requestId")
    val requestId: Int,
    
    @SerializedName("accept")
    val accept: Boolean
)

data class Connection(
    @SerializedName("connectionId")
    val connectionId: Int,
    
    @SerializedName("friendId")
    val friendId: Int,
    
    @SerializedName("connectedAt")
    val connectedAt: String
)

data class ConnectionsResponse(
    @SerializedName("data")
    val connections: List<Connection>
)

data class ApiResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: String? = null
)

data class User(
    val id: String,
    val name: String
)

data class SearchResponse(
    @SerializedName("data")
    val data: List<User>
)
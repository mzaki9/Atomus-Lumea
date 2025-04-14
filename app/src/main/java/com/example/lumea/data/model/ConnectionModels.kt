package com.example.lumea.data.model

import com.google.gson.annotations.SerializedName

// Request to send a connection request
data class ConnectionRequest(
    @SerializedName("senderId")
    val senderId: Int? = null,

    @SerializedName("receiverId")
    val receiverId: Int,

    @SerializedName("userId")
    val userId: String? = null // For backward compatibility with old code
)

// Request for searching users
data class SearchRequest(
    @SerializedName("name")
    val name: String
)

// Request for responding to a connection request
data class RespondRequestBody(
    @SerializedName("requestId")
    val requestId: Int,

    @SerializedName("accept")
    val accept: Boolean
)

// Response for connections
data class ConnectionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Connection>? = null,
    @SerializedName("message") val message: String? = null
)

// Connection model
data class Connection(
    @SerializedName("connectionId") val connectionId: Int,
    @SerializedName("friendId") val friendId: Int,
    @SerializedName("friendName") val friendName: String?,
    @SerializedName("connectedAt") val connectedAt: String
)

// Generic API response
data class ApiResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("success") val success: Boolean = false
)

// User model
data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

// User with connection request information model
data class UserConnectivity(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("isRequestSent") val isRequestSent: Boolean
)

// Search response
data class SearchResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<UserConnectivity> = emptyList(),
    @SerializedName("message") val message: String? = null
)

// Friend request model that matches backend format
data class FriendRequest(
    @SerializedName("requestId") val requestId: Int,
    @SerializedName("sender") val sender: User,
    @SerializedName("createdAt") val createdAt: String
)

// Friend requests response
data class FriendRequestsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<FriendRequest> = emptyList(),
    @SerializedName("message") val message: String? = null
)
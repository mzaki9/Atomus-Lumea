package com.example.lumea.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.lumea.data.model.User

class FriendViewModel : ViewModel() {
    var searchQuery by mutableStateOf("")
    var searchResults by mutableStateOf(listOf<User>())
        private set

    var friendRequests by mutableStateOf(
        listOf(
            User("1", "HUGO SABAM AUGUSTO"),
            User("2", "HUGO SABAM AUGUSTO"),
            User("3", "HUGO SABAM AUGUSTO")
        )
    )
        private set

    fun searchUsers() {
        // Simulasi search, ganti nanti dengan fetch dari API
        searchResults = List(3) { User(it.toString(), "HUGO SABAM AUGUSTO") }
    }

    fun sendFriendRequest(userId: String) {
        // Simulasi kirim permintaan, nanti bisa kirim ke backend
        searchResults = searchResults.filterNot { it.id == userId }
    }

    fun acceptRequest(userId: String) {
        friendRequests = friendRequests.filterNot { it.id == userId }
    }

    fun rejectRequest(userId: String) {
        friendRequests = friendRequests.filterNot { it.id == userId }
    }
}

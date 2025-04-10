package com.example.lumea.ui.screens.addfriends


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumea.ui.components.FriendTabSelector
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.example.lumea.data.auth.TokenManager
import com.example.lumea.ui.components.SearchSection
import com.example.lumea.ui.components.RequestSection
import com.example.lumea.ui.viewmodel.FriendViewModel

@Composable
fun AddFriendScreen(
    viewModel: FriendViewModel = viewModel(
        factory = FriendViewModel.Factory(LocalContext.current)
    )
) {

    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        FriendTabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            val query by viewModel.searchQuery.observeAsState("")
            val searchResults by viewModel.searchResults.observeAsState(emptyList())

            SearchSection(
                searchQuery = query,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSearch = {
                    viewModel.searchUsers(
                        name= query
                    )
                },
                users = searchResults,
                onAddFriend = { userId -> viewModel.sendFriendRequest(userId) }
            )
        } else {
            RequestSection(
                requests = viewModel.friendRequests,
                onAccept = { userId -> viewModel.acceptRequest(userId) },
                onReject = { userId -> viewModel.rejectRequest(userId) }
            )
        }
    }
}

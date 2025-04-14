package com.example.lumea.ui.screens.addfriends

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumea.ui.components.FriendTabSelector
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.example.lumea.ui.components.SearchSection
import com.example.lumea.ui.components.RequestSection
import com.example.lumea.ui.viewmodel.FriendViewModel
import kotlinx.coroutines.launch

@Composable
fun AddFriendScreen(
    viewModel: FriendViewModel = viewModel(
        factory = FriendViewModel.Factory(LocalContext.current)
    )
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val error by viewModel.error.observeAsState()
    val successMessage by viewModel.requestSuccess.observeAsState()

    // Show snackbar for success messages and errors
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle success message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            // Clear success message after showing snackbar
            viewModel.resetSuccessMessage()
        }
    }

    // Handle error message
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            // Clear error after showing snackbar
            viewModel.resetError()
        }
    }

    // Refresh friend requests when switching to requests tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            viewModel.fetchFriendRequests()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    onSearch = { viewModel.searchUsers(name = query) },
                    users = searchResults,
                    onAddFriend = { userId -> viewModel.sendFriendRequest(userId) },
                    isLoading = isLoading,
                    error = error
                )
            } else {
                val requests by viewModel.friendRequests.observeAsState(emptyList())

                RequestSection(
                    requests = requests,
                    onAccept = { requestId -> viewModel.acceptRequest(requestId) },
                    onReject = { requestId -> viewModel.rejectRequest(requestId) },
                    isLoading = isLoading,
                    error = error
                )
            }
        }
    }
}
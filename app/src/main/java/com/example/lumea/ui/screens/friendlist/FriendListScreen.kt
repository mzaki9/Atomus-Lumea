package com.example.lumea.ui.screens.friendlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumea.R
import com.example.lumea.ui.components.FriendCard
import com.example.lumea.ui.navigation.Screen
import com.example.lumea.ui.viewmodel.FriendListViewModel

@Composable
fun FriendListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FriendListViewModel = viewModel(
        factory = FriendListViewModel.Factory(context)
    )
    
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Fetch connections when the screen is first displayed
    LaunchedEffect(key1 = true) {
        viewModel.fetchFriendList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Daftar Teman",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            // Tombol tambah teman, navigasi ke AddFriendScreen
            IconButton(onClick = {
                navController.navigate(Screen.AddFriend.route)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.tambah_teman),
                    contentDescription = "Tambah Teman",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Content based on state
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                friends.isEmpty() -> {
                    Text(
                        text = "Belum ada teman. Tambahkan teman baru!",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(friends) { friend ->
                            FriendCard(
                                id = friend.id,
                                name = friend.name,
                                onClick = { friendId ->
                                    navController.navigate("detail_teman/$friendId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
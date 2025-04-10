package com.example.lumea.ui.screens.friendlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lumea.R
import com.example.lumea.ui.components.FriendCard
import com.example.lumea.ui.navigation.Screen
import androidx.compose.foundation.clickable


@Composable
fun FriendListScreen(navController: NavController) {
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

            // ✅ Tombol tambah teman, navigasi ke AddFriendScreen
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

        // Dummy list teman
        data class Friend(val id: String, val name: String)
        val friends = listOf(
            Friend(id = "1", name = "HUGO SABAM AUGUSTO"),
            Friend(id = "2", name = "ALBERT GHAZALY"),
            Friend(id = "3", name = "MUHAMMAD ZAKI"),
            Friend(id = "4", name = "VLADIMIR PUTIN")
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendCard(
                    id = friend.id,
                    name = friend.name,
                    onClick = { friendId ->
                        println("Navigating to friend detail: $friendId")
                        navController.navigate("detail_teman/$friendId")
                    }
                )
            }
        }
    }
}

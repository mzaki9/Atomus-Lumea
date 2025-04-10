package com.example.lumea.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lumea.data.model.User

@Composable
fun RequestSection(
    requests: List<User>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(requests) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(user.name, style = MaterialTheme.typography.bodyLarge)
                Row {
                    TextButton(onClick = { onAccept(user.id) }) {
                        Text("Accept")
                    }
                    TextButton(onClick = { onReject(user.id) }) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

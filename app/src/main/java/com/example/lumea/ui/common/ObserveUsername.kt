package com.example.lumea.ui.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.lumea.data.auth.UsernameManager

@Composable
fun ObserveUsername(context: Context = LocalContext.current): String {
    val usernameManager = UsernameManager.getInstance(context)
    val username by usernameManager.username.collectAsState()
    
    // Fetch username whenever this composable is used
    LaunchedEffect(key1 = true) {
        usernameManager.fetchUsername()
    }
    
    return username
}
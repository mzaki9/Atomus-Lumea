package com.example.lumea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.lumea.data.auth.UsernameManager
import com.example.lumea.services.TokenServiceManager
import com.example.lumea.ui.navigation.AppNavigation
import com.example.lumea.ui.theme.LumeaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

         // Initialize username manager
        val usernameManager = UsernameManager.getInstance(applicationContext)
        
        // Fetch username at app start
        lifecycleScope.launch {
            usernameManager.fetchUsername()
        }
        
        enableEdgeToEdge()

        // Start the token validation service
        TokenServiceManager.startTokenValidationService(this)

        setContent {
            LumeaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optionally stop token validation when app is destroyed
        // TokenServiceManager.stopTokenValidationService(this)
    }
}
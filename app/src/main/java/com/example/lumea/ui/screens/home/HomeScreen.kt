package com.example.lumea.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lumea.ui.components.GradientCardBackground
import com.example.lumea.ui.theme.AppTypography

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientCardBackground(
            modifier = Modifier.fillMaxSize(),
            height = 400.dp
        )
        
        // Content on top of gradient card
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "Sendi sehat",
                style =AppTypography.headlineSmall,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "semangat gowes ya",
                style = AppTypography.headlineSmall,
                color = Color.White
            )
        }
        
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 300.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            // Content inside white container goes here
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Add your content here
            }
        }

    }
}

@Composable @Preview
fun HomeScreenPreview() {
    HomeScreen()
}
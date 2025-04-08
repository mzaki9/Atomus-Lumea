package com.example.lumea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lumea.ui.theme.BluePrimary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GradientCardBackground(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    colors: List<Color> = listOf(
        Color(0xFF50ABFE), 
        Color(0xFF389AFF)
    ),
    bottomColor: Color = BluePrimary
) {
    // Create a custom wave shape that's inverted (waves on top)
    val waveShape = GenericShape { size, _ ->
        moveTo(0f, size.height)
        
        lineTo(size.width, size.height)
        lineTo(size.width, size.height * 0.3f)
        
        val waveWidth = size.width
        val waveAmplitude = size.height * 0.15f
        
        for (x in 0..size.width.toInt() step 5) {
            val waveY = sin(x * (2f * PI / waveWidth)) * waveAmplitude + size.height * 0.2f
            lineTo(size.width - x.toFloat(), waveY.toFloat())
        }
        
        lineTo(0f, size.height * 0.3f)
        close()
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(bottomColor)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(waveShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = colors
                    )
                )
        )
    }
}

@Composable @Preview
fun GradientCardPreview() {
    GradientCardBackground(
        height = 250.dp
    )
}

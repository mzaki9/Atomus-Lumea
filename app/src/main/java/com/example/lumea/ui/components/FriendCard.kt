package com.example.lumea.ui.components
import com.example.lumea.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun FriendCard(
    id: String,
    name: String,
    onClick: (String) -> Unit = {} // Lempar ID saat diklik
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Transparent)
            .clickable { onClick(id) } // kirim ID saat diklik
    ) {
        // Wave background
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF42A5F5)),
                ),
                size = size
            )

            // Wave shape
            val wavePath = Path().apply {
                moveTo(0f, height * 0.3f)
                quadraticBezierTo(
                    width * 0.5f, height * 0.2f,
                    width, height * 0.4f
                )
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }

            drawPath(
                path = wavePath,
                color = Color(0xFF1E88E5)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Lihat Detail",
                tint = Color.White
            )
        }

        Image(
            painter = painterResource(id = R.drawable.lumea_logo_small),
            contentDescription = "Connect Icon",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(24.dp)
        )
    }
}

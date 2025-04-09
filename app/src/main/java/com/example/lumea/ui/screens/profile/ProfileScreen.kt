package com.example.lumea.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lumea.R
import com.example.lumea.ui.components.GradientCardBackground

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Gradient
        GradientCardBackground(
            modifier = Modifier.fillMaxSize(),
            height = 400.dp
        )

        // Content Column (di atas background)
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Information (layer atas dengan background transparan)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "BOWO ALPENLIEBE",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Tambahkan jarak 8 dp di bawah nama
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Phone,
                                    contentDescription = "Phone",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "08XXXXXXXX",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Email,
                                    contentDescription = "Email",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "em*****@gmail.com",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = "Latest Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Latest Location",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your actual icon
                            contentDescription = "Profile Icon",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Graphical History Section (layer atas dengan background putih)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .fillMaxHeight(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column( // Ganti LazyColumn menjadi Column
                    modifier = Modifier.padding(top= 16.dp,start= 16.dp,end= 16.dp)
                ) {
                    Text(
                        text = "Riwayat Grafikal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn { // Pindahkan LazyColumn ke dalam Column
                        // Tambahkan item-item riwayat grafikal di sini
                        items(count = 10) { index -> // Contoh menambahkan 10 item
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(225.dp)
                                    .padding(bottom = 8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "Riwayat Item ${index + 1} (Coming Soon)", color = Color.Black)
                                }
                            }
                        }
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(bottom = 8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "Riwayat Tambahan 1 (Coming Soon)", color = Color.Black)
                                    // Tambahkan grafik lain di sini
                                }
                            }
                        }
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(bottom = 8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "Riwayat Tambahan 2 (Coming Soon)", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
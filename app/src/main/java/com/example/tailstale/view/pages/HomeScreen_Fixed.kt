package com.example.tailstale.view.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.tailstale.R
import kotlin.math.max
import kotlin.math.min

// Helper components for HomeScreen
@Composable
private fun PetOverlayIcon(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f),
        modifier = modifier
            .size(48.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PetVideoPlayer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Color(0xFFFFE0B2),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "🐕\nVideo Player\n(Replace with actual video file)",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
private fun PetStatusBar(label: String, value: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${value}%",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun HomeScreen() {
    var health by remember { mutableStateOf(85) }
    var hunger by remember { mutableStateOf(40) }
    var happiness by remember { mutableStateOf(70) }

    val cooldownMillis = 90_000L
    var lastClickTime by remember { mutableStateOf(0L) }
    val currentTime = System.currentTimeMillis()
    val isClickable = currentTime - lastClickTime > cooldownMillis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Pet info header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Buddy",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF9500)
                ) {
                    Text(
                        "Puppy",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                "3 months old",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status bars
        PetStatusBar("Health", health, Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(8.dp))
        PetStatusBar("Hunger", hunger, Color(0xFFFF5722))
        Spacer(modifier = Modifier.height(8.dp))
        PetStatusBar("Happiness", happiness, Color(0xFF2196F3))

        Spacer(modifier = Modifier.height(24.dp))

        // Video with overlay icons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            PetVideoPlayer(
                modifier = Modifier.fillMaxSize()
            )

            // Overlay icons on the right side
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Walking icon
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_directions_walk_24),
                    onClick = {
                        happiness = min(100, happiness + 10)
                        hunger = max(0, hunger - 5)
                    }
                )

                // Feeding icon
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_restaurant_24),
                    onClick = {
                        if (isClickable) {
                            hunger = min(100, hunger + 20)
                            health = min(100, health + 5)
                            lastClickTime = System.currentTimeMillis()
                        }
                    },
                    modifier = Modifier.alpha(if (isClickable) 1f else 0.5f)
                )

                // Play icon
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_sports_basketball_24),
                    onClick = {
                        happiness = min(100, happiness + 5)
                        hunger = max(0, hunger - 3)
                    }
                )

                // Medical icon
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.outline_local_hospital_24),
                    onClick = {
                        health = min(100, health + 20)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pet care tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Pet Care Tips",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Feed your pet regularly to maintain hunger levels\n" +
                            "• Play with your pet to increase happiness\n" +
                            "• Regular exercise keeps your pet healthy",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

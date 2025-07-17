package com.example.tailstale.view.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // App logo placeholder
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFFF9500), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🐾", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("PAWS TALK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Row {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
fun PetInfoSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good morning!",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "Buddy",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Golden Retriever • 2 years old",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Profile image placeholder
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFE3F2FD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🐕", fontSize = 30.sp)
        }
    }
}

@Composable
fun HealthStatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today's Health",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HealthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "Happiness",
                    value = 85,
                    color = Color(0xFF4CAF50)
                )
                HealthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "Energy",
                    value = 70,
                    color = Color(0xFF2196F3)
                )
                HealthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "Health",
                    value = 90,
                    color = Color(0xFFFF9500)
                )
            }
        }
    }
}

@Composable
fun HealthStatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value%",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PetImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Color(0xFFFFE0B2),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🐕",
                fontSize = 64.sp
            )
            Text(
                text = "Buddy is happy!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF795548)
            )
        }

        // Overlay buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.PlayArrow,
                onClick = { /* Play */ }
            )
            QuickActionButton(
                icon = Icons.Default.Favorite,
                onClick = { /* Feed */ }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickActionsSection() {
    Column {
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Feed",
                icon = Icons.Default.Favorite,
                color = Color(0xFFE91E63),
                onClick = { /* Navigate to feed */ }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Walk",
                icon = Icons.Default.Place,
                color = Color(0xFF4CAF50),
                onClick = { /* Navigate to walk */ }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Play",
                icon = Icons.Default.Star,
                color = Color(0xFF2196F3),
                onClick = { /* Navigate to play */ }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun Home() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        AppHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // Pet Info
        PetInfoSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Health Stats
        HealthStatsSection()

        Spacer(modifier = Modifier.height(32.dp))

        // Pet Image
        PetImageSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        QuickActionsSection()
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    Home()
}
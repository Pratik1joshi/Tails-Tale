package com.example.tailstale.view


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import android.widget.VideoView
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.example.tailstale.view.OnboardingStoryFlow
import com.example.tailstale.view.pages.ActivitiesScreen
import com.example.tailstale.view.pages.AddScreen
import com.example.tailstale.view.pages.HomeScreen
import com.example.tailstale.view.pages.ProfileScreen
import com.example.tailstale.view.pages.StatsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VirtualPetTheme {
                VirtualPetApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualPetApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Stats", "Add", "Activities", "Profile")
    val tabIcons = listOf(
        Icons.Default.Home,
        Icons.Default.Settings,//baseline_bar_chart_24
        Icons.Default.Add,
        Icons.Default.Star,//baseline_directions_run_24
        Icons.Default.Person
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF007AFF)
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                tabIcons[index],
                                contentDescription = title,
                                tint = if (selectedTab == index) Color(0xFF007AFF) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                title,
                                fontSize = 10.sp,
                                color = if (selectedTab == index) Color(0xFF007AFF) else Color.Gray
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen() // Default page
                1 -> StatsScreen()
                2 -> AddScreen()
                3 -> ActivitiesScreen()
                4 -> ProfileScreen()
            }
        }
    }
}


// this is the overlay icon with a painter resource
@Composable
fun OverlayIconPainter(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier
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
fun VideoPlayerView(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Placeholder for video - replace with actual video file
    Box(
        modifier = modifier
            .background(
                Color(0xFFFFE0B2), // Light orange background as placeholder
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

    // Uncomment and modify this when you have a video file
    /*
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.your_video_file}"))
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    start()
                }
            }
        },
        modifier = modifier
    )
    */
}

@Composable
fun OverlayIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier
            .size(48.dp)
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
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatusBar(label: String, value: Int, color: Color) {
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

// Other screen composables



@Composable
fun VirtualPetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF007AFF),
            secondary = Color(0xFFFF9500),
            background = Color(0xFFF5F5F5),
            surface = Color.White
        ),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MainActivity()
}
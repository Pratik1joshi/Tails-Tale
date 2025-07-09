package com.example.tailstale


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
        StatusBar("Health", health, Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(8.dp))
        StatusBar("Hunger", hunger, Color(0xFFFF5722))
        Spacer(modifier = Modifier.height(8.dp))
        StatusBar("Happiness", happiness, Color(0xFF2196F3))

        Spacer(modifier = Modifier.height(24.dp))

        // Video with overlay icons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Video view (placeholder for now - you'll need to add actual video file)
            VideoPlayerView(
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
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_directions_walk_24),
                    onClick = {
                        // Handle walking action
                        happiness = minOf(100, happiness + 10)
                        hunger = maxOf(0, hunger - 5)
                    }
                )

                //Feeding icon
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_restaurant_24),
                    onClick = {
                        if (isClickable) {
                            hunger = minOf(100, hunger + 20)
                            health = minOf(100, health + 5)
                            lastClickTime = System.currentTimeMillis()
                        }
                    },
                    modifier = Modifier.alpha(if (isClickable) 1f else 0.5f)
                // Dim the icon during cooldown
                )


                // Play icon
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_sports_basketball_24),
                    onClick = {
                        // Handle play action
                        happiness = minOf(100, happiness + 5)
                        hunger = maxOf(0, hunger - 3)
                    }
                )

                // Medical icon
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.outline_local_hospital_24),
                    onClick = {
                        // Handle medical action
                        health = minOf(100, health + 20)
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
fun StatsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,//baseline_bar_chart_24
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Stats Screen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                "Pet statistics and analytics will be shown here",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AddScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Add New Pet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                "Create and customize your new virtual pet",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivitiesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Build,//baseline_directions_run_24
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Activities",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                "Track your pet's daily activities and achievements",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                "Manage your account and app settings",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

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
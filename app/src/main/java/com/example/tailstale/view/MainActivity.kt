package com.example.tailstale

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.content.ContextCompat
import com.example.tailstale.view.LoginActivity
import com.example.tailstale.view.pages.ActivitiesScreen
import com.example.tailstale.view.pages.AddScreen
import com.example.tailstale.view.pages.HomeScreen
import com.example.tailstale.view.pages.ProfileScreen
import com.example.tailstale.view.pages.StatsScreen
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {

    // Permission launcher for notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, notifications will work
            println("Notification permission granted")
        } else {
            // Permission denied, handle accordingly
            println("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            VirtualPetTheme(darkTheme = isDarkTheme) {
                VirtualPetApp(onToggleTheme = { isDarkTheme = !isDarkTheme })
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    println("Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale and request permission
                    showNotificationPermissionRationale()
                }
                else -> {
                    // Request permission directly
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showNotificationPermissionRationale() {
        // You can show a dialog explaining why you need notification permission
        // For now, just request the permission
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualPetApp(onToggleTheme: () -> Unit) {

    var selectedVideoRes by remember { mutableStateOf(R.raw.sitting) }
    var isLooping by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Stats", "Add", "Activities", "Profile")
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isDarkTheme by remember { mutableStateOf(false) }

    val tabIcons = listOf(
        Icons.Default.Home,
        painterResource(id = R.drawable.baseline_bar_chart_24),
        Icons.Default.Add,
        painterResource(id = R.drawable.baseline_directions_run_24),
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
                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                                DropdownMenu(
                                    expanded = showSettingsMenu,
                                    onDismissRequest = { showSettingsMenu = false}
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Dark Mode") },
                                        onClick = {
                                            // Handle dark mode toggle
                                            onToggleTheme()
                                            showSettingsMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Logout") },
                                        onClick = {
                                            // Handle logout logic
                                            showSettingsMenu = false
                                            showLogoutDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color(0xFF007AFF)
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            when (val icon = tabIcons[index]) {
                                is ImageVector -> Icon(
                                    icon,
                                    contentDescription = title,
                                    tint = if (selectedTab == index) Color(0xFF007AFF) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                is Painter -> Image(
                                    painter = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                        if (selectedTab == index) Color(0xFF007AFF) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        },
                        label = {
                            Text(
                                title,
                                fontSize = 10.sp,
                                color = if (selectedTab == index) Color(0xFF007AFF) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    )

    { innerPadding ->
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
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Rest of your composables remain the same...

@Composable
fun OverlayIconPainter(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
fun VideoPlayerView(
    modifier: Modifier = Modifier,
    videoRes: Int,
    isLooping: Boolean,
    onCompletion: () -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            android.widget.VideoView(ctx).apply {
                setVideoURI(android.net.Uri.parse("android.resource://${ctx.packageName}/$videoRes"))
                setOnPreparedListener { it.isLooping = isLooping; start() }
                setOnCompletionListener { onCompletion() }
            }
        },
        modifier = modifier,
        update = { view ->
            view.setVideoURI(android.net.Uri.parse("android.resource://${context.packageName}/$videoRes"))
            view.setOnPreparedListener { it.isLooping = isLooping; view.start() }
            view.setOnCompletionListener { onCompletion() }
        }
    )
}

@Composable
fun OverlayIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
                tint = MaterialTheme.colorScheme.background,
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
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${value}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
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
fun VirtualPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
package com.example.tailstale.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tailstale.repo.UserRepositoryImpl
import com.example.tailstale.view.LoginActivity
import com.example.tailstale.view.pages.ActivitiesScreen
import com.example.tailstale.view.pages.AddScreen
import com.example.tailstale.view.pages.HomeScreen
import com.example.tailstale.view.pages.ProfileScreen
import com.example.tailstale.view.pages.StatsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.repo.PetRepositoryImpl
import com.example.tailstale.viewmodel.PetViewModel
import com.example.tailstale.viewmodel.PetViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.example.tailstale.ui.theme.VirtualPetTheme
import com.example.tailstale.ui.theme.ThemeState
import com.example.tailstale.service.RealTimeNotificationMonitor
import com.example.tailstale.R

class MainActivity : ComponentActivity() {

    // Register for notification permission result
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, initialize notifications
            initializeNotifications()
        } else {
            // Permission denied - handle gracefully
            android.util.Log.w("MainActivity", "Notification permission denied by user")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channels immediately
        RealTimeNotificationMonitor.getInstance().createNotificationChannels(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        setContent {
            VirtualPetTheme {
                VirtualPetApp()
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
                    initializeNotifications()
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, no runtime permission needed
            initializeNotifications()
        }
    }

    private fun initializeNotifications() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            RealTimeNotificationMonitor.getInstance().startRealTimeMonitoring(this, userId)
            android.util.Log.d("MainActivity", "Notification monitoring started for user: $userId")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualPetApp() {
    // Create PetViewModel with dependency injection
    val currentUser = FirebaseAuth.getInstance().currentUser
    val petViewModel: PetViewModel = viewModel(
        factory = PetViewModelFactory(
            PetRepositoryImpl(),
            UserRepositoryImpl()
        )
    )

    // Get context here so it's available in LaunchedEffect
    val context = LocalContext.current

    // Load user's pets when the app starts
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            petViewModel.loadUserPets(userId)
            petViewModel.startRealTimeAging(userId)
            petViewModel.loadUserAchievements(userId)

            // NEW: Start real-time notification monitoring
            petViewModel.startRealTimeNotificationMonitoring(context, userId)
        }
    }

    // NEW: Handle app lifecycle for notifications
    DisposableEffect(currentUser?.uid) {
        onDispose {
            currentUser?.uid?.let { userId ->
                petViewModel.stopRealTimeNotificationMonitoring(context, userId)
            }
        }
    }

    // Notification states - NOW WITH REAL-TIME INTEGRATION
    val notifications by petViewModel.notifications.collectAsState()
    val unreadNotificationCount by petViewModel.unreadNotificationCount.collectAsState()
    val criticalAlertsCount by petViewModel.criticalAlertsCount.collectAsState()
    val realTimeNotifications by petViewModel.realTimeNotifications.collectAsState()

    var selectedVideoRes by remember { mutableStateOf(R.raw.sitting) }
    var isLooping by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Stats", "Add", "Activities", "Profile")
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNotificationPanel by remember { mutableStateOf(false) }

    val tabIcons = listOf(
        Icons.Default.Home,
        painterResource(id = R.drawable.baseline_bar_chart_24),
        Icons.Default.Add,
        painterResource(id = R.drawable.baseline_directions_run_24),
        Icons.Default.Person
    )

    // Theme state
    val isDarkMode by ThemeState.isDarkMode

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
                            // App logo - made bigger as requested
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "TailsTale Logo",
                                modifier = Modifier
                                    .size(40.dp) // Increased from 32dp to 40dp
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tail'sTale",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            // Notification icon with badge
                            Box {
                                IconButton(onClick = { showNotificationPanel = true }) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = if (unreadNotificationCount > 0) Color(0xFFFF9500) else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Notification badge
                                if (unreadNotificationCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF44336),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                DropdownMenu(
                                    expanded = showSettingsMenu,
                                    onDismissRequest = { showSettingsMenu = false}
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (isDarkMode) "Light Mode" else "Dark Mode")
                                            }
                                        },
                                        onClick = {
                                            ThemeState.toggleTheme()
                                            showSettingsMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Logout,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Logout")
                                            }
                                        },
                                        onClick = {
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.height(56.dp) // Reduced from 70dp to 56dp for smaller navbar
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            when (val icon = tabIcons[index]) {
                                is ImageVector -> Icon(
                                    icon,
                                    contentDescription = title,
                                    tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                is Painter -> Image(
                                    painter = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                        if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        },
                        label = {
                            Text(
                                title,
                                fontSize = 10.sp,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> HomeScreen() // Default page
                1 -> StatsScreen(petViewModel = petViewModel)
                2 -> AddScreen()
                3 -> ActivitiesScreen()
                4 -> ProfileScreen(
                    userRepository = UserRepositoryImpl() // Your existing implementation
                )
            }
        }
    }

    // Notification Panel
    if (showNotificationPanel) {
        com.example.tailstale.view.components.NotificationPanel(
            notifications = notifications,
            onNotificationClick = { notification ->
                petViewModel.markNotificationAsRead(context, notification.id)
                // Handle navigation based on notification type
                when (notification.actionType) {
                    "vaccinate", "treat_disease", "health_check" -> {
                        selectedTab = 1 // Navigate to Stats screen
                        showNotificationPanel = false
                    }
                    else -> {
                        // Handle other actions
                        petViewModel.handleNotificationAction(notification.actionType, notification.petId)
                    }
                }
            },
            onMarkAsRead = { notificationId ->
                petViewModel.markNotificationAsRead(context, notificationId)
            },
            onClearNotification = { notificationId ->
                petViewModel.clearNotification(notificationId)
            },
            onMarkAllAsRead = {
                petViewModel.markAllNotificationsAsRead(context)
            },
            onClearAll = {
                petViewModel.clearAllNotifications()
            },
            onDismiss = {
                showNotificationPanel = false
            },
            petViewModel = petViewModel
        )
    }

    // Logout Dialog - COMPLETING THE MISSING IMPLEMENTATION
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Logout user
                        FirebaseAuth.getInstance().signOut()

                        // Navigate to login screen
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)

                        showLogoutDialog = false
                    }
                ) {
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

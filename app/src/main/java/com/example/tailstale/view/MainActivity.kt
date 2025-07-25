package com.example.tailstale.view


import android.content.Intent
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
import com.example.tailstale.R
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
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
    // Create PetViewModel with dependency injection
    val currentUser = FirebaseAuth.getInstance().currentUser
    val petViewModel: PetViewModel = viewModel(
        factory = PetViewModelFactory(
            PetRepositoryImpl(),
            UserRepositoryImpl()
        )
    )

    // Load user's pets when the app starts
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            petViewModel.loadUserPets(userId)
            petViewModel.startRealTimeAging(userId)
            petViewModel.loadUserAchievements(userId) // Load achievement data
        }
    }

    var selectedVideoRes by remember { mutableStateOf(R.raw.sitting) }
    var isLooping by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Stats", "Add", "Activities", "Profile")
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                            Text(
                                "PAWS TALK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            IconButton(onClick = { /* Notifications */ }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
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
                )
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Confirm Logout",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface
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

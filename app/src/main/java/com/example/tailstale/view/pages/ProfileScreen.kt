package com.example.tailstale.view.pages

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tailstale.model.UserModel
import com.example.tailstale.model.Achievement
import com.example.tailstale.repo.UserRepository
import com.example.tailstale.viewmodel.ProfileViewModel
import com.example.tailstale.viewmodel.ProfileViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    userRepository: UserRepository
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showAchievementDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    // Create ProfileViewModel with dependency injection
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository)
    )

    val userProfile = profileViewModel.userProfile
    val isLoading = profileViewModel.isLoading
    val isUploading = profileViewModel.isUploading
    val errorMessage = profileViewModel.errorMessage
    val isInitialized = profileViewModel.isInitialized

    // Permission handling for camera and storage
    val permissions = mutableListOf<String>().apply {
        add(android.Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        try {
            uri?.let {
                profileViewModel.uploadProfileImage(context, it)
            }
        } catch (e: Exception) {
            profileViewModel.setError("Failed to select image: ${e.message}")
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        try {
            bitmap?.let {
                profileViewModel.setError("Camera functionality will be implemented soon")
            }
        } catch (e: Exception) {
            profileViewModel.setError("Failed to capture image: ${e.message}")
        }
    }

    // Handle permission results
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && showImagePicker) {
            // Permissions granted, but dialog should handle the picker launch
        }
    }

    // Show loading screen while profile is being loaded
    if (!isInitialized && isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    "Loading your profile...",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }

    // Show error screen if profile failed to load
    if (!isInitialized && userProfile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red
                )
                Text(
                    "Failed to load profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    errorMessage ?: "Unknown error occurred",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { profileViewModel.refreshProfile() }
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Main profile content - only show if profile is loaded
    userProfile?.let { profile ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Profile Header Card with user info
                UserProfileHeaderCard(
                    userModel = profile,
                    isUploading = isUploading,
                    onEditProfile = { showEditDialog = true },
                    onImageClick = {
                        // Always show the image picker dialog - let it handle permissions
                        showImagePicker = true
                    }
                )
            }

            item {
                // Achievements Card
                UserAchievementsCard(
                    achievements = profile.achievements,
                    onAddAchievement = { showAchievementDialog = true }
                )
            }

            item {
                // Pets Card - Updated to show actual pets instead of IDs
                PetsOverviewCard(
                    pets = profileViewModel.userPets, // Use actual pets instead of pet IDs
                    onManagePets = {
                        // Navigate to pets management screen
                    }
                )
            }

            item {
                // Account Actions Card
                AccountActionsCard(
                    onSignOut = { profileViewModel.signOut() },
                    onRefresh = { profileViewModel.refreshProfile() }
                )
            }

            item {
                // App Version
                Text(
                    "Version 1.0.0",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

        }

        // Loading Overlay for updates
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Updating profile...")
                    }
                }
            }
        }

        // Error Snackbar
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.contains("success", ignoreCase = true))
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFFF5722)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (message.contains("success", ignoreCase = true))
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            LaunchedEffect(message) {
                kotlinx.coroutines.delay(5000)
                profileViewModel.clearError()
            }
        }

        // Dialogs
        if (showEditDialog) {
            EditUserProfileDialog(
                userModel = profile,
                onDismiss = { showEditDialog = false },
                onSave = { displayName, email ->
                    profileViewModel.updateProfile(displayName, email)
                    showEditDialog = false
                }
            )
        }

        if (showImagePicker) {
            ImagePickerDialog(
                onDismiss = { showImagePicker = false },
                onGallery = {
                    imagePickerLauncher.launch("image/*")
                    showImagePicker = false
                },
                onCamera = {
                    cameraLauncher.launch(null)
                    showImagePicker = false
                }
            )
        }

        if (showAchievementDialog) {
            AddAchievementDialog(
                onDismiss = { showAchievementDialog = false },
                onAdd = { title, description ->
                    profileViewModel.addAchievement(title, description)
                    showAchievementDialog = false
                }
            )
        }
    }
}

@Composable
fun UserProfileHeaderCard(
    userModel: UserModel,
    isUploading: Boolean,
    onEditProfile: () -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dynamic Profile Picture with Cloudinary
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9500))
                    .border(3.dp, Color.White, CircleShape)
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (userModel.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = userModel.profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }

                // Upload indicator
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color.White
                        )
                    }
                }

                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .background(Color(0xFF007AFF), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = userModel.displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // User Email
            Text(
                text = userModel.email,
                fontSize = 16.sp,
                color = Color.Gray
            )

            // Bio
            if (userModel.bio.isNotEmpty()) {
                Text(
                    text = userModel.bio,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Location
            if (userModel.location.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userModel.location,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Button
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    onAdd: () -> Unit = {},
    color: Color = Color(0xFF007AFF)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        // Add button for testing - remove in production
        if (label == "Coins") {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAdd,
                modifier = Modifier.size(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Coins",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun UserAchievementsCard(
    achievements: List<Achievement>, // Fixed: Use Achievement from model instead of UserProfile.Achievement
    onAddAchievement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Achievement list - Fixed: Use Column instead of LazyColumn to prevent nested scrolling
            if (achievements.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show only first 3 achievements to prevent layout issues
                    achievements.take(3).forEach { achievement ->
                        AchievementItem(achievement = achievement)
                    }

                    // Show "View More" if there are more than 3 achievements
                    if (achievements.size > 3) {
                        Text(
                            text = "... and ${achievements.size - 3} more achievements",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Text(
                    "No achievements yet. Complete tasks to earn achievements.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Add Achievement Button
            OutlinedButton(
                onClick = onAddAchievement,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Achievement")
            }
        }
    }
}

@Composable
fun AchievementItem(
    achievement: Achievement // Fixed: Use Achievement from model instead of UserProfile.Achievement
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .background(Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Achievement details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = achievement.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Achievement icon
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF007AFF)
            )
        }
    }
}

@Composable
fun PetsOverviewCard(
    pets: List<com.example.tailstale.model.PetModel>, // Fixed: Use correct PetModel type
    onManagePets: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Your Pets",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Pet list or message
            if (pets.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pets) { pet ->
                        PetItem(pet = pet) // Create a proper pet item component
                    }
                }
            } else {
                Text(
                    "No pets added yet. Tap the button below to add your first pet.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Manage Pets Button
            OutlinedButton(
                onClick = onManagePets,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Pets")
            }
        }
    }
}

@Composable
fun PetItem(pet: com.example.tailstale.model.PetModel) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pet avatar/icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(getPetTypeColor(pet.type)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getPetEmoji(pet.type),
                    fontSize = 24.sp
                )
            }

            // Pet name
            Text(
                text = pet.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            // Pet type and age
            Text(
                text = "${pet.type.lowercase().replaceFirstChar { it.uppercase() }} • ${pet.age}mo",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            // Health indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Health",
                    modifier = Modifier.size(12.dp),
                    tint = if (pet.health > 70) Color.Green else if (pet.health > 40) Color.Yellow else Color.Red
                )
                Text(
                    text = "${pet.health}%",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Helper functions for pet display
private fun getPetTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "DOG" -> Color(0xFFD2691E)
        "CAT" -> Color(0xFF708090)
        "RABBIT" -> Color(0xFFDDA0DD)
        "BIRD" -> Color(0xFF87CEEB)
        "HAMSTER" -> Color(0xFFDAA520)
        else -> Color(0xFFFF9500)
    }
}

private fun getPetEmoji(type: String): String {
    return when (type.uppercase()) {
        "DOG" -> "🐕"
        "CAT" -> "🐱"
        "RABBIT" -> "🐰"
        "BIRD" -> "🐦"
        "HAMSTER" -> "🐹"
        else -> "🐾"
    }
}

@Composable
fun AccountActionsCard(
    onSignOut: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Account action items
            AccountActionItem(
                title = "Privacy & Security",
                subtitle = "Manage your privacy settings",
                icon = Icons.Default.Lock
            )

            AccountActionItem(
                title = "Backup & Sync",
                subtitle = "Sync your data across devices",
                icon = Icons.Default.Cloud
            )

            AccountActionItem(
                title = "Sign Out",
                subtitle = "Sign out of your account",
                icon = Icons.Default.ExitToApp,
                textColor = Color.Red,
                onClick = onSignOut
            )

            AccountActionItem(
                title = "Refresh Profile",
                subtitle = "Reload your profile data",
                icon = Icons.Default.Refresh,
                onClick = onRefresh
            )
        }
    }
}

@Composable
fun AccountActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (textColor == Color.Red) Color.Red else Color(0xFF007AFF)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
    }
}

@Composable
fun EditUserProfileDialog(
    userModel: UserModel,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var displayName by remember { mutableStateOf(userModel.displayName) }
    var email by remember { mutableStateOf(userModel.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profile")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(displayName, email)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Change Profile Picture",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Choose how you'd like to update your profile picture:")

                // Gallery option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGallery() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF007AFF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Choose from Gallery",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                "Select an existing photo",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Camera option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCamera() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF007AFF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Take a Photo",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                "Use your camera to take a new photo",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddAchievementDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Achievement")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(title, description)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

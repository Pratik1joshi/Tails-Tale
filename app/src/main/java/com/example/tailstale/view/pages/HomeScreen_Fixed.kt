package com.example.tailstale.view.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.R
import com.example.tailstale.di.AppModule
import com.example.tailstale.viewmodel.PetViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri

// Helper components for HomeScreen
@Composable
private fun PetOverlayIcon(
    painter: Painter,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    label: String = "", // Add label parameter
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = if (isEnabled) 0.7f else 0.3f),
            modifier = modifier
                .size(48.dp)
                .clickable(enabled = isEnabled) { onClick() }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(if (isEnabled) 1f else 0.5f)
                )
            }
        }

        // Small label text
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PetVideoPlayer(
    modifier: Modifier = Modifier,
    videoRes: Int = R.raw.sitting,
    isLooping: Boolean = true,
    onCompletion: () -> Unit = {}
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse("android.resource://${ctx.packageName}/$videoRes"))
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = isLooping
                    start()
                }
                setOnCompletionListener { onCompletion() }
            }
        },
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        update = { view ->
            view.setVideoURI(Uri.parse("android.resource://${context.packageName}/$videoRes"))
            view.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = isLooping
                view.start()
            }
            view.setOnCompletionListener { onCompletion() }
        }
    )
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
private fun CooldownIndicator(
    remainingTime: Long,
    modifier: Modifier = Modifier
) {
    if (remainingTime > 0) {
        Box(
            modifier = modifier
                .background(
                    Color.Red.copy(alpha = 0.8f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${remainingTime / 1000}s",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CompactStatusBar(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    isInverted: Boolean = false
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$value%",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
        ) {
            val displayValue = if (isInverted) 100 - value else value
            val barColor = if (isInverted && value > 70) Color.Red else color

            Box(
                modifier = Modifier
                    .fillMaxWidth(displayValue / 100f)
                    .height(6.dp)
                    .background(barColor, RoundedCornerShape(3.dp))
            )
        }
    }
}

// Helper function to calculate pet age
private fun calculatePetAge(creationDate: Long): String {
    val petDate = Date(creationDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val currentDate = LocalDate.now()
    val period = Period.between(petDate, currentDate)

    return when {
        period.years > 0 -> "${period.years} year${if (period.years > 1) "s" else ""} old"
        period.months > 0 -> "${period.months} month${if (period.months > 1) "s" else ""} old"
        period.days > 0 -> "${period.days} day${if (period.days > 1) "s" else ""} old"
        else -> "Newborn"
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Create PetViewModel with dependency injection - Updated to use our new aging system
    val petViewModel: PetViewModel = viewModel(
        factory = com.example.tailstale.viewmodel.PetViewModelFactory(
            com.example.tailstale.repo.PetRepositoryImpl(),
            com.example.tailstale.repo.UserRepositoryImpl()
        )
    )

    // Observe pet data with StateFlow collectAsState
    val pets by petViewModel.pets.collectAsState()
    val currentPet by petViewModel.currentPet.collectAsState()
    val petAgingStats by petViewModel.petAgingStats.collectAsState() // NEW: Aging stats
    val loading by petViewModel.loading.collectAsState()
    val error by petViewModel.error.collectAsState()

    // Cooldown system - 10 seconds
    val cooldownMillis = 10_000L
    var lastClickTime by remember { mutableStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    // Calculate remaining cooldown time
    var remainingCooldown by remember { mutableStateOf(0L) }

    LaunchedEffect(lastClickTime) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            val timeElapsed = currentTime - lastClickTime
            remainingCooldown = maxOf(0L, cooldownMillis - timeElapsed)
            if (remainingCooldown <= 0) break
            delay(100) // Update every 100ms for smooth countdown
        }
    }

    val isActionEnabled = remainingCooldown <= 0L

    // Video state
    var selectedVideoRes by remember { mutableStateOf(R.raw.sitting) }
    var isLooping by remember { mutableStateOf(true) }

    // NEW: Start real-time aging when screen loads
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            petViewModel.startRealTimeAging(userId) // Start aging service
            petViewModel.loadUserPets(userId)
        }
    }

    // NEW: Stop aging when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            petViewModel.stopRealTimeAging()
        }
    }

    // Function to handle action with cooldown and enhanced stat changes
    fun performAction(
        videoRes: Int,
        actionType: String,
        onPetAction: () -> Unit = {}
    ) {
        if (isActionEnabled) {
            lastClickTime = System.currentTimeMillis()

            // Perform pet action with enhanced effects
            currentPet?.let { pet ->
                when (actionType) {
                    "feed" -> {
                        val statsUpdate = mapOf(
                            "hunger" to maxOf(0, pet.hunger - 25), // Significantly reduce hunger
                            "happiness" to minOf(100, pet.happiness + 10), // Increase happiness
                            "health" to minOf(100, pet.health + 5), // Small health boost
                            "lastFed" to System.currentTimeMillis()
                        )
                        petViewModel.updatePetStatsDirect(pet.id, statsUpdate)
                    }
                    "play" -> {
                        val statsUpdate = mapOf(
                            "happiness" to minOf(100, pet.happiness + 20), // Big happiness boost
                            "energy" to maxOf(0, pet.energy - 15), // Tire the pet
                            "hunger" to minOf(100, pet.hunger + 5), // Playing makes hungry
                            "lastPlayed" to System.currentTimeMillis()
                        )
                        petViewModel.updatePetStatsDirect(pet.id, statsUpdate)
                    }
                    "clean" -> {
                        val statsUpdate = mapOf(
                            "cleanliness" to 100, // Full cleanliness
                            "happiness" to minOf(100, pet.happiness + 15), // Happy to be clean
                            "health" to minOf(100, pet.health + 10), // Health boost from cleanliness
                            "lastCleaned" to System.currentTimeMillis()
                        )
                        petViewModel.updatePetStatsDirect(pet.id, statsUpdate)
                    }
                    "sleep" -> {
                        val statsUpdate = mapOf(
                            "energy" to minOf(100, pet.energy + 30), // Rest restores energy
                            "health" to minOf(100, pet.health + 5), // Rest improves health
                            "happiness" to minOf(100, pet.happiness + 5) // Rested pets are happier
                        )
                        petViewModel.updatePetStatsDirect(pet.id, statsUpdate)
                    }
                }
            }

            // Play video
            selectedVideoRes = videoRes
            isLooping = false

            coroutineScope.launch {
                delay(10_000) // 10 seconds video duration
                selectedVideoRes = R.raw.sitting
                isLooping = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Loading state
        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error state - Fixed type inference
        error?.let { errorMessage: String ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (errorMessage.contains("success", ignoreCase = true))
                        Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // NEW: Pet Selection Row (if multiple pets)
        if (pets.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Pets (${pets.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pets.take(3).forEach { pet -> // Show max 3 pets to avoid overflow
                            Card(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(100.dp)
                                    .clickable { petViewModel.selectPet(pet) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentPet?.id == pet.id) Color(0xFF007AFF) else Color(0xFFF8F8F8)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (currentPet?.id == pet.id) 8.dp else 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = when (pet.type.uppercase()) {
                                            "DOG" -> "🐕"
                                            "CAT" -> "🐱"
                                            "RABBIT" -> "🐰"
                                            "BIRD" -> "🐦"
                                            "HAMSTER" -> "🐹"
                                            else -> "🐾"
                                        },
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    Text(
                                        text = pet.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (currentPet?.id == pet.id) Color.White else Color.Black,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = "${pet.age}mo",
                                        fontSize = 8.sp,
                                        color = if (currentPet?.id == pet.id) Color.White.copy(0.8f) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pet info header - Enhanced with aging info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentPet?.name ?: "Loading...",
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
                        text = currentPet?.type ?: "Pet",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentPet?.let { pet ->
                        "${pet.age} months old"
                    } ?: "Unknown age",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                // NEW: Growth stage indicator
                currentPet?.let { pet ->
                    Text(
                        text = pet.growthStage.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // NEW: Compact Status bars - 2 per row, smaller size for better pet visibility
        currentPet?.let { pet ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Health & Hunger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStatusBar(
                            label = "Health",
                            value = pet.health,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatusBar(
                            label = "Hunger",
                            value = pet.hunger,
                            color = Color(0xFFFF5722),
                            modifier = Modifier.weight(1f),
                            isInverted = true // Higher hunger = worse
                        )
                    }

                    // Row 2: Happiness & Energy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStatusBar(
                            label = "Happiness",
                            value = pet.happiness,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatusBar(
                            label = "Energy",
                            value = pet.energy,
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Cleanliness & Overall Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStatusBar(
                            label = "Clean",
                            value = pet.cleanliness,
                            color = Color(0xFF00BCD4),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatusBar(
                            label = "Score",
                            value = pet.getOverallHealthScore(),
                            color = when {
                                pet.getOverallHealthScore() >= 80 -> Color(0xFF4CAF50)
                                pet.getOverallHealthScore() >= 60 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NEW: Real-time aging info card (compact version)
        currentPet?.let { pet ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕐 Real-time aging active",
                        fontSize = 12.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Weight: ${String.format("%.1f", petAgingStats["weight"] ?: pet.weight)}kg",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${pet.growthStage.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            fontSize = 10.sp,
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // NEW: Force Age Button for testing (removable in production)
        currentPet?.let { pet ->
            OutlinedButton(
                onClick = { petViewModel.forceAgePet(pet.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF007AFF)
                )
            ) {
                Text("🕐 Force Age Update (Test)", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Video with overlay icons - NOW MORE PROMINENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp) // Increased height for better visibility
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Video as background
            PetVideoPlayer(
                modifier = Modifier.fillMaxSize(),
                videoRes = selectedVideoRes,
                isLooping = isLooping,
                onCompletion = {
                    selectedVideoRes = R.raw.sitting
                    isLooping = true
                }
            )

            // Cooldown indicator
            if (remainingCooldown > 0) {
                CooldownIndicator(
                    remainingTime = remainingCooldown,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            // Left-side icons
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sleeping icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_bed_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupsleeping, actionType = "sleep")
                    },
                    isEnabled = isActionEnabled,
                    label = "Sleep"
                )

                // Washing icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_wash_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupbathing, actionType = "clean")
                    },
                    isEnabled = isActionEnabled,
                    label = "Wash"
                )

                // Sitting icon action
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_chair_alt_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupsitting, actionType = "rest")
                    },
                    isEnabled = isActionEnabled,
                    label = "Sit"
                )

                // Bathing icon action
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_bathtub_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupbathing, actionType = "clean")
                    },
                    isEnabled = isActionEnabled,
                    label = "Bath"
                )
            }

            // Right-side icons
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Walking icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_directions_walk_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupwalking, actionType = "play")
                    },
                    isEnabled = isActionEnabled,
                    label = "Walk"
                )

                // Eating icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_restaurant_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupeating, actionType = "feed")
                    },
                    isEnabled = isActionEnabled,
                    label = "Eat"
                )

                // Playing icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_sports_basketball_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupplaying, actionType = "play")
                    },
                    isEnabled = isActionEnabled,
                    label = "Play"
                )

                // Health icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.outline_local_hospital_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupvaccination, actionType = "health")
                    },
                    isEnabled = isActionEnabled,
                    label = "Health"
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
                    "• Stats decay gradually over time - keep caring for your pet!\n" +
                            "• Feed when hungry (red bar) to restore hunger\n" +
                            "• Play to boost happiness and tire your pet\n" +
                            "• Clean regularly to maintain health\n" +
                            "• Let your pet sleep to restore energy",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

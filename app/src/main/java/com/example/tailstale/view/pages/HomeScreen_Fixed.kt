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

// Helper components for HomeScreen
@Composable
private fun PetOverlayIcon(
    painter: Painter,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
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
}

@Composable
private fun PetVideoPlayer(
    modifier: Modifier = Modifier,
    videoRes: Int = 0,
    isLooping: Boolean = true,
    onCompletion: () -> Unit = {}
) {
    // Placeholder for video - replace with actual video player when you have video files
    Box(
        modifier = modifier
            .background(
                Color(0xFFFFE0B2),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "🐕\nVideo Player\n(Playing: ${if (videoRes != 0) "Video" else "Default"})",
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
    val petViewModel: PetViewModel = viewModel(factory = AppModule.provideViewModelFactory())

    // Observe pet data with StateFlow collectAsState
    val pets by petViewModel.pets.collectAsState()
    val currentPet by petViewModel.currentPet.collectAsState()
    val loading by petViewModel.loading.collectAsState()
    val error by petViewModel.error.collectAsState()

    // Use pet stats from currentPet or default values
    val health = currentPet?.health ?: 85
    val hunger = currentPet?.hunger ?: 40
    val happiness = currentPet?.happiness ?: 70

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

    // Load pets when component mounts
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            println("DEBUG: Loading pets for user: ${user.uid}")
            petViewModel.loadUserPets(user.uid)
        } ?: run {
            println("DEBUG: No current user found")
        }
    }

    // Debug logging for state changes
    LaunchedEffect(pets, currentPet, loading, error) {
        println("DEBUG: pets.size = ${pets.size}")
        println("DEBUG: currentPet = ${currentPet?.name}")
        println("DEBUG: loading = $loading")
        println("DEBUG: error = $error")
    }

    // Function to handle action with cooldown
    fun performAction(
        videoRes: Int,
        onPetAction: () -> Unit = {}
    ) {
        if (isActionEnabled) {
            lastClickTime = System.currentTimeMillis()

            // Perform pet action
            onPetAction()

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
            Text(
                text = "Error: $errorMessage",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Pet info header - Dynamic content with explicit null safety
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

            Text(
                text = currentPet?.let { pet: com.example.tailstale.model.PetModel ->
                    calculatePetAge(pet.creationDate)
                } ?: "Unknown age",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cooldown indicator
        if (remainingCooldown > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CooldownIndicator(remainingCooldown)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Status bars - Using actual pet stats
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
                        performAction(videoRes = R.raw.pupsleeping) {
                            // No specific pet action for sleeping
                        }
                    },
                    isEnabled = isActionEnabled
                )

                // Washing icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_wash_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupbathing) {
                            petViewModel.cleanPet()
                        }
                    },
                    isEnabled = isActionEnabled
                )

                // Sitting icon action
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_chair_alt_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupsitting) {
                            // No specific pet action for sitting
                        }
                    },
                    isEnabled = isActionEnabled
                )

                // Bathing icon action
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_bathtub_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupbathing) {
                            petViewModel.cleanPet()
                        }
                    },
                    isEnabled = isActionEnabled
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
                        performAction(videoRes = R.raw.pupwalking) {
                            petViewModel.playWithPet("walking")
                        }
                    },
                    isEnabled = isActionEnabled
                )

                // Eating icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_restaurant_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupeating) {
                            petViewModel.feedPet("food")
                        }
                    },
                    isEnabled = isActionEnabled
                )

                // Playing icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.baseline_sports_basketball_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupplaying) {
                            petViewModel.playWithPet("ball")
                        }
                    },
                    isEnabled = isActionEnabled
                )

                // Health icon actions
                PetOverlayIcon(
                    painter = painterResource(id = R.drawable.outline_local_hospital_24),
                    onClick = {
                        performAction(videoRes = R.raw.pupvaccination) {
                            // Health checkup action - could add to ViewModel later
                        }
                    },
                    isEnabled = isActionEnabled
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
                            "• Regular exercise keeps your pet healthy\n" +
                            "• Actions have a 10-second cooldown to simulate realistic pet care",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

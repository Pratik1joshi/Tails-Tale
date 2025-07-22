//package com.example.tailstale.view.pages
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import coil.compose.AsyncImage
//import com.example.tailstale.model.PetModel
//import com.example.tailstale.model.GrowthStage
//import com.example.tailstale.repo.PetRepositoryImpl
//import com.example.tailstale.repo.UserRepositoryImpl
//import com.example.tailstale.viewmodel.PetViewModel
//import com.example.tailstale.viewmodel.PetViewModelFactory
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.delay
//
//@Composable
//fun HomeScreen() {
//    val context = LocalContext.current
//    val currentUser = FirebaseAuth.getInstance().currentUser
//
//    // Create PetViewModel with dependency injection
//    val petViewModel: PetViewModel = viewModel(
//        factory = PetViewModelFactory(
//            PetRepositoryImpl(),
//            UserRepositoryImpl()
//        )
//    )
//
//    val pets by petViewModel.pets.collectAsState()
//    val currentPet by petViewModel.currentPet.collectAsState()
//    val petAgingStats by petViewModel.petAgingStats.collectAsState()
//    val loading by petViewModel.loading.collectAsState()
//    val error by petViewModel.error.collectAsState()
//
//    // Start real-time aging when screen loads
//    LaunchedEffect(currentUser?.uid) {
//        currentUser?.uid?.let { userId ->
//            petViewModel.startRealTimeAging(userId)
//            petViewModel.loadUserPets(userId)
//        }
//    }
//
//    // Stop aging when screen is disposed
//    DisposableEffect(Unit) {
//        onDispose {
//            petViewModel.stopRealTimeAging()
//        }
//    }
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5))
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        item {
//            // Welcome Header
//            WelcomeHeaderCard(currentUser?.displayName ?: "User")
//        }
//
//        item {
//            // Pet Selection Row
//            if (pets.isNotEmpty()) {
//                PetSelectionCard(
//                    pets = pets,
//                    selectedPet = currentPet,
//                    onPetSelected = { pet -> petViewModel.selectPet(pet) }
//                )
//            }
//        }
//
//        item {
//            // Current Pet Display
//            currentPet?.let { pet ->
//                CurrentPetCard(
//                    pet = pet,
//                    agingStats = petAgingStats,
//                    onForceAge = { petViewModel.forceAgePet(pet.id) }
//                )
//            }
//        }
//
//        item {
//            // Pet Stats Card
//            currentPet?.let { pet ->
//                PetStatsCard(pet = pet)
//            }
//        }
//
//        item {
//            // Pet Care Actions
//            currentPet?.let { pet ->
//                PetCareActionsCard(
//                    pet = pet,
//                    onFeed = { petViewModel.feedPet("default_food") },
//                    onPlay = { petViewModel.playWithPet("default_toy") },
//                    onClean = { petViewModel.cleanPet() }
//                )
//            }
//        }
//
//        item {
//            // Real-time Aging Info Card
//            currentPet?.let { pet ->
//                RealTimeAgingCard(
//                    pet = pet,
//                    agingStats = petAgingStats
//                )
//            }
//        }
//
//        // Error display
//        error?.let { errorMessage ->
//            item {
//                ErrorCard(
//                    message = errorMessage,
//                    onDismiss = { petViewModel.clearError() }
//                )
//            }
//        }
//
//        // Loading indicator
//        if (loading) {
//            item {
//                Box(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun WelcomeHeaderCard(userName: String) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Text(
//                text = "Welcome back, $userName! 👋",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//
//            Text(
//                text = "Your pets are growing in real-time!",
//                fontSize = 16.sp,
//                color = Color.Gray,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun PetSelectionCard(
//    pets: List<PetModel>,
//    selectedPet: PetModel?,
//    onPetSelected: (PetModel) -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "Your Pets (${pets.size})",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black,
//                modifier = Modifier.padding(bottom = 12.dp)
//            )
//
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(pets) { pet ->
//                    PetSelectionItem(
//                        pet = pet,
//                        isSelected = selectedPet?.id == pet.id,
//                        onClick = { onPetSelected(pet) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PetSelectionItem(
//    pet: PetModel,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .width(100.dp)
//            .height(120.dp)
//            .clickable { onClick() },
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) Color(0xFF007AFF) else Color(0xFFF8F8F8)
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Pet emoji based on type
//            Text(
//                text = getPetEmoji(pet.type),
//                fontSize = 32.sp,
//                modifier = Modifier.padding(bottom = 4.dp)
//            )
//
//            Text(
//                text = pet.name,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.Medium,
//                color = if (isSelected) Color.White else Color.Black,
//                textAlign = TextAlign.Center,
//                maxLines = 1
//            )
//
//            Text(
//                text = "${pet.age}mo",
//                fontSize = 10.sp,
//                color = if (isSelected) Color.White.copy(0.8f) else Color.Gray
//            )
//        }
//    }
//}
//
//@Composable
//fun CurrentPetCard(
//    pet: PetModel,
//    agingStats: Map<String, Any>,
//    onForceAge: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Pet Avatar
//            Box(
//                modifier = Modifier
//                    .size(120.dp)
//                    .clip(CircleShape)
//                    .background(getPetTypeColor(pet.type)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = getPetEmoji(pet.type),
//                    fontSize = 48.sp
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Pet Name and Age
//            Text(
//                text = pet.name,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//
//            Text(
//                text = "${pet.age} months old • ${pet.growthStage.name.lowercase().replaceFirstChar { it.uppercase() }}",
//                fontSize = 16.sp,
//                color = Color.Gray
//            )
//
//            Text(
//                text = pet.getLifeStageDescription(),
//                fontSize = 14.sp,
//                color = Color.Gray,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Force Age Button (for testing)
//            OutlinedButton(
//                onClick = onForceAge,
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = Color(0xFF007AFF)
//                )
//            ) {
//                Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(16.dp))
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Force Age Update")
//            }
//        }
//    }
//}
//
//@Composable
//fun PetStatsCard(pet: PetModel) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Text(
//                text = "Pet Stats",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            // Stats grid
//            Column(
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                StatBar("Health", pet.health, Color(0xFF4CAF50))
//                StatBar("Happiness", pet.happiness, Color(0xFFFFEB3B))
//                StatBar("Hunger", 100 - pet.hunger, Color(0xFFFF9800)) // Invert hunger display
//                StatBar("Energy", pet.energy, Color(0xFF2196F3))
//                StatBar("Cleanliness", pet.cleanliness, Color(0xFF9C27B0))
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Overall health score
//            val healthScore = pet.getOverallHealthScore()
//            Text(
//                text = "Overall Score: $healthScore/100",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = when {
//                    healthScore >= 80 -> Color(0xFF4CAF50)
//                    healthScore >= 60 -> Color(0xFFFF9800)
//                    else -> Color(0xFFF44336)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun StatBar(label: String, value: Int, color: Color) {
//    Column {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = label,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black
//            )
//            Text(
//                text = "$value%",
//                fontSize = 14.sp,
//                color = Color.Gray
//            )
//        }
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(8.dp)
//                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth(value / 100f)
//                    .height(8.dp)
//                    .background(color, RoundedCornerShape(4.dp))
//            )
//        }
//    }
//}
//
//@Composable
//fun PetCareActionsCard(
//    pet: PetModel,
//    onFeed: () -> Unit,
//    onPlay: () -> Unit,
//    onClean: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Text(
//                text = "Care Actions",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                CareActionButton(
//                    icon = Icons.Default.Restaurant,
//                    label = "Feed",
//                    color = Color(0xFF4CAF50),
//                    needsAttention = pet.hunger > 70,
//                    onClick = onFeed
//                )
//
//                CareActionButton(
//                    icon = Icons.Default.SportsBaseball,
//                    label = "Play",
//                    color = Color(0xFF2196F3),
//                    needsAttention = pet.happiness < 40,
//                    onClick = onPlay
//                )
//
//                CareActionButton(
//                    icon = Icons.Default.CleaningServices,
//                    label = "Clean",
//                    color = Color(0xFF9C27B0),
//                    needsAttention = pet.cleanliness < 30,
//                    onClick = onClean
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun CareActionButton(
//    icon: ImageVector,
//    label: String,
//    color: Color,
//    needsAttention: Boolean,
//    onClick: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Button(
//            onClick = onClick,
//            modifier = Modifier.size(64.dp),
//            shape = CircleShape,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (needsAttention) Color(0xFFFF5722) else color
//            )
//        ) {
//            Icon(
//                icon,
//                contentDescription = label,
//                modifier = Modifier.size(28.dp),
//                tint = Color.White
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = label,
//            fontSize = 12.sp,
//            color = Color.Black,
//            fontWeight = if (needsAttention) FontWeight.Bold else FontWeight.Normal
//        )
//
//        if (needsAttention) {
//            Text(
//                text = "⚠️",
//                fontSize = 16.sp
//            )
//        }
//    }
//}
//
//@Composable
//fun RealTimeAgingCard(
//    pet: PetModel,
//    agingStats: Map<String, Any>
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    Icons.Default.Schedule,
//                    contentDescription = null,
//                    tint = Color(0xFF007AFF),
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Real-Time Aging Status",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF007AFF)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Aging information
//            AgingInfoRow("Current Age", "${agingStats["ageInMonths"] ?: 0} months")
//            AgingInfoRow("Growth Stage", agingStats["growthStage"]?.toString() ?: "Unknown")
//            AgingInfoRow("Weight", "${agingStats["weight"] ?: 0.0} kg")
//            AgingInfoRow("Real Days Alive", "${agingStats["ageInRealDays"] ?: 0} days")
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Timing information
//            val nextAgeUp = agingStats["nextAgeUpIn"] as? Long ?: 0
//            val nextStatsDecay = agingStats["nextStatsDecayIn"] as? Long ?: 0
//
//            if (nextAgeUp > 0) {
//                AgingInfoRow("Next Age Up", "in ${nextAgeUp}h")
//            } else {
//                AgingInfoRow("Next Age Up", "Soon!")
//            }
//
//            if (nextStatsDecay > 0) {
//                AgingInfoRow("Stats Decay", "in ${nextStatsDecay}m")
//            } else {
//                AgingInfoRow("Stats Decay", "Now!")
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Text(
//                text = "🕐 Pets age 1 month every real hour\n📉 Stats decay every 30 minutes\n💾 All changes auto-save to database",
//                fontSize = 12.sp,
//                color = Color.Gray,
//                lineHeight = 16.sp
//            )
//        }
//    }
//}
//
//@Composable
//fun AgingInfoRow(label: String, value: String) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = label,
//            fontSize = 14.sp,
//            color = Color.Gray
//        )
//        Text(
//            text = value,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color.Black
//        )
//    }
//}
//
//@Composable
//fun ErrorCard(message: String, onDismiss: () -> Unit) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = if (message.contains("success", ignoreCase = true))
//                Color(0xFF4CAF50) else Color(0xFFFF5722)
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                if (message.contains("success", ignoreCase = true))
//                    Icons.Default.CheckCircle else Icons.Default.Error,
//                contentDescription = null,
//                tint = Color.White,
//                modifier = Modifier.size(20.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = message,
//                color = Color.White,
//                fontSize = 14.sp,
//                modifier = Modifier.weight(1f)
//            )
//            IconButton(onClick = onDismiss) {
//                Icon(
//                    Icons.Default.Close,
//                    contentDescription = "Dismiss",
//                    tint = Color.White,
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//        }
//    }
//}
//
//// Helper functions
//private fun getPetEmoji(type: String): String {
//    return when (type.uppercase()) {
//        "DOG" -> "🐕"
//        "CAT" -> "🐱"
//        "RABBIT" -> "🐰"
//        "BIRD" -> "🐦"
//        "HAMSTER" -> "🐹"
//        else -> "🐾"
//    }
//}
//
//private fun getPetTypeColor(type: String): Color {
//    return when (type.uppercase()) {
//        "DOG" -> Color(0xFFD2691E)
//        "CAT" -> Color(0xFF708090)
//        "RABBIT" -> Color(0xFFDDA0DD)
//        "BIRD" -> Color(0xFF87CEEB)
//        "HAMSTER" -> Color(0xFFDAA520)
//        else -> Color(0xFFFF9500)
//    }
//}

package com.example.tailstale.view.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.di.AppModule
import com.example.tailstale.model.PetType
import com.example.tailstale.viewmodel.PetViewModel
import com.google.firebase.auth.FirebaseAuth

enum class PetTypeEnum(val displayName: String, val icon: ImageVector, val emoji: String) {
    DOG("Dog", Icons.Default.Star, "🐕"),
    CAT("Cat", Icons.Default.Star, "🐱"),
    BIRD("Bird", Icons.Default.Star, "🐦"),
    RABBIT("Rabbit", Icons.Default.Star, "🐰"),
    HAMSTER("Hamster", Icons.Default.Star, "🐹")
}

enum class PetColor(val displayName: String, val color: Color) {
    BROWN("Brown", Color(0xFF8D6E63)),
    BLACK("Black", Color(0xFF424242)),
    WHITE("White", Color(0xFFF5F5F5)),
    GOLDEN("Golden", Color(0xFFFFB300)),
    GRAY("Gray", Color(0xFF757575)),
    ORANGE("Orange", Color(0xFFFF7043))
}

enum class Gender(val displayName: String, val icon: ImageVector) {
    MALE("Male", Icons.Default.Person),
    FEMALE("Female", Icons.Default.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val petViewModel: PetViewModel = viewModel(
        factory = com.example.tailstale.viewmodel.PetViewModelFactory(
            com.example.tailstale.repo.PetRepositoryImpl(),
            com.example.tailstale.repo.UserRepositoryImpl()
        )
    )

    // Observe pets and states
    val pets by petViewModel.pets.collectAsState()
    val currentPet by petViewModel.currentPet.collectAsState()
    val loading by petViewModel.loading.collectAsState()
    val error by petViewModel.error.collectAsState()

    // Form states
    var petName by remember { mutableStateOf("") }
    var selectedPetType by remember { mutableStateOf(PetTypeEnum.DOG) }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showModeSelection by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }

    // Load user's pets when screen loads
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            petViewModel.loadUserPets(userId)
        }
    }

    // Show success dialog when pet is created
    LaunchedEffect(pets.size) {
        if (pets.isNotEmpty() && petName.isNotEmpty()) {
            showSuccessDialog = true
            petName = "" // Reset form
        }
    }

    // Clear error after showing
    LaunchedEffect(error) {
        if (error != null && error!!.contains("successfully", ignoreCase = true)) {
            kotlinx.coroutines.delay(3000)
            petViewModel.clearError()
        }
    }

    // Validation states
    val isNameValid = petName.isNotBlank() && petName.length >= 2
    val isFormValid = isNameValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header with conditional title
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (pets.isEmpty()) Icons.Default.Add else Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF007AFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (pets.isEmpty()) "Add Your First Pet" else "Manage Your Pets",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        if (pets.isEmpty()) "Create your virtual companion" else "You have ${pets.size} pet${if (pets.size > 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show current pets if any exist
            if (pets.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Your Pets",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        pets.forEach { pet ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { petViewModel.selectPet(pet) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentPet?.id == pet.id) Color(0xFF007AFF).copy(alpha = 0.1f) else Color(0xFFF8F8F8)
                                ),
                                border = if (currentPet?.id == pet.id) BorderStroke(2.dp, Color(0xFF007AFF)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
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
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            pet.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                        Text(
                                            "${pet.type.lowercase().replaceFirstChar { it.uppercase() }} • ${pet.age} months old",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (currentPet?.id == pet.id) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode selection buttons for existing pets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showModeSelection = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF007AFF)
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Pet", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showAddForm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Another", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Pet creation form (always show for first pet, or when add another is clicked)
            if (pets.isEmpty() || showAddForm) {
                // Pet Name Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Pet Name",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your pet's name") },
                            isError = petName.isNotBlank() && !isNameValid,
                            supportingText = {
                                if (petName.isNotBlank() && !isNameValid) {
                                    Text("Name must be at least 2 characters", color = Color.Red)
                                }
                            },
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pet Type Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Pet Type",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(PetTypeEnum.values()) { petType ->
                                PetTypeCard(
                                    petType = petType,
                                    isSelected = selectedPetType == petType,
                                    onSelect = { selectedPetType = petType }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Gender",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Gender.values().forEach { gender ->
                                GenderOption(
                                    gender = gender,
                                    isSelected = selectedGender == gender,
                                    onSelect = { selectedGender = gender },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create Pet Button
                Button(
                    onClick = {
                        if (isFormValid && currentUser != null) {
                            val petType = when (selectedPetType) {
                                PetTypeEnum.DOG -> PetType.DOG
                                PetTypeEnum.CAT -> PetType.CAT
                                PetTypeEnum.BIRD -> PetType.BIRD
                                PetTypeEnum.RABBIT -> PetType.RABBIT
                                PetTypeEnum.HAMSTER -> PetType.HAMSTER
                            }

                            petViewModel.createPet(
                                name = petName,
                                petType = petType,
                                userId = currentUser.uid
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !loading && currentUser != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (pets.isEmpty()) "Create My Pet" else "Add Another Pet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Error message
                error?.let { errorMessage ->
                    if (!errorMessage.contains("successfully", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5722)
                            )
                        ) {
                            Text(
                                errorMessage,
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccessDialog = false
                        showAddForm = false
                    },
                    title = {
                        Text(
                            "Success!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    },
                    text = {
                        Text("Your new pet has been created successfully! You can now take care of them on the Home screen.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSuccessDialog = false
                                showAddForm = false
                            }
                        ) {
                            Text("OK", color = Color(0xFF007AFF))
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    }
                )
            }

            // Mode Selection Dialog
            if (showModeSelection && pets.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { showModeSelection = false },
                    title = {
                        Text(
                            "Choose Action",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text("What would you like to do?")
                            Spacer(modifier = Modifier.height(16.dp))

                            // Change Pet Option
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showModeSelection = false
                                        // Just close dialog, pets are already shown above
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF007AFF).copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFF007AFF))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Select a different pet from your list", color = Color.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Add Pet Option
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showModeSelection = false
                                        showAddForm = true
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF4CAF50))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Create a new pet", color = Color.Black)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showModeSelection = false }
                        ) {
                            Text("Cancel", color = Color(0xFF007AFF))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PetTypeCard(
    petType: PetTypeEnum,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF007AFF) else Color.White
        ),
        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                petType.emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                petType.displayName,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GenderOption(
    gender: Gender,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF007AFF) else Color.White
        ),
        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                gender.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                gender.displayName,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

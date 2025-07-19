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

// Data class for Pet
data class Pet(
    val id: String = "",
    val name: String = "",
    val type: PetType = PetType.DOG,
    val breed: String = "",
    val age: Int = 0,
    val color: PetColor = PetColor.BROWN,
    val gender: Gender = Gender.MALE,
    val health: Int = 100,
    val hunger: Int = 50,
    val happiness: Int = 80,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PetType(val displayName: String, val icon: ImageVector, val emoji: String) {
    DOG("Dog", Icons.Default.Star, "🐕"),//Pets
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
    MALE("Male", Icons.Default.Person),//male
    FEMALE("Female", Icons.Default.AccountCircle)//female
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen() {
    var petName by remember { mutableStateOf("") }
    var selectedPetType by remember { mutableStateOf(PetType.DOG) }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PetColor.BROWN) }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Get theme colors for dark mode support
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline

    // Validation states
    val isNameValid = petName.isNotBlank() && petName.length >= 2
    val isAgeValid = petAge.isNotBlank() && petAge.toIntOrNull() != null && petAge.toInt() in 1..30
    val isFormValid = isNameValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Dynamic background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor), // Dynamic surface color
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = primaryColor // Dynamic primary color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add New Pet",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor // Dynamic text color
                    )
                    Text(
                        "Create your virtual companion",
                        fontSize = 14.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f) // Secondary text color
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pet Name Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor) // Dynamic surface color
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pet Name",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor // Dynamic text color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Enter your pet's name",
                                color = onSurfaceColor.copy(alpha = 0.6f) // Dynamic placeholder color
                            )
                        },
                        isError = petName.isNotBlank() && !isNameValid,
                        supportingText = {
                            if (petName.isNotBlank() && !isNameValid) {
                                Text("Name must be at least 2 characters", color = errorColor)
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = outlineColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pet Type Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor) // Dynamic surface color
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pet Type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor // Dynamic text color
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(PetType.values()) { petType ->
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
                colors = CardDefaults.cardColors(containerColor = surfaceColor) // Dynamic surface color
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Gender",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor // Dynamic text color
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
                    if (isFormValid) {
                        isLoading = true
                        // Simulate API call
                        createPet(
                            pet = Pet(
                                id = "pet_${System.currentTimeMillis()}",
                                name = petName,
                                type = selectedPetType,
                                gender = selectedGender
                            ),
                            onSuccess = {
                                isLoading = false
                                showSuccessDialog = true
                                // Reset form
                                petName = ""
                                selectedPetType = PetType.DOG
                                selectedGender = Gender.MALE
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor, // Dynamic primary color
                    disabledContainerColor = onSurfaceColor.copy(alpha = 0.3f) // Dynamic disabled color
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary // Dynamic progress indicator color
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary // Dynamic icon color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create My Pet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary // Dynamic text color
                    )
                }
            }

            // Error message
            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    errorMessage,
                    color = errorColor, // Dynamic error color
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Text(
                        "Success!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50) // Keep success color consistent
                    )
                },
                text = {
                    Text(
                        "Your new pet has been created successfully!",
                        color = onSurfaceColor // Dynamic text color
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showSuccessDialog = false }
                    ) {
                        Text("OK", color = primaryColor) // Dynamic button color
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50) // Keep success color consistent
                    )
                },
                containerColor = surfaceColor, // Dynamic dialog background
                textContentColor = onSurfaceColor // Dynamic dialog text color
            )
        }
    }
}

@Composable
fun PetTypeCard(
    petType: PetType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor else surfaceColor // Dynamic colors
        ),
        border = if (isSelected) null else BorderStroke(1.dp, outlineColor.copy(alpha = 0.3f)) // Dynamic border
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
                color = if (isSelected) onPrimaryColor else onSurfaceColor, // Dynamic text color
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
    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor else surfaceColor // Dynamic colors
        ),
        border = if (isSelected) null else BorderStroke(1.dp, outlineColor.copy(alpha = 0.3f)) // Dynamic border
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
                tint = if (isSelected) onPrimaryColor else onSurfaceColor.copy(alpha = 0.7f), // Dynamic icon color
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                gender.displayName,
                fontSize = 14.sp,
                color = if (isSelected) onPrimaryColor else onSurfaceColor, // Dynamic text color
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Simulate pet creation (replace with actual repository/API call)
private fun createPet(
    pet: Pet,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    // Simulate network delay
    Thread {
        try {
            Thread.sleep(2000) // 2 second delay
            // Simulate success (90% success rate)
            if (Math.random() > 0.1) {
                // Here you would typically save to database/API
                println("Pet created: $pet")
                onSuccess()
            } else {
                onError("Failed to create pet. Please try again.")
            }
        } catch (e: Exception) {
            onError("An error occurred: ${e.message}")
        }
    }.start()
}
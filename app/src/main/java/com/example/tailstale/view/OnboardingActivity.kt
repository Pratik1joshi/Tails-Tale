package com.example.tailstale.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tailstale.model.PetType

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnboardingStoryFlow()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OnboardingStoryFlow() {
    var currentStep by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var selectedPetType by remember { mutableStateOf<PetType?>(null) }
    var petName by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Handle navigation to login
    fun navigateToLogin() {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra("FROM_ONBOARDING", true)
        }
        context.startActivity(intent)
        (context as? ComponentActivity)?.finish()
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(600)) with
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(600))
        },
        label = "step_transition"
    ) { step ->
        when (step) {
            0 -> WelcomeStep(
                onNext = { currentStep = 1 },
                onLogin = { navigateToLogin() }
            )
            1 -> NameInputStep(
                userName = userName,
                onNameChange = { userName = it },
                onNext = { currentStep = 2 }
            )
            2 -> PetLoveStep(userName = userName, onNext = { currentStep = 3 })
            3 -> PetSelectionStep(
                userName = userName,
                selectedPetType = selectedPetType,
                onPetTypeSelected = { selectedPetType = it },
                onNext = { currentStep = 4 }
            )
            4 -> PetReactionStep(
                userName = userName,
                selectedPetType = selectedPetType!!,
                onNext = { currentStep = 5 }
            )
            5 -> PetNamingStep(
                userName = userName,
                selectedPetType = selectedPetType!!,
                petName = petName,
                onPetNameChange = { petName = it },
                onNext = {
                    val intent = Intent(context, SignUpActivity::class.java).apply {
                        putExtra("USER_NAME", userName)
                        putExtra("PET_TYPE", selectedPetType?.name)
                        putExtra("PET_NAME", petName)
                    }
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }
            )
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit, onLogin: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_animation"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f + animatedFloat * 0.2f),
                        Color(0xFF764ba2).copy(alpha = 0.5f + animatedFloat * 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.4f + animatedFloat * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏰",
                fontSize = 120.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .scale(scaleAnimation)
                    .graphicsLayer {
                        translationY = floatingOffset
                    }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(3) { index ->
                    val sparkleScale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000 + index * 200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "sparkle_$index"
                    )
                    Text(
                        text = "✨",
                        fontSize = 24.sp,
                        modifier = Modifier.scale(sparkleScale)
                    )
                }
            }

            AnimatedVisibility(
                visible = scaleAnimation > 0.8f,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome to Pet Kingdom!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "A magical realm where dreams come alive and friendships bloom forever!",
                        fontSize = 18.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = scaleAnimation > 0.9f,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Text(
                            text = "Begin My Adventure ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(2.dp, Color(0xFF8B5CF6)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Text(
                            text = "I Already Have an Account 🏠",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameInputStep(
    userName: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val animatedFloat by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f + animatedFloat * 0.2f),
                        Color(0xFF764ba2).copy(alpha = 0.5f + animatedFloat * 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.4f + animatedFloat * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "What should we call you?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Don't worry, you can change this later!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = userName,
                onValueChange = onNameChange,
                placeholder = { Text("Enter your name", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedIndicatorColor = Color(0xFF667eea),
                    unfocusedIndicatorColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = onNext,
                enabled = userName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PetLoveStep(userName: String, onNext: () -> Unit) {
    val animatedFloat by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f + animatedFloat * 0.2f),
                        Color(0xFF764ba2).copy(alpha = 0.5f + animatedFloat * 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.4f + animatedFloat * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hi $userName! 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Do you love pets?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "We're about to help you find your perfect virtual companion!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                Text(
                    text = "Yes, I love pets! 🐾",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PetSelectionStep(
    userName: String,
    selectedPetType: PetType?,
    onPetTypeSelected: (PetType) -> Unit,
    onNext: () -> Unit
) {
    val animatedFloat by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f + animatedFloat * 0.2f),
                        Color(0xFF764ba2).copy(alpha = 0.5f + animatedFloat * 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.4f + animatedFloat * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose your pet type, $userName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                items(PetType.values()) { petType ->
                    PetSelectionCard(
                        petType = petType,
                        isSelected = selectedPetType == petType,
                        onSelected = { onPetTypeSelected(petType) }
                    )
                }
            }

            Button(
                onClick = onNext,
                enabled = selectedPetType != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PetReactionStep(
    userName: String,
    selectedPetType: PetType,
    onNext: () -> Unit
) {
    val animatedFloat by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f + animatedFloat * 0.2f),
                        Color(0xFF764ba2).copy(alpha = 0.5f + animatedFloat * 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.4f + animatedFloat * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Great choice!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "You chose a ${selectedPetType.name.lowercase()}! 🎉",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "Now let's give your new friend a name!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                Text(
                    text = "Let's name it!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetNamingStep(
    userName: String,
    selectedPetType: PetType,
    petName: String,
    onPetNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val animatedFloat by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f + animatedFloat * 0.2f),
                        Color(0xFF764ba2).copy(alpha = 0.5f + animatedFloat * 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.4f + animatedFloat * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "What's your ${selectedPetType.name.lowercase()}'s name?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = petName,
                onValueChange = onPetNameChange,
                placeholder = { Text("Enter pet name", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    focusedIndicatorColor = Color(0xFF667eea),
                    unfocusedIndicatorColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = onNext,
                enabled = petName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Complete Setup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PetSelectionCard(
    petType: PetType,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    Card(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clickable { onSelected() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF667eea) else Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getPetEmoji(petType),
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = petType.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getPetEmoji(petType: PetType): String {
    return when (petType) {
        PetType.DOG -> "🐕"
        PetType.CAT -> "🐱"
        PetType.BIRD -> "🐦"
        PetType.RABBIT -> "🐰"
        PetType.HAMSTER -> "🐹"
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    OnboardingStoryFlow()
}
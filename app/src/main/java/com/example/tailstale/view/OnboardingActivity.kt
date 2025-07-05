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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay
import kotlin.math.sin
import androidx.compose.animation.ExperimentalAnimationApi

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

    // Smooth transitions between steps
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
            0 -> WelcomeStep(onNext = { currentStep = 1 })
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
                    // Navigate to SignUpActivity with data
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
fun WelcomeStep(onNext: () -> Unit) {
    // Animated background
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

    // Floating animation for castle
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Scale animation for entrance
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
            // Animated castle with floating effect
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

            // Animated sparkles
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
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFffeaa7).copy(alpha = 0.3f),
                        Color(0xFFfdcb6e).copy(alpha = 0.5f),
                        Color(0xFFe17055).copy(alpha = 0.3f)
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
            // Animated waving hand
            Text(
                text = "👋",
                fontSize = 80.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .graphicsLayer {
                        rotationZ = sin(waveAnimation * 2 * Math.PI.toFloat()) * 20f
                    }
            )

            Text(
                text = "Hello, Adventurer!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Every great journey begins with a name. What should we call you?",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = userName,
                onValueChange = onNameChange,
                label = { Text("Your Name") },
                placeholder = { Text("Enter your magical name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFF8B5CF6),
                    focusedLabelColor = Color(0xFF8B5CF6)
                ),
                singleLine = true
            )

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = userName.isNotBlank(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Text(
                    text = "Continue the Magic! ✨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PetLoveStep(userName: String, onNext: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "hearts")

    // Multiple floating hearts
    val heartOffsets = remember {
        List(5) { index ->
            Pair(
                (-50..50).random().dp,
                (index * 100).dp
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFfd79a8).copy(alpha = 0.3f),
                        Color(0xFFfdcb6e).copy(alpha = 0.4f),
                        Color(0xFFe84393).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Floating hearts background
        heartOffsets.forEachIndexed { index, (x, y) ->
            val floatingY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + index * 300, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heart_$index"
            )

            Text(
                text = "💕",
                fontSize = 24.sp,
                modifier = Modifier
                    .offset(x = x, y = y)
                    .graphicsLayer {
                        translationY = floatingY
                        alpha = 0.6f
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bouncing paw
            val bounceAnimation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bounce"
            )

            Text(
                text = "🐾",
                fontSize = 80.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .graphicsLayer {
                        translationY = bounceAnimation
                    }
            )

            Text(
                text = "Hey $userName!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "I have a feeling you're someone who loves adorable companions... Am I right?",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe84393)
                )
            ) {
                Text(
                    text = "Yes, I Love Pets! 💕",
                    fontSize = 16.sp,
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
    val petOptions = listOf(
        PetType.DOG to "🐕",
        PetType.CAT to "🐱",
        PetType.RABBIT to "🐰",
        PetType.BIRD to "🐦",
        PetType.HAMSTER to "🐹"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF74b9ff).copy(alpha = 0.3f),
                        Color(0xFF0984e3).copy(alpha = 0.4f),
                        Color(0xFF6c5ce7).copy(alpha = 0.3f)
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
                text = "Perfect, $userName!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Which of these adorable companions speaks to your heart?",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                items(petOptions) { (petType, emoji) ->
                    PetSelectionCard(
                        petType = petType,
                        emoji = emoji,
                        isSelected = selectedPetType == petType,
                        onClick = { onPetTypeSelected(petType) }
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedPetType != null,
                enter = fadeIn() + slideInVertically()
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6c5ce7)
                    )
                ) {
                    Text(
                        text = "This One's Perfect! 💖",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PetSelectionCard(
    petType: PetType,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF6c5ce7).copy(alpha = 0.3f)
            else Color.White.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            3.dp,
            if (isSelected) Color(0xFF6c5ce7) else Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 40.sp
            )
        }
    }
}

@Composable
fun PetReactionStep(
    userName: String,
    selectedPetType: PetType,
    onNext: () -> Unit
) {
    val petEmoji = when(selectedPetType) {
        PetType.DOG -> "🐕"
        PetType.CAT -> "🐱"
        PetType.RABBIT -> "🐰"
        PetType.BIRD -> "🐦"
        PetType.HAMSTER -> "🐹"
    }

    val petName = selectedPetType.name.lowercase().replaceFirstChar { it.uppercase() }

    // Celebration animation
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val celebration by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebration"
    )

    LaunchedEffect(Unit) {
        delay(3000)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00b894).copy(alpha = 0.4f),
                        Color(0xFF00cec9).copy(alpha = 0.5f),
                        Color(0xFF74b9ff).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Celebration particles
        repeat(10) { index ->
            val particleOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + index * 100, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particle_$index"
            )

            Text(
                text = "🎉",
                fontSize = 20.sp,
                modifier = Modifier
                    .offset(
                        x = (50 + index * 30).dp,
                        y = (100 + index * 50).dp
                    )
                    .graphicsLayer {
                        rotationZ = particleOffset
                        alpha = 0.8f
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = petEmoji,
                fontSize = 120.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .scale(1f + celebration * 0.1f)
            )

            Text(
                text = "Awesome Choice!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00b894),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "So you love ${petName}s! 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "They're absolutely wonderful companions!",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
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
    val petEmoji = when(selectedPetType) {
        PetType.DOG -> "🐕"
        PetType.CAT -> "🐱"
        PetType.RABBIT -> "🐰"
        PetType.BIRD -> "🐦"
        PetType.HAMSTER -> "🐹"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "final")
    val finalGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFa29bfe).copy(alpha = 0.3f),
                        Color(0xFF6c5ce7).copy(alpha = 0.4f),
                        Color(0xFFfd79a8).copy(alpha = 0.3f)
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
                text = petEmoji,
                fontSize = 80.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .scale(finalGlow)
            )

            Text(
                text = "One Last Thing...",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Every special companion needs a special name. What would you like to call your new friend?",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = petName,
                onValueChange = onPetNameChange,
                label = { Text("Pet Name") },
                placeholder = { Text("Choose a wonderful name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFF6c5ce7),
                    focusedLabelColor = Color(0xFF6c5ce7)
                ),
                singleLine = true
            )

            AnimatedVisibility(
                visible = petName.isNotBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6c5ce7)
                    )
                ) {
                    Text(
                        text = "Start Our Adventure Together! 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    OnboardingStoryFlow()
}
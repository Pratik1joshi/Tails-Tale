package com.example.tailstale.view.pages
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.tailstale.OverlayIconPainter
import com.example.tailstale.R
import com.example.tailstale.StatusBar
import com.example.tailstale.VideoPlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    var health by remember { mutableStateOf(85) }
    var hunger by remember { mutableStateOf(40) }
    var happiness by remember { mutableStateOf(70) }
    var showPlayVideo by remember { mutableStateOf(false) }

    val cooldownMillis = 90_000L
    var lastClickTime by remember { mutableStateOf(0L) }
    val currentTime = System.currentTimeMillis()

    val isClickable = currentTime - lastClickTime > cooldownMillis
// Video state
    var selectedVideoRes by remember { mutableStateOf(R.raw.sitting) }
    var isLooping by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())

    ) {
        // Pet info header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Buddy",
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
                        "Puppy",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                "3 months old",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status bars
        StatusBar("Health", health, Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(8.dp))
        StatusBar("Hunger", hunger, Color(0xFFFF5722))
        Spacer(modifier = Modifier.height(8.dp))
        StatusBar("Happiness", happiness, Color(0xFF2196F3))

        Spacer(modifier = Modifier.height(24.dp))

        // Video with overlay icons


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Video as background
            VideoPlayerView(
                modifier = Modifier.matchParentSize(),
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
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_bed_24),
                    onClick = {
                        selectedVideoRes = R.raw.pupsleeping
                        isLooping = false
                        coroutineScope.launch {
                            delay(60_000) // 1 minute
                            selectedVideoRes = R.raw.sitting
                            isLooping = true
                        }
                    },
                    modifier = Modifier.alpha(0.8f)
                )
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_wash_24),
                    onClick = { selectedVideoRes = R.raw.pupbathing },
                    modifier = Modifier.alpha(0.8f)
                )
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_chair_alt_24),
                    onClick = { selectedVideoRes = R.raw.pupsitting },
                    modifier = Modifier.alpha(0.8f)
                )
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_bathtub_24),
                    onClick = { selectedVideoRes = R.raw.pupbathing },
                    modifier = Modifier.alpha(0.8f)
                )

            }


            // Overlay icons on the right side
            // Right-side icons
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_directions_walk_24),
                    onClick = {
                        happiness = minOf(100, happiness + 10)
                        hunger = maxOf(0, hunger - 5)
                    },
                    modifier = Modifier.alpha(0.8f)
                )
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_restaurant_24),
                    onClick = {
                        if (isClickable) {
                            hunger = minOf(100, hunger + 20)
                            health = minOf(100, health + 5)
                            lastClickTime = System.currentTimeMillis()
                        }
                    },
                    modifier = Modifier.alpha(if (isClickable) 1f else 0.5f)
                )
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.baseline_sports_basketball_24),
                    onClick = {
                        happiness = minOf(100, happiness + 5)
                        hunger = maxOf(0, hunger - 3)
                    }
                )
                OverlayIconPainter(
                    painter = painterResource(id = R.drawable.outline_local_hospital_24),
                    onClick = {
                        health = minOf(100, health + 20)
                    }
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
                            "• Regular exercise keeps your pet healthy",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}








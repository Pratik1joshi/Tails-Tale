package com.example.tailstale.view.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.viewmodel.PetViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val petViewModel: PetViewModel = viewModel(
        factory = com.example.tailstale.viewmodel.PetViewModelFactory(
            com.example.tailstale.repo.PetRepositoryImpl(),
            com.example.tailstale.repo.UserRepositoryImpl()
        )
    )

    var activities by remember { mutableStateOf<List<com.example.tailstale.model.ActivityRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    val coroutineScope = rememberCoroutineScope()

    // Load activities when screen loads
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            loading = true
            coroutineScope.launch {
                try {
                    val result = petViewModel.getRecentActivities(userId, 100)
                    result.fold(
                        onSuccess = { activityList ->
                            activities = activityList
                            error = null
                        },
                        onFailure = { exception ->
                            error = "Failed to load activities: ${exception.message}"
                        }
                    )
                } catch (e: Exception) {
                    error = "Error loading activities: ${e.message}"
                } finally {
                    loading = false
                }
            }
        }
    }

    // Filter activities based on selected filter
    val filteredActivities = when (selectedFilter) {
        "Feeding" -> activities.filter { it.activityType == com.example.tailstale.model.ActivityType.FEEDING }
        "Playing" -> activities.filter { it.activityType == com.example.tailstale.model.ActivityType.PLAYING }
        "Cleaning" -> activities.filter { it.activityType == com.example.tailstale.model.ActivityType.CLEANING }
        "Sleeping" -> activities.filter { it.activityType == com.example.tailstale.model.ActivityType.SLEEPING }
        "Walking" -> activities.filter { it.activityType == com.example.tailstale.model.ActivityType.WALKING }
        "Sitting" -> activities.filter { it.activityType == com.example.tailstale.model.ActivityType.SITTING }
        else -> activities
    }

    // Group activities by date
    val groupedActivities = filteredActivities.groupBy { activity ->
        activity.getFormattedDate()
    }.toSortedMap(compareByDescending { it })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Header
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
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF007AFF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Pet Activities",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    if (activities.isEmpty()) "No activities recorded yet" else "${activities.size} activities recorded",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter buttons - IMPROVED: Better layout for multiple filters
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Filter Activities",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                // First row: Most common filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val primaryFilters = listOf("All", "Feeding", "Playing", "Walking")
                    primaryFilters.forEach { filter ->
                        FilterChip(
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            selected = selectedFilter == filter,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Second row: Additional filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val secondaryFilters = listOf("Cleaning", "Sleeping", "Sitting")
                    secondaryFilters.forEach { filter ->
                        FilterChip(
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            selected = selectedFilter == filter,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add spacer to balance the row since we only have 3 items
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading state
        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error state
        error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5722))
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Activities list
        if (filteredActivities.isEmpty() && !loading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No activities found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        "Start interacting with your pets to see activities here!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Activities grouped by date
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedActivities.forEach { (date, dayActivities) ->
                    item {
                        // Date header
                        Text(
                            text = date,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(dayActivities) { activity ->
                        ActivityCard(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: com.example.tailstale.model.ActivityRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity emoji
            Text(
                text = activity.activityType.emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Activity details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${activity.petName} ${activity.activityType.description.lowercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = activity.getFormattedTime(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Show stats changes if any
                if (activity.statsChanged.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activity.statsChanged.forEach { (stat, change) ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (change > 0) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFFF5722).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${stat.replaceFirstChar { it.uppercase() }}: ${if (change > 0) "+" else ""}$change",
                                    fontSize = 10.sp,
                                    color = if (change > 0) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Duration
            if (activity.duration > 0) {
                Text(
                    text = activity.getDurationText(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

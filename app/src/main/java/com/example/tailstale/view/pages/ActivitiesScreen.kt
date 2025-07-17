package com.example.tailstale.view.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

data class ActivityRecord(val activity: String, val date: Date)

@Composable
fun ActivitiesScreen() {
    val activities = listOf(
        ActivityRecord("Playing", Date()),
        ActivityRecord("Sleeping", Date(System.currentTimeMillis() - 3600000L)),
        ActivityRecord("Sitting", Date(System.currentTimeMillis() - 7200000L))
    )
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Build,//baseline_directions_run_24
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Activities",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                "Track your pet's daily activities and achievements",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Activity Records", fontWeight = FontWeight.Bold)
                activities.forEach {
                    Text("- ${it.activity}: ${dateFormat.format(it.date)}")
                }
            }
        }
    }
}

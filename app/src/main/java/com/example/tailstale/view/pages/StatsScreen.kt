package com.example.tailstale.view.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape

data class VaccinationRecord(val name: String, val date: Date)
data class FeedingRecord(val date: Date)
data class BathingRecord(val date: Date)

@Composable
fun StatsScreen() {
    // Get colors from Material Theme to support dark mode
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor), // Dynamic background color
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,//baseline_bar_chart_24
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = onBackgroundColor // Dynamic icon color
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Stats Screen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = onBackgroundColor // Dynamic text color
            )
            val vaccinations = listOf(
                VaccinationRecord("Rabies", Date()),
                VaccinationRecord("Distemper", Date())
            )
            val feedings = listOf(
                FeedingRecord(Date()),
                FeedingRecord(Date(System.currentTimeMillis() - 86400000L))
            )
            val bathings = listOf(
                BathingRecord(Date(System.currentTimeMillis() - 172800000L))
            )

            val dateFormat =
                remember { SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Vaccination Records",
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor // Dynamic text color
                )
                vaccinations.forEach {
                    Text(
                        "- ${it.name}: ${dateFormat.format(it.date)}",
                        color = onBackgroundColor // Dynamic text color
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Feeding Records",
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor // Dynamic text color
                )
                feedings.forEachIndexed { idx, it ->
                    Text(
                        "- Feeding #${idx + 1}: ${dateFormat.format(it.date)}",
                        color = onBackgroundColor // Dynamic text color
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Bathing Records",
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor // Dynamic text color
                )
                bathings.forEachIndexed { idx, it ->
                    Text(
                        "- Bath #${idx + 1}: ${dateFormat.format(it.date)}",
                        color = onBackgroundColor // Dynamic text color
                    )
                }
            }
        }
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color) {
    // Get theme colors for dark mode support
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = onBackgroundColor // Dynamic text color
            )
            Text(
                "$value%",
                fontSize = 14.sp,
                color = onBackgroundColor.copy(alpha = 0.7f) // Slightly transparent for secondary text
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    surfaceVariantColor.copy(alpha = 0.3f),
                    RoundedCornerShape(6.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .height(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
        }
    }
}
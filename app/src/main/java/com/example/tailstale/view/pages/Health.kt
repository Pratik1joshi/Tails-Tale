package com.example.tailstale.view.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tailstale.view.ActivityItem
import com.example.tailstale.view.AppointmentItem
import com.example.tailstale.view.DetailedHealthStat
import com.example.tailstale.view.PageHeader

@Composable
fun Health() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader("Health Dashboard")
        }

        item {
            // Detailed Health Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Buddy's Health Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DetailedHealthStat("Overall Health", 85, Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailedHealthStat("Energy Level", 70, Color(0xFF2196F3))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailedHealthStat("Appetite", 40, Color(0xFFF44336))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailedHealthStat("Mood", 90, Color(0xFF9C27B0))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Recent Activities",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ActivityItem("Morning Walk", "30 minutes", "🚶")
                    ActivityItem("Playtime", "45 minutes", "🎾")
                    ActivityItem("Training Session", "20 minutes", "🎓")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Upcoming Appointments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AppointmentItem("Vaccination", "June 15, 2025", Icons.Default.Info)
                    AppointmentItem("Grooming", "June 20, 2025", Icons.Default.Star)
                }
            }
        }
    }
}
@Composable
@Preview
fun HealthPagePreview() {
    Health()
}
//
//@Composable
//fun Health() {
//    Column (
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color.White)
//    ){
//        Text("Yooooooooooo" )
//    }
//}
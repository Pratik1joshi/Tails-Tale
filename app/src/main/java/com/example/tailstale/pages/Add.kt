package com.example.tailstale.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tailstale.view.AddActionCard
import com.example.tailstale.view.PageHeader


@Composable
fun Add() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PageHeader("Add New Entry")

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AddActionCard(
                    title = "Log Meal",
                    description = "Record feeding time and food type",
                    icon = Icons.Default.Place,
                    color = Color(0xFFFF8C42)
                )
            }

            item {
                AddActionCard(
                    title = "Record Activity",
                    description = "Track walks, playtime, and exercise",
                    icon = Icons.Default.Star,
                    color = Color(0xFF2196F3)
                )
            }

            item {
                AddActionCard(
                    title = "Health Note",
                    description = "Add health observations or symptoms",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF4CAF50)
                )
            }

            item {
                AddActionCard(
                    title = "Take Photo",
                    description = "Capture a moment with your pet",
                    icon = Icons.Default.AccountBox,
                    color = Color(0xFF9C27B0)
                )
            }

            item {
                AddActionCard(
                    title = "Weight Check",
                    description = "Log current weight measurement",
                    icon = Icons.Default.AccountCircle,
                    color = Color(0xFFFF5722)
                )
            }
        }
    }
}

//
//@Composable
//fun Add() {
//    Column (
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color.White)
//    ){
//        Text("Ram" )
//    }
//}
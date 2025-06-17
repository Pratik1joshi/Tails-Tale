package com.example.tailstale.view.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tailstale.view.FoodEntry
import com.example.tailstale.view.NutritionCard
import com.example.tailstale.view.PageHeader
import com.example.tailstale.view.ScheduleItem

//
//@Composable
//fun Foods() {
//    Column (
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color.White)
//    ){
//        Text("Yooooooooooo" )
//    }
//}


@Composable
fun Foods() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader("Food & Nutrition")
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
                        text = "Today's Meals",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FoodEntry("Breakfast", "7:00 AM", "Puppy kibble - 200g", true)
                    FoodEntry("Lunch", "12:30 PM", "Wet food - 150g", true)
                    FoodEntry("Dinner", "6:00 PM", "Puppy kibble - 200g", false)
                    FoodEntry("Treats", "Throughout day", "Training treats", false)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NutritionCard(
                    modifier = Modifier.weight(1f),
                    title = "Daily Calories",
                    value = "850",
                    target = "1000",
                    unit = "kcal"
                )
                NutritionCard(
                    modifier = Modifier.weight(1f),
                    title = "Water Intake",
                    value = "400",
                    target = "500",
                    unit = "ml"
                )
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
                        text = "Feeding Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ScheduleItem("Breakfast", "7:00 AM", "Puppy kibble")
                    ScheduleItem("Lunch", "12:30 PM", "Wet food")
                    ScheduleItem("Dinner", "6:00 PM", "Puppy kibble")
                }
            }
        }
    }
}

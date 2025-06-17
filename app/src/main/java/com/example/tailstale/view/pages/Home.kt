package com.example.tailstale.pages

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import com.example.tailstale.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tailstale.view.AppHeader
import com.example.tailstale.view.HealthStatsSection
import com.example.tailstale.view.PetImageSection
import com.example.tailstale.view.PetInfoSection
import com.example.tailstale.view.QuickActionsSection

class HomeActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Home()
        }
    }
}

//
//@OptIn(ExperimentalMaterial3Api::class)
//
//
//@Composable
//fun BottomNavigation() {
//    Surface(
//        color = Color.White,
//        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//        shadowElevation = 8.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//// home health
//            BottomNavItem(Icons.Default.Home, "Home", true)
//            BottomNavItem(Icons.Default.Favorite, "Health", false)
//
//// Plus button
//            FloatingActionButton(
//                onClick = { },
//                containerColor = Color(0xFF00BCD4),
//                modifier = Modifier.size(48.dp)
//            ) {
//                Icon(
//                    Icons.Default.Add,
//                    contentDescription = "Add",
//                    tint = Color.White
//                )
//            }
//
//// food person
//            BottomNavItem(Icons.Default.Home, "Food", false)
//            BottomNavItem(Icons.Default.Person, "Profile", false)
//        }
//    }
//}
//// bottom navigation item
//@Composable
//fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.padding(horizontal = 8.dp)
//    ) {
//
//        Icon(
//            imageVector = icon,
//            contentDescription = label,
//            tint = if (isSelected) Color(0xFFFF8C42) else Color.Gray,
//            modifier = Modifier.size(24.dp)
//        )
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = label,
//            fontSize = 12.sp,
//            color = if (isSelected) Color(0xFFFF8C42) else Color.Gray
//        )
//    }
//}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun Home() {
//    Column (
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color.White)
//    ){
//        // TopAppBar
//        val topAppBarState = rememberTopAppBarState()
//        Scaffold(
//            topBar = {
//                androidx.compose.material3.TopAppBar(
//                    title = { Image(
//                        painter = painterResource(R.drawable.logo),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//
//                            .height(100.dp)
//                            .width(100.dp)
//                            .offset(x=-22.dp, y= -12.dp)
//
//                    )},
//                    navigationIcon = {
//                        {
////                            Icon(Icons.Default.ArrowBack,contentDescription = null)
//                        }
//                    },
//                    actions = {
//                        IconButton(onClick = {
//                            // Handle notification action
//                        }) {
//                            Icon(Icons.Default.Notifications,contentDescription = null)
//                        }
//
//                        IconButton(onClick = {
//                            //handles settings action
//                        }) {
//                            Icon(Icons.Default.Settings,contentDescription = null)
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .height(56.dp),
//
//
//
//
//                )
//            }
//        ) { innerPadding ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//
//
//            }
//        }
//
//
//
//
//
//    }
//}


@Composable
fun Home() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        AppHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // Pet Info
        PetInfoSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Health Stats
        HealthStatsSection()

        Spacer(modifier = Modifier.height(32.dp))

        // Pet Image
        PetImageSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        QuickActionsSection()
    }
}
@Preview(showBackground = true)
@Composable
fun HomePreview() {
    Home()
}
package com.example.tailstale.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tailstale.MainActivity
import com.example.tailstale.R
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val sharedPreferences = context.getSharedPreferences(
        "User",
        Context.MODE_PRIVATE
    )

    val editor = sharedPreferences.edit()

    val localEmail : String = sharedPreferences.getString("email","").toString()

    LaunchedEffect(Unit) {
        delay(2000)

        if(localEmail.isEmpty()){
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
            activity.finish()
        }else{
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            activity.finish()
        }


    }

    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun PrevSplash() {
    SplashBody()
}
package com.example.tailstale.view


import android.app.Activity
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.R
import com.example.tailstale.di.AppModule
import com.example.tailstale.viewmodel.AuthViewModel
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
    val activity = context as? Activity
    val authViewModel: AuthViewModel = viewModel(factory = AppModule.provideViewModelFactory())

    // Observe authentication state with proper delegation
    val currentUser by authViewModel.currentUser.collectAsState()
    val loading by authViewModel.loading.collectAsState()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()

    // Track initialization state
    var hasCheckedAuth by remember { mutableStateOf(false) }

    // Check authentication when splash loads
    LaunchedEffect(Unit) {
        println("DEBUG: SplashActivity - Starting authentication check")
        authViewModel.enableAutoLogin()
        hasCheckedAuth = true
    }

    // Handle navigation based on auth state - only after we've checked
    LaunchedEffect(hasCheckedAuth, loading, isSignedIn, currentUser) {
        if (hasCheckedAuth && !loading) {
            println("DEBUG: SplashActivity - Auth check complete")
            println("DEBUG: isSignedIn = $isSignedIn")
            println("DEBUG: currentUser = ${currentUser?.displayName}")

            delay(1000) // Show splash for 1 second minimum

            if (isSignedIn && currentUser != null) {
                println("DEBUG: SplashActivity - Navigating to MainActivity")
                // User is authenticated, go to MainActivity
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                activity?.finish()
            } else {
                println("DEBUG: SplashActivity - Navigating to OnboardingActivity")
                // User is not authenticated, go to onboarding
                val intent = Intent(context, OnboardingActivity::class.java)
                context.startActivity(intent)
                activity?.finish()
            }
        }
    }

    Column(
        modifier = Modifier
            .padding()
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

        if (loading) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Checking authentication...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Debug information (remove in production)
        if (hasCheckedAuth) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Auth: ${if (isSignedIn) "Signed In" else "Not Signed In"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

}

@Preview
@Composable
fun PrevSplash() {
    SplashBody()
}
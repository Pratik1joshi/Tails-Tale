package com.example.tailstale.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.R
import com.example.tailstale.di.AppModule
import com.example.tailstale.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tailstale.view.MainActivity


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBody() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val fromOnboarding = activity?.intent?.getBooleanExtra("FROM_ONBOARDING", false) ?: false
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    val authViewModel: AuthViewModel = viewModel(factory = AppModule.provideViewModelFactory())

    LaunchedEffect(Unit) {
        authViewModel.disableAutoLogin()
    }

    val loading by authViewModel.loading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()

    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                val credential = GoogleAuthProvider.getCredential(token, null)
                authViewModel.signInWithGoogle(credential)
            }
        } catch (e: ApiException) {
            authViewModel.setError("Google sign in failed: ${e.message}")
        }
    }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearError()
        }
    }

    fun autoScroll(targetPosition: Int) {
        coroutineScope.launch {
            delay(100)
            scrollState.animateScrollTo(targetPosition)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = Color.White)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Welcome Back!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field with testTag
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email") // ADDED
                    .focusRequester(emailFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) autoScroll(400)
                    },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Email", tint = Color.Gray)
                },
                label = { Text("Email") },
                placeholder = { Text("abc@gmail.com") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray.copy(0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field with testTag
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password") // ADDED
                    .focusRequester(passwordFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) autoScroll(500)
                    },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.Gray)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            painter = painterResource(
                                if (passwordVisibility)
                                    R.drawable.baseline_visibility_24
                                else
                                    R.drawable.baseline_visibility_off_24
                            ),
                            contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                            tint = Color.Gray
                        )
                    }
                },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray.copy(0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = Color.White,
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text("Remember me", color = Color.Gray, fontSize = 14.sp)
                }
                Text(
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        showForgotPasswordDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button with testTag
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        authViewModel.setError("Email and password cannot be empty")
                    } else {
                        authViewModel.signInWithEmail(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_button"), // ADDED
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, OnboardingActivity::class.java)
                        context.startActivity(intent)
                        if (fromOnboarding) {
                            activity?.finish()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(Modifier.weight(1f), color = Color.Gray.copy(0.3f))
                Text("  Or continue with  ", color = Color.Gray, fontSize = 14.sp)
                Divider(Modifier.weight(1f), color = Color.Gray.copy(0.3f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Logins (no testTags needed for now)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Google, Facebook, GitHub login cards...
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Forgot Password Dialog
            if (showForgotPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showForgotPasswordDialog = false },
                    title = {
                        Text("Reset Password", fontWeight = FontWeight.SemiBold)
                    },
                    text = {
                        Column {
                            Text(
                                "Enter your email address and we'll send you a link to reset your password.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it },
                                label = { Text("Email") },
                                placeholder = { Text("Enter your email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (resetEmail.isNotBlank()) {
                                authViewModel.resetPassword(resetEmail)
                                showForgotPasswordDialog = false
                                resetEmail = ""
                            } else {
                                authViewModel.setError("Please enter your email address")
                            }
                        }) {
                            Text("Send Reset Link")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showForgotPasswordDialog = false
                            resetEmail = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
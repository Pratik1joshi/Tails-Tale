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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tailstale.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailstale.di.AppModule
import com.example.tailstale.model.PetType
import com.example.tailstale.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.delay

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignupBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupBody() {
    // Get data from onboarding
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val intent = activity?.intent

    val prefilledName = intent?.getStringExtra("USER_NAME") ?: ""
    val selectedPetType = intent?.getStringExtra("PET_TYPE")?.let {
        PetType.valueOf(it)
    }
    val selectedPetName = intent?.getStringExtra("PET_NAME") ?: ""

    var name by remember { mutableStateOf(prefilledName) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Get view model
    val authViewModel: AuthViewModel = viewModel(factory = AppModule.provideViewModelFactory())

    // Observe state
    val loading by authViewModel.loading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()

    // Setup Google Sign In
    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    // Handle sign in result
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

    // Navigate to main activity if signed in
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    }

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Section
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp) // Reduced height to save space
                    .width(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                placeholder = { Text("Full Name") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                placeholder = { Text("abc@gmail.com") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                placeholder = { Text("Password") },
                visualTransformation = if (passwordVisibility)
                    VisualTransformation.None else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            if (!passwordVisibility) R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ),
                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                        modifier = Modifier.clickable { passwordVisibility = !passwordVisibility }
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                placeholder = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisibility)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            if (!confirmPasswordVisibility) R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ),
                        contentDescription = if (confirmPasswordVisibility) "Hide password" else "Show password",
                        modifier = Modifier.clickable {
                            confirmPasswordVisibility = !confirmPasswordVisibility
                        }
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms and Conditions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Checkbox(
                    checked = terms,
                    onCheckedChange = { terms = it },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = Color.White,
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    "I agree to the Terms and Conditions",
                    modifier = Modifier.clickable { showTermsDialog = true },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Up Button
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        authViewModel.setError("All fields must be filled")
                    } else if (password != confirmPassword) {
                        authViewModel.setError("Passwords do not match")
                    } else if (!terms) {
                        authViewModel.setError("You must accept the terms and conditions")
                    } else {
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        // Pass onboarding data to signup
                        authViewModel.signUpWithCompleteData(
                            email = email,
                            password = password,
                            displayName = name,
                            petType = selectedPetType?.name ?: "",
                            petName = selectedPetName
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(56.dp),
                enabled = !loading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign Up", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In Link
            Text(
                "Already have an account? Sign In Now",
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable {
                        if (prefilledName.isNotEmpty() || selectedPetType != null) {
                            // Coming from onboarding, go to login with flag
                            val loginIntent = Intent(context, LoginActivity::class.java).apply {
                                putExtra("FROM_ONBOARDING", true)
                            }
                            context.startActivity(loginIntent)
                        } else {
                            // Regular signup flow
                            val loginIntent = Intent(context, LoginActivity::class.java)
                            context.startActivity(loginIntent)
                        }
                        activity?.finish()
                    },
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    "  Use other Methods  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Social Login Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Google Sign In
                Card(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = "Sign up with Google",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Facebook Sign In
                Card(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            // Implement Facebook login here
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.facebook),
                            contentDescription = "Sign up with Facebook",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // GitHub Sign In
                Card(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            val provider = OAuthProvider.newBuilder("github.com")
                            provider.scopes = listOf("user:email")

                            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                            auth.startActivityForSignInWithProvider(context as ComponentActivity, provider.build())
                                .addOnSuccessListener { authResult ->
                                    val credential = authResult.credential
                                    credential?.let {
                                        authViewModel.signInWithGithub(it)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    authViewModel.setError("GitHub sign in failed: ${e.message}")
                                }
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.github),
                            contentDescription = "Sign up with GitHub",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Extra spacing at bottom for better scrolling
            Spacer(modifier = Modifier.height(32.dp))

            // Terms and Conditions Dialog
            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    title = { Text("Terms and Conditions") },
                    text = {
                        Text(
                            "By accepting these Terms and Conditions, you agree to use this application responsibly. " +
                                    "Your personal data will be handled according to our privacy policy. " +
                                    "This is a pet simulator application meant for entertainment purposes only."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                terms = true
                                showTermsDialog = false
                            }
                        ) {
                            Text("Agree")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showTermsDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    SignupBody()
}
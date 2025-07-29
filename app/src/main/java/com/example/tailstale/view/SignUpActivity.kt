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
import androidx.compose.ui.platform.testTag
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
    // Context and onboarding data
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val intent = activity?.intent
    val prefilledName = intent?.getStringExtra("USER_NAME") ?: ""
    val selectedPetType = intent?.getStringExtra("PET_TYPE")?.let {
        PetType.valueOf(it)
    }
    val selectedPetName = intent?.getStringExtra("PET_NAME") ?: ""

    // State
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
        error?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("signup_name"), // ✅ Added
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                placeholder = { Text("Full Name") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Down)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("signup_email"), // ✅ Added
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null) },
                placeholder = { Text("abc@gmail.com") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Down)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("signup_password"), // ✅ Added
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                placeholder = { Text("Password") },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Down)
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            if (passwordVisibility) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                        ),
                        contentDescription = "Toggle Password",
                        modifier = Modifier.clickable { passwordVisibility = !passwordVisibility }
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("signup_confirm_password"), // ✅ Added
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                placeholder = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            if (confirmPasswordVisibility) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                        ),
                        contentDescription = "Toggle Confirm Password",
                        modifier = Modifier.clickable { confirmPasswordVisibility = !confirmPasswordVisibility }
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.1f),
                    unfocusedContainerColor = Color.Gray.copy(0.05f)
                ),
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
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                            authViewModel.setError("All fields must be filled")
                        password != confirmPassword ->
                            authViewModel.setError("Passwords do not match")
                        !terms ->
                            authViewModel.setError("You must accept the terms and conditions")
                        else -> {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            authViewModel.signUpWithCompleteData(
                                email = email,
                                password = password,
                                displayName = name,
                                petType = selectedPetType?.name ?: "",
                                petName = selectedPetName
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(56.dp)
                    .testTag("signup_button"), // ✅ Added
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

            // Remaining social login & dialog code can stay unchanged
            // ...
        }
    }
}
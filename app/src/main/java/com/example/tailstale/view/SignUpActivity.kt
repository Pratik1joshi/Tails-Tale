package com.example.tailstale.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tailstale.R

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignupBody(onReturnToLogin = { finish() })
        }
    }
}

@Composable
fun SignupBody(onReturnToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(16.dp),
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
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            placeholder = { Text("Full Name") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
            placeholder = { Text("abc@gmail.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            ),
            suffix = {
                Icon(
                    painter = painterResource(
                        if (!passwordVisibility) R.drawable.baseline_visibility_off_24
                        else R.drawable.baseline_visibility_24
                    ),
                    contentDescription = null,
                    modifier = Modifier.clickable { passwordVisibility = !passwordVisibility }
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            placeholder = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            ),
            suffix = {
                Icon(
                    painter = painterResource(
                        if (!confirmPasswordVisibility) R.drawable.baseline_visibility_off_24
                        else R.drawable.baseline_visibility_24
                    ),
                    contentDescription = null,
                    modifier = Modifier.clickable { confirmPasswordVisibility = !confirmPasswordVisibility }
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Signup Success", Toast.LENGTH_SHORT).show()
                    // You can add navigation to another screen here
                }
            },
            modifier = Modifier.width(200.dp)
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onReturnToLogin() },
            modifier = Modifier.width(200.dp)
        ) {
            Text("Return to Login")

        }
    }
}
@Composable
@Preview(showBackground = true)
fun SignupPreview() {
    SignupBody(onReturnToLogin = {})
}
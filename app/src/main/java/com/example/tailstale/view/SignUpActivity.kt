package com.example.tailstale.view

import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            SignupBody()
        }
    }
}

@Composable
fun SignupBody() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var terms by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

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
                .height(250.dp)
                .width(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Person,
                contentDescription = null) },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),

            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Email,
                contentDescription = null) },
            placeholder = { Text("abc@gmail.com") },
            keyboardOptions = KeyboardOptions
                (keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Lock,
                contentDescription = null) },
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisibility)
                VisualTransformation.None else
                    PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions
                (keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            ),
            suffix = {
                Icon(
                    painter = painterResource(
                        if (!passwordVisibility)
                            R.drawable.baseline_visibility_off_24
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(12.dp),
            prefix = { Icon(imageVector = Icons.Default.Lock,
                contentDescription = null) },
            placeholder = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisibility)
                VisualTransformation.None

            else

                PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions
                (keyboardType = KeyboardType.Password),

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(0.2f),
                unfocusedContainerColor = Color.Gray.copy(0.5f)
            ),
            suffix = {
                Icon(
                    painter = painterResource(
                        if (!confirmPasswordVisibility)
                            R.drawable.baseline_visibility_off_24

                        else
                            R.drawable.baseline_visibility_24

                    ),
                    contentDescription = null,
                    modifier = Modifier.clickable
                    {
                        confirmPasswordVisibility = !confirmPasswordVisibility
                    }
                )
            }
        )
        Spacer(modifier = Modifier
            .height(16.dp))
//        Button(
//            onClick = {
//                if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
//                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                } else if (password != confirmPassword) {
//                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(context, "Signup Success", Toast.LENGTH_SHORT).show()
//                    // You can add navigation to another screen here
//                }
//            },
//            modifier = Modifier.width(200.dp)
//        ) {
//            Text("Sign Up")
//        }
        Button(
            onClick = {
//                    if (email == "ram@gmail.com" && password == "123456") {
//                        Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
//
//                    } else {
//                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                    }
            },
            modifier = Modifier.width(200.dp)
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier
            .height(16.dp))
        Row {
//            Checkbox(
//                checked = terms,
//                onCheckedChange = { terms = it },
//                colors = CheckboxDefaults.colors(
//                    checkmarkColor = Color.Green,
//                    checkedColor = Color.White
//                ),
//                modifier = Modifier
//                    .padding(vertical = 10.dp)
//            )
            Text("I agree to the Terms and Conditions",
                modifier = Modifier
                    .padding( vertical = 25.dp)
                    .clickable {
                        showDialog = true

                },
            )
            if (showDialog){
                // Show Terms and Conditions dialog
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Terms and Conditions") },
                    text = { Text("We are noobs. ") },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Agree")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Disagree")
                        }
                    }
                )
            }
        }

        Text("Already had an account? SignIn Now",
            modifier = Modifier
                .padding(vertical = 15.dp)
                .clickable {
                    val intent = Intent(
                        context, LoginActivity::class.java
                    )
                    context.startActivity(intent)
                },
        )
//        Button(
//            onClick = { onReturnToLogin() },
//            modifier = Modifier.width(200.dp)
//        ) {
//            Text("Return to Login")
//
//        }
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(
                modifier = Modifier
                    .height(1.dp)
                    .width(100.dp)
                    .background(color = Color.Black)
            ) { }
            Text("Use other Methods")
            Row(
                modifier = Modifier
                    .height(1.dp)
                    .width(100.dp)
                    .background(color = Color.Black)
            ) { }
        }
        Row(
            modifier = Modifier
                .width(150.dp)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Image(
                painter = painterResource(R.drawable.google),
                contentDescription = null, modifier = Modifier
                    .height(35.dp)
                    .width(35.dp)
                    .clip(
                        RoundedCornerShape(100.dp)
                    )
            )
            Image(
                painter = painterResource(R.drawable.facebook),
                contentDescription = null, modifier = Modifier
                    .height(40.dp)
                    .width(40.dp)
                    .clip(
                        RoundedCornerShape(100.dp)
                    )
            )
            Image(
                painter = painterResource(R.drawable.github),
                contentDescription = null, modifier = Modifier
                    .height(30.dp)
                    .width(30.dp)
                    .clip(
                        RoundedCornerShape(100.dp)
                    )
            )
        }
    }
}
@Composable
@Preview(showBackground = true)
fun SignupPreview() {
    SignupBody()
}
package com.example.tailstale

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.tailstale.ui.theme.TailsTaleTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@Composable
fun LoginBody(){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var context = LocalContext.current
    val snackbarHostState = remember {SnackbarHostState()}
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = Color.White), horizontalAlignment = Alignment.CenterHorizontally
        ) {

//            Button(onClick = { showDialog = true }) {
//                Text("Show AlertDialog")
//            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                    }, // dismiss when clicked outside
                    confirmButton = {
                        Button(onClick = {
                            // Confirm action
                            showDialog = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            // Cancel action
                            showDialog = false
                        }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text(text = "Alert Title") },
                    text = { Text("This is an alert dialog message.") }
                )
            }


            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .height(250.dp)
                    .width(250.dp)
            )

            OutlinedTextField(
                value = email, onValueChange = {
                    email = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(12.dp),
                prefix = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                placeholder = {
                    Text("abc@gmail.com")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.2f),
                    unfocusedContainerColor = Color.Gray.copy(0.5f)
                )
            )
            OutlinedTextField(
                value = password, onValueChange = {
                    password = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                prefix = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                suffix = {
                    Icon(
                        painter = painterResource(
                            if (!passwordVisibility) R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ), contentDescription = null,
                        modifier = Modifier.clickable {
                            passwordVisibility = !passwordVisibility
                        })
                },
                placeholder = {
                    Text("********")
                },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(0.2f),
                    unfocusedContainerColor = Color.Gray.copy(0.5f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = Color.Green,
                            checkedColor = Color.White
                        )
                    )
                    Text("Remember me")
                }
                Text("Forget Password?")
            }
            Button(
                onClick = {
                    if (email == "ram@gmail.com" && password == "123456") {
                        Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.width(200.dp)
            ) {
                Text("Login")
            }
            Text("Don't have an account? Signup Now", modifier = Modifier.padding(vertical = 10.dp))
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
                modifier = Modifier.width(150.dp).padding(vertical = 10.dp),
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
                    contentDescription = null, modifier = Modifier.height(40.dp).width(40.dp).clip(
                        RoundedCornerShape(100.dp)
                    )
                )
                Image(
                    painter = painterResource(R.drawable.github),
                    contentDescription = null, modifier = Modifier.height(30.dp).width(30.dp).clip(
                        RoundedCornerShape(100.dp)
                    )
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLogin(){
    LoginBody()
}
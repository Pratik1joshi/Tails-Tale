package com.example.tailstale.view

import android.content.Intent
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import android.os.Bundle
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tailstale.R

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold { innerPadding ->
                SignUpBody(innerPadding)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpBody(innerPadding: PaddingValues) {

    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    val countries = listOf("Nepal", "Germany", "Scotland")
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var birthday by remember { mutableStateOf("") }
    val genderOptions = listOf("Male", "Female", "Other")
    var selectedGender by remember { mutableStateOf(genderOptions[0]) }
    var termsAccepted by remember { mutableStateOf(false) }


    Column (
        modifier = Modifier
            .background(color = Color.White)
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
                .width(150.dp)
                .padding(vertical = 20.dp)
        )
        // Register Heading
        Text(
            text = "Register",
            style = TextStyle(
                fontWeight = FontWeight.Medium
            ),
            fontSize = 20.sp
        )
        Spacer(Modifier.height(20.dp))
        // First name and Last name
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ){
            TextField(
                modifier = Modifier
                    .width(180.dp)
                    .height(30.dp),
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") }
            )
            TextField(
                modifier = Modifier
                    .width(180.dp)
                    .height(30.dp),
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") }
            )
        }

        Spacer(Modifier.height(40.dp))
        // email
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(Modifier.height(40.dp))
        // country
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier

                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCountry,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                countries.forEach { countryOption ->
                    DropdownMenuItem(
                        text = { Text(countryOption) },
                        onClick = {
                            selectedCountry = countryOption
                            expanded = false
                        }
                    )
                }
            }
        }

        // birthday


        OutlinedTextField(
            value = birthday,
            onValueChange = {},
            readOnly = true,
            label = { Text("Birthday") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            // show date picker on click
            enabled = true,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Gender",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 20.dp, start = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            genderOptions.forEach { gender ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = selectedGender == gender,
                        onClick = {
                            selectedGender = gender
                        }
                    )
                    Text(text = gender)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            androidx.compose.material3.Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Text(
                text = "I agree to the Terms and Conditions",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = {  },
            enabled = termsAccepted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text(text = "Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))
        ClickableText(
            text = AnnotatedString("Already have an account? Sign in!"),
            onClick = { offset ->
                if (offset >= 25) { // "Sign in!" starts at index 25
                    context.startActivity(Intent(context, LoginActivity::class.java))
                }
            },
            style = TextStyle(
                color = Color.Blue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )



    }
}


@Preview
@Composable
fun PreviewSignup() {
    SignUpBody(PaddingValues(10.dp))
}
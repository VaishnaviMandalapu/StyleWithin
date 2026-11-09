package com.example.stylewithinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface

import androidx.compose.material3.TextButton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

import androidx.compose.foundation.layout.Row

private val Cream = Color(0xFFFAF6F3)
private val Plum = Color(0xFF704B68)
private val MutedText = Color(0xFF756B69)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                StyleWithinApp()
            }
        }
    }
}


@Composable
fun StyleWithinApp() {
    var showLogin by rememberSaveable { mutableStateOf(false) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var submitted by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val validEmail = email.matches(
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    )

    val validPassword =
        password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }

    val nameError = submitted && !showLogin && fullName.isBlank()
    val emailError = submitted && !validEmail
    val passwordError = submitted && !validPassword

    if (isLoggedIn) {
        WelcomeScreen(
            onLogout = {
                isLoggedIn = false
                showLogin = true
                fullName = ""
                email = ""
                password = ""
                submitted = false
                message = "Successfully logged out!"
            }
        )
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Cream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (showLogin) "Welcome Back" else "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Plum
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (showLogin && message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = Plum,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (showLogin)
                        "Login to your StyleWithin account."
                    else
                        "Create an account to get started.",
                    color = MutedText,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!showLogin) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            message = ""
                        },
                        label = { Text("Full name") },
                        singleLine = true,
                        isError = nameError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (nameError) {
                        ErrorText("Please enter your full name.")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = ""
                    },
                    label = { Text("Email address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (emailError) {
                    ErrorText("Please enter a valid email address.")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        message = ""
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    isError = passwordError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (passwordError) {
                    ErrorText(
                        "Use 8+ characters with uppercase, lowercase, a number and a special character."
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        submitted = true
                        message = ""

                        if (
                            validEmail &&
                            validPassword &&
                            (showLogin || fullName.isNotBlank())
                        ) {
                            if (showLogin) {
                                isLoggedIn = true
                            } else {
                                message = "Registration details look valid. Please login to continue."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Plum
                    )
                ) {
                    Text(
                        text = if (showLogin) "Login" else "Create Account",
                        fontSize = 16.sp
                    )
                }

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        color = Plum,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = {
                        showLogin = !showLogin
                        submitted = false
                        message = ""
                        password = ""
                    }
                ) {
                    Text(
                        text = if (showLogin)
                            "Don't have an account? Register"
                        else
                            "Already have an account? Login",
                        color = Plum
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onLogout: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onLogout) {
                    Text(
                        text = "↪ Logout",
                        color = Plum,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to StyleWithin!",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Plum
                )

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(
                        id = R.drawable.stylewithin_logo
                    ),
                    contentDescription = "StyleWithin butterfly logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Discover the style within you.",
                    fontSize = 16.sp,
                    color = MutedText
                )
            }
        }
    }
}


@Composable
fun ErrorText(message: String) {
    Text(
        text = message,
        color = Color(0xFF9C27B0),
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}
package com.example.proyectospersonalesyactividades_utn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (userId: Long) -> Unit,
    onNavigateToRegistration: () -> Unit // This callback navigates to registration
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State to hold the message for the user (e.g., login failed)
    var loginMessage by remember { mutableStateOf<String?>(null) } // <-- State for the message

    val loginSuccess by viewModel.loginSuccess.observeAsState()

    // Use LaunchedEffect to react to changes in loginSuccess
    LaunchedEffect(loginSuccess) {
        when (loginSuccess) {
            true -> {
                // Login successful
                val loggedInUserId = 1L // <<-- REPLACE WITH REAL USER ID FROM LOGIN
                onLoginSuccess(loggedInUserId) // Navigate on success
                loginMessage = null // Clear message on success
            }
            false -> {
                // Login failed. Display a message.
                // THIS IS WHERE THE MESSAGE FOR FAILURE IS SET
                loginMessage = "Credenciales incorrectas. Verifica tu usuario y contraseña."
                // Note: This message doesn't specifically say "user not registered"
                // because the ViewModel only returns Boolean. If you need that specificity,
                // your ViewModel/Controller need to provide more detailed failure info.
            }
            null -> {
                // Initial state or after a previous attempt that didn't set true/false
                loginMessage = null // No message initially
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(), // Hides password
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display login message if not null <-- Displaying the message
        loginMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // The Login Button <-- This button triggers the login attempt
        Button(
            onClick = {
                loginMessage = null // Clear previous message on new attempt
                if (username.isNotBlank() && password.isNotBlank()) {
                    viewModel.login(username, password) // Call the login function
                } else {
                    loginMessage = "Por favor, completa todos los campos." // Message for empty fields
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The Registration Navigation Button <-- This button guides the user to register
        TextButton(
            onClick = {
                loginMessage = null // Clear message when navigating away
                onNavigateToRegistration() // Call the navigation callback
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿No tienes cuenta? Regístrate aquí") // <-- Text suggesting registration
        }
    }
}
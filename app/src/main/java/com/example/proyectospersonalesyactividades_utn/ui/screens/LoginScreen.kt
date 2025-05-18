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
    var loginMessage by remember { mutableStateOf<String?>(null) }

    // Observe the LiveData<Long?>: null for initial/failure, non-null Long for success (the user ID)
    val loggedInUserId by viewModel.loginSuccess.observeAsState(initial = null) // Now observing Long?

    // State to track if a login attempt has been made.
    // Needed to distinguish initial null from a null result after an attempt (failure).
    var loginAttemptMade by remember { mutableStateOf(false) }


    // Use LaunchedEffect to react to changes in loggedInUserId
    LaunchedEffect(loggedInUserId) {
        when {
            // If loggedInUserId becomes non-null, login was successful
            loggedInUserId != null -> {
                val userId = loggedInUserId!! // Get the non-null userId safely
                onLoginSuccess(userId) // Pass the actual user ID to the navigation callback
                loginMessage = null // Clear message on success
                loginAttemptMade = false // Reset attempt state after success
                viewModel.resetLoginState() // Consume the success event by resetting ViewModel state

            }
            // If loggedInUserId is null...
            loggedInUserId == null && loginAttemptMade -> {
                // ... and a login attempt *was* made, it means login failed.
                loginMessage = "Credenciales incorrectas. Verifica tu usuario y contraseña."
                loginAttemptMade = false // Reset attempt state after failure
                viewModel.resetLoginState() // Consume the failure event by resetting ViewModel state
            }
            // If loggedInUserId is null and no attempt was made, it's the initial state.
            else -> { // This covers the case loggedInUserId == null && !loginAttemptMade
                loginMessage = null // No message initially or after a reset that wasn't from a failure
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
                    loginAttemptMade = true // *** Mark that an attempt is being made BEFORE calling ViewModel ***
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
                loginAttemptMade = false // *** Reset attempt state when navigating away ***
                viewModel.resetLoginState() // Reset ViewModel state on navigation away
                onNavigateToRegistration() // Call the navigation callback
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿No tienes cuenta? Regístrate aquí") // <-- Text suggesting registration
        }
    }
}
package com.example.proyectospersonalesyactividades_utn.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun RegistrationScreen(
    viewModel: MainViewModel,
    onRegistrationSuccess: () -> Unit // Callback when registration is successful
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Observe the registration success state from the ViewModel
    val registrationSuccess by viewModel.registrationSuccess.observeAsState()

    // Use LaunchedEffect to react to changes in registrationSuccess
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess == true) {
            // Registration was successful, trigger the callback
            onRegistrationSuccess()
        }
        // You might want to add logic here to handle registrationSuccess == false
        // to show an error message, for example. This would require another
        // LiveData or state in the ViewModel for error messages.
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrarse", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            // Hide the password input
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Validate input fields
                if (username.isNotBlank() && password.isNotBlank() && email.isNotBlank()) {
                    // Call the ViewModel's register function
                    viewModel.register(username, password, email)
                } else {
                    // Optionally show a message to the user if fields are empty
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Cuenta")
        }

        // You could add a Text composable here to show loading state or error messages
        // based on other states or LiveData from the ViewModel.
    }
}
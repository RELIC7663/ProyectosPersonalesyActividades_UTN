package com.example.proyectospersonalesyactividades_utn.ui.navigation

import androidx.compose.runtime.*
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import com.example.proyectospersonalesyactividades_utn.ui.screens.* // Asegúrate de que RegistrationScreen está en este package o impórtalo

/**
 * Define las posibles pantallas de la app.
 */
sealed class Screen {
    object Login : Screen()
    object Registration : Screen() // Add the Registration screen
    data class Projects(val userId: Long) : Screen()
    data class Activities(val projectId: Long, val userId: Long) : Screen()
}

/**
 * Composable que gestiona la "navegación" entre pantallas sin librerías externas.
 *
 * @param viewModel ViewModel compartido entre las pantallas.
 */
@Composable
fun AppNavHost(viewModel: MainViewModel) {
    // Estado de pantalla actual
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (val screen = currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { userId ->
                    // Al ingresar, mostrar lista de proyectos
                    viewModel.loadProjects(userId) // Make sure you have a way to get the actual userId after login
                    currentScreen = Screen.Projects(userId) // Navigate to Projects
                },
                // Add a way to navigate to Registration from Login screen
                onNavigateToRegistration = {
                    currentScreen = Screen.Registration
                }
            )
        }
        is Screen.Registration -> { // Add the case for the Registration screen
            RegistrationScreen(
                viewModel = viewModel,
                onRegistrationSuccess = {
                    // After successful registration, navigate back to Login screen
                    currentScreen = Screen.Login
                }
            )
        }
        is Screen.Projects -> {
            ProjectsScreen(
                viewModel = viewModel,
                userId = screen.userId,
                onProjectSelected = { projectId ->
                    viewModel.loadActivities(projectId)
                    currentScreen = Screen.Activities(projectId, screen.userId)
                },
                onLogout = {
                    viewModel.resetLoginState()
                    currentScreen = Screen.Login
                }
            )
        }

        is Screen.Activities -> {
            ActivitiesScreen(
                viewModel = viewModel,
                projectId = screen.projectId,
                onBack = {
                    // Volver a lista de proyectos
                    viewModel.loadProjects(screen.userId) // Reload projects when returning
                    currentScreen = Screen.Projects(screen.userId)
                }
            )
        }
    }
}
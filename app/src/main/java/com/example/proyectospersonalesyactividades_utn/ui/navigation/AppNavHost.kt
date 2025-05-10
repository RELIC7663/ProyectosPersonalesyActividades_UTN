package com.example.proyectospersonalesyactividades_utn.ui.navigation

import androidx.compose.runtime.*
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import com.example.proyectospersonalesyactividades_utn.ui.screens.*


/**
 * Define las posibles pantallas de la app.
 */
sealed class Screen {
    object Login : Screen()
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
            LoginScreen(viewModel) { userId ->
                // Al ingresar, mostrar lista de proyectos
                viewModel.loadProjects(userId)
                currentScreen = Screen.Projects(userId)
            }
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
                    viewModel.loadProjects(screen.userId)
                    currentScreen = Screen.Projects(screen.userId)
                }
            )
        }
    }
}

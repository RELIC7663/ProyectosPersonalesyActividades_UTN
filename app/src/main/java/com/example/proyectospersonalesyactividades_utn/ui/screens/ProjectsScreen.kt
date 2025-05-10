package com.example.proyectospersonalesyactividades_utn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    userId: Long,
    onProjectSelected: (projectId: Long) -> Unit,
    onLogout: () -> Unit
) {
    val projects by viewModel.projects.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Proyectos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        projects.forEach { project ->
            Text("- ${project.name}")
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { onProjectSelected(project.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Actividades de ${project.name}")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}

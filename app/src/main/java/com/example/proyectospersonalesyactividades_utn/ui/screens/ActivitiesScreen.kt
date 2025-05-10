package com.example.proyectospersonalesyactividades_utn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun ActivitiesScreen(
    viewModel: MainViewModel,
    projectId: Long,
    onBack: () -> Unit
) {
    // Observa LiveData como estado Compose
    val activitiesState = viewModel.activities.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Actividades", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (activitiesState.value.isEmpty()) {
            Text("No hay actividades registradas.")
        } else {
            activitiesState.value.forEach { activity ->
                Text("- ${activity.name}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }
    }
}

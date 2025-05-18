package com.example.proyectospersonalesyactividades_utn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import com.example.proyectospersonalesyactividades_utn.models.Activity // Ensure Activity data class is imported
import com.example.proyectospersonalesyactividades_utn.models.Project // Ensure Project data class is imported if needed for title

// Make sure your Activity and Project data classes are accessible
// data class Activity(...)
// data class Project(...)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: MainViewModel,
    projectId: Long, // The ID of the project whose activities to display
    userId: Long, // Pass userId as well, needed for potential future activity ops or back navigation state
    onBack: () -> Unit // Callback to navigate back to Projects
) {
    // Observe the list of activities from the ViewModel
    val activities by viewModel.activities.observeAsState(emptyList())

    // Observe the project details (for title) and progress
    val currentProject by viewModel.currentProject.observeAsState()
    val projectProgress by viewModel.progress.observeAsState(0f) // Observe progress, default 0f

    // State to manage dialogs and the activity being edited/deleted
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var activityToEditState by remember { mutableStateOf<Activity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var activityToDeleteId by remember { mutableStateOf<Long?>(null) }


    // LaunchedEffect to trigger loading data when the screen is first displayed
    LaunchedEffect(projectId) {
        viewModel.loadActivities(projectId) // Load activities
        viewModel.loadProjectDetails(projectId) // Load project details for title/context
        // Progress will be calculated automatically after loading activities
    }

    // LaunchedEffect to show the Edit dialog when activityToEditState is set
    LaunchedEffect(activityToEditState) {
        if (activityToEditState != null) {
            showEditDialog = true
        } else {
            showEditDialog = false // Ensure dialog is hidden when state is null
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Actividades: ${currentProject?.name ?: "Cargando..."}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Call the onBack lambda when clicked
                         Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás") // The back arrow icon
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Agregar Actividad")
            }
        },
        content = { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Add padding
            ) {

                // Display Project Progress
                Text(
                    "Avance del Proyecto: ${(projectProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LinearProgressIndicator( // Optional: Visual progress bar
                    progress = projectProgress,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )


                // List of Activities
                if (activities.isEmpty()) {
                    Box( // Use Box to center the message
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay actividades registradas para este proyecto. Toca '+' para agregar una.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn {
                        items(activities, key = { it.id }) { activity -> // Use key for better list performance
                            ActivityItem(
                                activity = activity,
                                onEditClick = {
                                    activityToEditState = it // Set the activity for editing
                                    // showEditDialog will be true via LaunchedEffect
                                },
                                onDeleteClick = {
                                    activityToDeleteId = it.id // Set the activity ID for deletion
                                    showDeleteConfirmDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    )

    // --- Dialogs ---

    // Add Activity Dialog
    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onActivityAdded = { name, description, startDate, endDate, status ->
                val newActivity = Activity(
                    projectId = projectId, // Assign to the current project ID
                    name = name,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    status = status
                    // id is 0L by default
                )
                viewModel.addActivity(newActivity) // Call ViewModel add function
                showAddDialog = false
            }
        )
    }

    // Edit Activity Dialog - Shown only if showEditDialog is true and activityToEditState is not null
    if (showEditDialog && activityToEditState != null) {
        EditActivityDialog(
            activity = activityToEditState!!, // Pass the activity object to the dialog
            onDismiss = {
                showEditDialog = false
                activityToEditState = null // Clear state
            },
            onActivityUpdated = { updatedName, updatedDescription, updatedStartDate, updatedEndDate, updatedStatus ->
                val updatedActivity = activityToEditState!!.copy( // Use copy to maintain original ID, projectId
                    name = updatedName,
                    description = updatedDescription,
                    startDate = updatedStartDate,
                    endDate = updatedEndDate,
                    status = updatedStatus
                )
                viewModel.updateActivity(updatedActivity) // Call ViewModel update function
                showEditDialog = false
                activityToEditState = null // Clear state after update
            }
        )
    }

    // Delete Confirmation Dialog for Activities
    if (showDeleteConfirmDialog && activityToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false; activityToDeleteId = null },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta actividad?") },
            confirmButton = {
                Button(
                    onClick = {
                        activityToDeleteId?.let { id ->
                            viewModel.deleteActivity(id, projectId) // Call ViewModel delete function
                        }
                        showDeleteConfirmDialog = false
                        activityToDeleteId = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false; activityToDeleteId = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- Helper Composables ---

// ActivityItem Composable with Edit/Delete icons
@Composable
fun ActivityItem(
    activity: Activity,
    onEditClick: (activity: Activity) -> Unit, // Pass the Activity object
    onDeleteClick: (activity: Activity) -> Unit // Pass the Activity object
) {
    Card(
        modifier = Modifier.fillMaxWidth()
        // TODO: Add clickable if you want to view activity details
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
            Text(activity.name, style = MaterialTheme.typography.titleMedium)
            Text(activity.description ?: "Sin descripción", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Estado: ${activity.status}", style = MaterialTheme.typography.bodySmall)
            // TODO: Display dates if needed
            // Text("Inicio: ${activity.startDate}", style = MaterialTheme.typography.bodySmall)
            // Text("Fin: ${activity.endDate}", style = MaterialTheme.typography.bodySmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Align icons to the end
            ) {
                // Edit Button
                IconButton(onClick = { onEditClick(activity) }) { // Pass the activity object
                    Icon(Icons.Filled.Edit, "Editar Actividad")
                }
                // Delete Button
                IconButton(onClick = { onDeleteClick(activity) }) { // Pass the activity object
                    Icon(Icons.Filled.Delete, "Eliminar Actividad")
                }
            }
        }
    }
}

// AddActivityDialog - Collects data and passes to callback
@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    // Callback provides collected input data
    onActivityAdded: (name: String, description: String, startDate: String, endDate: String, status: String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf(TextFieldValue("")) }
    var endDate by remember { mutableStateOf(TextFieldValue("")) }
    var status by remember { mutableStateOf("Planificado") } // Default status

    // List of possible statuses from your DB schema
    val statuses = listOf("Planificado", "En ejecución", "Realizado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Actividad") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (Opcional)") })
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Consider using Date Pickers
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Consider using Date Pickers
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown/Selector
                StatusSelector(statuses, status) { selectedStatus ->
                    status = selectedStatus
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Basic validation
                    if (name.text.isNotBlank() && startDate.text.isNotBlank() && endDate.text.isNotBlank()) {
                        onActivityAdded(name.text, description.text, startDate.text, endDate.text, status)
                    }
                    // Optionally show error message
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


// EditActivityDialog - Receives Activity data, collects updates, passes back
@Composable
fun EditActivityDialog(
    activity: Activity, // Receives the activity data
    onDismiss: () -> Unit,
    onActivityUpdated: (name: String, description: String, startDate: String, endDate: String, status: String) -> Unit
) {
    // Initialize states with activity data
    var name by remember { mutableStateOf(TextFieldValue(activity.name)) }
    var description by remember { mutableStateOf(TextFieldValue(activity.description ?: "")) }
    var startDate by remember { mutableStateOf(TextFieldValue(activity.startDate)) }
    var endDate by remember { mutableStateOf(TextFieldValue(activity.endDate)) }
    var status by remember { mutableStateOf(activity.status) } // Current status

    val statuses = listOf("Planificado", "En ejecución", "Realizado")


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Actividad") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (Opcional)") })
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Consider using Date Pickers
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Consider using Date Pickers
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown/Selector
                StatusSelector(statuses, status) { selectedStatus ->
                    status = selectedStatus
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Basic validation
                    if (name.text.isNotBlank() && startDate.text.isNotBlank() && endDate.text.isNotBlank()) {
                        onActivityUpdated(name.text, description.text, startDate.text, endDate.text, status)
                    }
                    // Optionally show error message
                }
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Simple Status Selector Composable (using DropdownMenu)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(
    statuses: List<String>,
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Estado: $selectedStatus")
            Icon(Icons.Filled.Edit, contentDescription = "Seleccionar Estado", modifier = Modifier.size(20.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
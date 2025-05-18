package com.example.proyectospersonalesyactividades_utn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.proyectospersonalesyactividades_utn.models.Project // Ensure Project data class is imported

// Make sure your Project data class is accessible
// data class Project(val id: Long = 0L, val userId: Long, val name: String, ...)


@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    userId: Long, // The ID of the currently logged-in user
    onProjectSelected: (projectId: Long) -> Unit, // This is the callback to navigate to Activities
    onLogout: () -> Unit // onLogout should handle viewModel.resetLoginState() in AppNavHost
) {
    // Observe the list of projects from the ViewModel
    val projects by viewModel.projects.observeAsState(emptyList())

    // State to manage the visibility of the Add Project dialog
    var showAddDialog by remember { mutableStateOf(false) }

    // State to manage the visibility of the Edit Project dialog
    var showEditDialog by remember { mutableStateOf(false) }

    // State to manage which project is currently being edited (holding the Project object)
    var projectToEditState by remember { mutableStateOf<Project?>(null) }

    // State to manage the visibility of the Delete Confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // State to hold the ID of the project to be deleted
    var projectToDeleteId by remember { mutableStateOf<Long?>(null) }


    // LaunchedEffect to trigger loading projects when the screen is first displayed
    LaunchedEffect(userId) {
        viewModel.loadProjects(userId)
    }

    // LaunchedEffect to show the Edit dialog when projectToEditState is set
    // This also handles clearing the state when the dialog is dismissed
    LaunchedEffect(projectToEditState) {
        if (projectToEditState != null) {
            showEditDialog = true
        } else {
            showEditDialog = false // Ensure dialog is hidden when state is null
        }
    }


    Scaffold( // Using Scaffold for better structure with a floating action button
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Agregar Proyecto")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
                    .padding(horizontal = 16.dp) // Apply horizontal padding
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp), // Add top padding
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Proyectos", style = MaterialTheme.typography.headlineMedium)
                    // Logout button - placed here or in a top app bar is common
                    Button(onClick = onLogout) {
                        Text("Cerrar Sesión")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // List of Projects
                if (projects.isEmpty()) {
                    Box( // Use Box to center the message
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay proyectos. Toca '+' para agregar uno.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn {
                        items(projects, key = { it.id }) { project -> // Use key for better list performance
                            ProjectItem(
                                project = project,
                                // Pass the navigation callback to the ProjectItem's new parameter
                                onViewActivitiesClick = { projectId ->
                                    onProjectSelected(projectId) // This calls the callback from AppNavHost
                                },
                                onEditClick = {
                                    projectToEditState = it // Set the project object for editing
                                    // showEditDialog will be true via the LaunchedEffect reacting to projectToEditState
                                },
                                onDeleteClick = {
                                    projectToDeleteId = it.id // Set the project ID for deletion
                                    showDeleteConfirmDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp)) // Space between items
                        }
                    }
                }
            }
        }
    )

    // --- Dialogs ---

    // Add Project Dialog
    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onProjectAdded = { name, description, startDate, endDate ->
                // Create a Project object using the current userId
                val newProject = Project(
                    userId = userId,
                    name = name,
                    description = description, // Assuming description is String? based on DB schema
                    startDate = startDate,
                    endDate = endDate
                    // id is 0L by default in the data class
                )
                viewModel.addProject(newProject) // Call ViewModel add function
                showAddDialog = false
            }
        )
    }

    // Edit Project Dialog - Shown only if showEditDialog is true and projectToEditState is not null
    if (showEditDialog && projectToEditState != null) {
        EditProjectDialog(
            project = projectToEditState!!, // Pass the project object to the dialog
            onDismiss = {
                showEditDialog = false
                projectToEditState = null // Clear the project being edited state when dialog is dismissed
            },
            onProjectUpdated = { updatedName, updatedDescription, updatedStartDate, updatedEndDate ->
                val updatedProject = projectToEditState!!.copy( // Use copy to maintain original ID, userId
                    name = updatedName,
                    description = updatedDescription, // Assuming description is String?
                    startDate = updatedStartDate,
                    endDate = updatedEndDate
                )
                viewModel.updateProject(updatedProject) // Call ViewModel update function
                showEditDialog = false
                projectToEditState = null // Clear the project being edited state after update
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && projectToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false; projectToDeleteId = null },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este proyecto? Se eliminarán también todas sus actividades.") },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDeleteId?.let { id ->
                            viewModel.deleteProject(id, userId) // Call ViewModel delete function
                        }
                        showDeleteConfirmDialog = false
                        projectToDeleteId = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false; projectToDeleteId = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- Helper Composables ---

// ProjectItem Composable with a View Activities button and icons for Edit/Delete
@Composable
fun ProjectItem(
    project: Project,
    onViewActivitiesClick: (projectId: Long) -> Unit, // NEW: Callback for viewing activities
    onEditClick: (project: Project) -> Unit,
    onDeleteClick: (project: Project) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            // Removed clickable from the main column
            .padding(12.dp)
        ) {
            Text(project.name, style = MaterialTheme.typography.titleMedium)
            Text(project.description ?: "Sin descripción", style = MaterialTheme.typography.bodySmall) // Show default if null
            Spacer(modifier = Modifier.height(4.dp))
            Text("Inicio: ${project.startDate}", style = MaterialTheme.typography.bodySmall)
            Text("Fin: ${project.endDate}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp)) // Space before buttons

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Distribute buttons
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to View Activities - Calls the new onViewActivitiesClick lambda
                OutlinedButton(onClick = { onViewActivitiesClick(project.id) }) {
                    Text("Ver Actividades")
                }

                Row { // Group Edit and Delete icons together
                    // Edit Button
                    IconButton(onClick = { onEditClick(project) }) {
                        Icon(Icons.Filled.Edit, "Editar")
                    }
                    // Delete Button
                    IconButton(onClick = { onDeleteClick(project) }) {
                        Icon(Icons.Filled.Delete, "Eliminar")
                    }
                }
            }
        }
    }
}

// AddProjectDialog - (Keep the same)
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onProjectAdded: (name: String, description: String, startDate: String, endDate: String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf(TextFieldValue("")) }
    var endDate by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Proyecto") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (Opcional)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin (YYYY-MM-DD)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.text.isNotBlank() && startDate.text.isNotBlank() && endDate.text.isNotBlank()) {
                        onProjectAdded(name.text, description.text, startDate.text, endDate.text)
                    }
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


// EditProjectDialog - (Keep the same)
@Composable
fun EditProjectDialog(
    project: Project,
    onDismiss: () -> Unit,
    onProjectUpdated: (name: String, description: String, startDate: String, endDate: String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(project.name)) }
    var description by remember { mutableStateOf(TextFieldValue(project.description ?: "")) }
    var startDate by remember { mutableStateOf(TextFieldValue(project.startDate)) }
    var endDate by remember { mutableStateOf(TextFieldValue(project.endDate)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Proyecto") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (Opcional)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin (YYYY-MM-DD)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.text.isNotBlank() && startDate.text.isNotBlank() && endDate.text.isNotBlank()) {
                        onProjectUpdated(name.text, description.text, startDate.text, endDate.text)
                    }
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
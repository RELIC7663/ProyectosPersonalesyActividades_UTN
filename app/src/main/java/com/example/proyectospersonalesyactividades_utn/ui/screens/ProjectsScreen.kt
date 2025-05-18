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
// data class Project(val id: Long = 0, val userId: Long, val name: String, ...)

@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    userId: Long, // The ID of the currently logged-in user
    onProjectSelected: (projectId: Long) -> Unit,
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
                                onProjectClick = onProjectSelected,
                                onEditClick = {
                                    // Set the project state for editing and show the dialog
                                    projectToEditState = it // Set the project object
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    // Set the project ID to delete and show confirmation dialog
                                    projectToDeleteId = it.id
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
                    description = description,
                    startDate = startDate,
                    endDate = endDate
                    // id is 0, database will assign it
                )
                viewModel.addProject(newProject) // Call ViewModel add function
                showAddDialog = false
            }
        )
    }

    // Edit Project Dialog - Shown only if projectToEditState is not null
    if (showEditDialog && projectToEditState != null) {
        EditProjectDialog(
            project = projectToEditState!!, // Pass the project object to the dialog
            onDismiss = {
                showEditDialog = false
                projectToEditState = null // Clear the project being edited state
            },
            onProjectUpdated = { updatedName, updatedDescription, updatedStartDate, updatedEndDate ->
                // Create an updated Project object based on the one being edited
                val updatedProject = projectToEditState!!.copy( // Use copy to maintain original ID, userId, etc.
                    name = updatedName,
                    description = updatedDescription,
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

// ProjectItem Composable (Improved layout)
@Composable
fun ProjectItem(
    project: Project,
    onProjectClick: (projectId: Long) -> Unit,
    onEditClick: (project: Project) -> Unit, // Pass the whole Project object
    onDeleteClick: (project: Project) -> Unit // Pass the whole Project object
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
        // Use separate click for the card body vs. icon buttons
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { onProjectClick(project.id) } // Make the main content clickable
            .padding(12.dp)
        ) {
            Text(project.name, style = MaterialTheme.typography.titleMedium)
            Text(project.description ?: "Sin descripción", style = MaterialTheme.typography.bodySmall) // Show default if null
            Spacer(modifier = Modifier.height(4.dp))
            Text("Inicio: ${project.startDate}", style = MaterialTheme.typography.bodySmall)
            Text("Fin: ${project.endDate}", style = MaterialTheme.typography.bodySmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Align icons to the end
            ) {
                // Edit Button
                IconButton(onClick = { onEditClick(project) }) { // Pass the project object
                    Icon(Icons.Filled.Edit, "Editar")
                }
                // Delete Button
                IconButton(onClick = { onDeleteClick(project) }) { // Pass the project object
                    Icon(Icons.Filled.Delete, "Eliminar")
                }
            }
        }
    }
}

// AddProjectDialog - Collects data and passes to callback
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    // Callback provides collected input data
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
                // TODO: Consider using a Date Picker for date fields and validating format
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Consider using a Date Picker for date fields and validating format
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin (YYYY-MM-DD)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Basic validation
                    if (name.text.isNotBlank() && startDate.text.isNotBlank() && endDate.text.isNotBlank()) {
                        onProjectAdded(name.text, description.text, startDate.text, endDate.text)
                    }
                    // Optionally show error message if validation fails
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


// EditProjectDialog - Receives Project data, collects updates, passes back
@Composable
fun EditProjectDialog(
    project: Project, // Receives the project data to pre-fill the form
    onDismiss: () -> Unit,
    // Callback provides updated input data (excluding ID, which is in the original Project)
    onProjectUpdated: (name: String, description: String, startDate: String, endDate: String) -> Unit
) {
    // Initialize states with project data
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
                // TODO: Consider using a Date Picker for date fields and validating format
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Consider using a Date Picker for date fields and validating format
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin (YYYY-MM-DD)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Basic validation
                    if (name.text.isNotBlank() && startDate.text.isNotBlank() && endDate.text.isNotBlank()) {
                        // Pass the updated data back to the screen
                        onProjectUpdated(name.text, description.text, startDate.text, endDate.text)
                    }
                    // Optionally show error message if validation fails
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
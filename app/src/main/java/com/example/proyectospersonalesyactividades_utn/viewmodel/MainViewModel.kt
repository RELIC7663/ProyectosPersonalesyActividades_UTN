// src/main/kotlin/com/example/proyectospersonalesyactividades_utn/viewmodel/MainViewModel.kt
package com.example.proyectospersonalesyactividades_utn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.proyectospersonalesyactividades_utn.controller.DataController
import com.example.proyectospersonalesyactividades_utn.models.Project
import com.example.proyectospersonalesyactividades_utn.models.Activity
import kotlinx.coroutines.launch

class MainViewModel(private val controller: DataController) : ViewModel() {

    // — Login state —
    // Changed to Boolean? to allow null for initial/reset state
    private val _loginSuccess = MutableLiveData<Long?>(null)
    val loginSuccess: LiveData<Long?> = _loginSuccess // Exposed as LiveData<Long?>

    private val _currentProject = MutableLiveData<Project?>(null)
    val currentProject: LiveData<Project?> = _currentProject


    fun login(username: String, password: String) {
        viewModelScope.launch {
            // Optional: Set to null or loading state before the attempt
            _loginSuccess.value = null
            _loginSuccess.value = controller.login2(username, password)
        }


    }

    // Add function to reset login state after it's consumed by the UI
    fun resetLoginState() {
        _loginSuccess.value = null // Reset to null after the event is handled
    }

    // — Registration state —
    // Changed to Boolean? to allow null for initial/reset state
    private val _registrationSuccess = MutableLiveData<Boolean?>(null)
    val registrationSuccess: LiveData<Boolean?> = _registrationSuccess

    // Modified to accept email and use the specific controller function
    fun register(username: String, password: String, email: String) {
        viewModelScope.launch {
            // Optional: Set to null or loading state before the attempt
            _registrationSuccess.value = null
            val userId = controller.registerUser(username, password, email)

            // Assume registration is successful if userId is positive (or not -1)
            _registrationSuccess.value = userId > 0 // or userId != -1, based on your implementation
        }
    }

    // Add function to reset registration state after it's consumed by the UI
    fun resetRegistrationState() {
        _registrationSuccess.value = null // Reset to null
    }


    // — Projects list —
    private val _projects = MutableLiveData<List<Project>>(emptyList())
    val projects: LiveData<List<Project>> = _projects

    // LiveData to hold the project being edited (optional, but useful for dialogs)
    private val _projectToEdit = MutableLiveData<Project?>(null)
    val projectToEdit: LiveData<Project?> = _projectToEdit


    fun loadProjects(userId: Long) {
        viewModelScope.launch {
            // This already correctly calls your getProjects method
            _projects.value = controller.getProjects(userId)
        }
    }

    // --- Project CRUD ViewModel Functions using YOUR Controller methods ---

    fun addProject(project: Project) {
        viewModelScope.launch {
            // Calls YOUR createProject method
            val newRowId = controller.createProject(project)
            if (newRowId != -1L) {
                // Refresh the list after successful insertion
                // Need the userId from the project to reload the list
                loadProjects(project.userId)
            }
            // Optional: Handle error if newRowId is -1
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            // Calls YOUR updateProject method
            val rowsAffected = controller.updateProject(project)
            if (rowsAffected > 0) {
                // Refresh the list after successful update
                // Need the userId from the project to reload the list
                loadProjects(project.userId)
            }
            // Optional: Handle error if rowsAffected is 0
        }
    }

    fun deleteProject(projectId: Long, userId: Long) {
        viewModelScope.launch {
            // Calls YOUR deleteProject method
            val rowsAffected = controller.deleteProject(projectId)
            if (rowsAffected > 0) {
                // Refresh the list after successful deletion
                loadProjects(userId) // Use the userId passed to the screen
            }
            // Optional: Handle error if rowsAffected is 0
        }
    }

    // Function to select a project for editing

    // Function to clear the selected project for editing
    fun clearProjectToEdit() {
        _projectToEdit.value = null
    }

    // — Activities list —
    private val _activities = MutableLiveData<List<Activity>>(emptyList())
    val activities: LiveData<List<Activity>> = _activities

    fun loadActivities(projectId: Long) {
        viewModelScope.launch {
            _activities.value = controller.getActivities(projectId)
            // Recalculate progress after loading activities for this project
            calculateProgress(projectId)
        }
    }
    fun loadProjectDetails(projectId: Long) {
        viewModelScope.launch {
            // *** IMPORTANT: This requires a 'getProjectById' method in your DataController ***
            // *** that queries the database for a single project by its ID and returns a Project? ***
            _currentProject.value = controller.getProjectById(projectId) // Assuming controller.getProjectById exists and returns Project?
        }
    }

    // --- New Activity CRUD ViewModel Functions ---

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            val newRowId = controller.createActivity(activity) // Assuming controller.addActivity exists
            if (newRowId != -1L) {
                // Refresh the list and progress after successful insertion
                loadActivities(activity.projectId) // Reload activities for the project
            }
            // Optional: Handle error if newRowId is -1
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            val rowsAffected = controller.updateActivity(activity) // Assuming controller.updateActivity exists
            if (rowsAffected > 0) {
                // Refresh the list and progress after successful update
                loadActivities(activity.projectId) // Reload activities for the project
            }
            // Optional: Handle error if rowsAffected is 0
        }
    }

    fun deleteActivity(activityId: Long, projectId: Long) {
        viewModelScope.launch {
            val rowsAffected = controller.deleteActivity(activityId) // Assuming controller.deleteActivity exists
            if (rowsAffected > 0) {
                // Refresh the list and progress after successful deletion
                loadActivities(projectId) // Reload activities for the project
            }
            // Optional: Handle error if rowsAffected is 0
        }
    }


    // — Progress —
    private val _progress = MutableLiveData<Float>(0f) // Initialize with 0%
    val progress: LiveData<Float> = _progress

    fun calculateProgress(projectId: Long) {
        viewModelScope.launch {
            _progress.value = controller.getProjectProgress(projectId) // Assuming getProjectProgress exists
        }
    }
}
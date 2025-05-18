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
    private val _loginSuccess = MutableLiveData<Boolean?>(null)
    val loginSuccess: LiveData<Boolean?> = _loginSuccess

    fun login(username: String, password: String) {
        viewModelScope.launch {
            // Optional: Set to null or loading state before the attempt
            _loginSuccess.value = null
            _loginSuccess.value = controller.login(username, password)
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

    fun loadProjects(userId: Long) {
        viewModelScope.launch {
            _projects.value = controller.getProjects(userId)
        }
    }

    // — Activities list —
    private val _activities = MutableLiveData<List<Activity>>(emptyList())
    val activities: LiveData<List<Activity>> = _activities

    fun loadActivities(projectId: Long) {
        viewModelScope.launch {
            _activities.value = controller.getActivities(projectId)
        }
    }

    // — Progress —
    private val _progress = MutableLiveData<Float>()
    val progress: LiveData<Float> = _progress

    fun calculateProgress(projectId: Long) {
        viewModelScope.launch {
            _progress.value = controller.getProjectProgress(projectId)
        }
    }
}
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
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginSuccess.value = controller.login(username, password)
        }
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

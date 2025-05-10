// src/main/kotlin/com/example/proyectospersonalesyactividades_utn/models/Activity.kt
package com.example.proyectospersonalesyactividades_utn.models

/**
 * Representa una actividad dentro de un proyecto.
 */
data class Activity(
    val id: Long,           // activity_id en la base
    val projectId: Long,    // referencia a Project.id
    val name: String,       // nombre de la actividad
    val description: String,// descripción opcional
    val startDate: String,  // fecha de inicio
    val endDate: String,    // fecha de fin
    val status: String      // 'Planificado', 'En ejecución' o 'Realizado'
)

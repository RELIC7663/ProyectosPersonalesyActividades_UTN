// src/main/kotlin/com/example/proyectospersonalesyactividades_utn/models/Project.kt
package com.example.proyectospersonalesyactividades_utn.models

/**
 * Representa un proyecto perteneciente a un usuario.
 */
data class Project(
    val id: Long = 0L,      // project_id en la base
    val userId: Long,       // referencia a User.id
    val name: String,       // nombre del proyecto
    val description: String,// descripción opcional
    val startDate: String,  // fecha de inicio (ISO String)
    val endDate: String     // fecha de fin (ISO String)
)

// src/main/kotlin/com/example/proyectospersonalesyactividades_utn/models/User.kt
package com.example.proyectospersonalesyactividades_utn.models

/**
 * Representa un usuario registrado en la aplicación.
 */
data class User(
    val id: Long,         // user_id en la base de datos
    val username: String, // nombre único
    val email: String     // correo electrónico
)

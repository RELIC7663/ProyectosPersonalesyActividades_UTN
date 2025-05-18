package com.example.proyectospersonalesyactividades_utn.controller

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.proyectospersonalesyactividades_utn.database.dbhelper
import com.example.proyectospersonalesyactividades_utn.models.Project
import com.example.proyectospersonalesyactividades_utn.models.Activity

/**
 * DataController: capa de repositorio que expone APIs de datos a ViewModel/UI.
 */
class DataController(context: Context) {

    // Instancia de SQLiteOpenHelper personalizado
    private val dbHelper by lazy { dbhelper(context) }

    // ── USUARIOS ─────────────────────────────────────────────────────────

    /**
     * Intenta login; true si las credenciales son válidas.
     */
    suspend fun login(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            dbHelper.checkUserCredentials(username, password)
        }
    }
    suspend fun login2(username: String, password: String): Long? = withContext(Dispatchers.IO) {
        dbHelper.checkUserCredentials2(username, password) // Call the updated dbHelper method
    }


    /**
     * Registra un usuario; devuelve nuevo ID o -1.
     */
    suspend fun registerUser(username: String, password: String, email: String): Long {
        return withContext(Dispatchers.IO) {
            dbHelper.addUser(username, password, email)
        }
    }

    // ── PROYECTOS ────────────────────────────────────────────────────────

    /**
     * Crea un proyecto; devuelve project_id o -1.
     */
    suspend fun createProject(project: Project): Long {
        return withContext(Dispatchers.IO) {
            dbHelper.addProject(
                project.userId,
                project.name,
                project.description,
                project.startDate,
                project.endDate
            )
        }
    }

    /**
     * Lee todos los proyectos de un usuario como lista de modelos.
     */
    suspend fun getProjects(userId: Long): List<Project> {
        return withContext(Dispatchers.IO) {
            val cursor = dbHelper.getProjects(userId)
            val list = mutableListOf<Project>()
            cursor.use {
                while (it.moveToNext()) {
                    list += Project(
                        id = it.getLong(it.getColumnIndexOrThrow("project_id")),
                        userId = userId,
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        startDate = it.getString(it.getColumnIndexOrThrow("start_date")),
                        endDate = it.getString(it.getColumnIndexOrThrow("end_date"))
                    )
                }
            }
            list
        }
    }
    suspend fun getProjectById(projectId: Long): Project? = withContext(Dispatchers.IO) {
        var project: Project? = null
        // Use dbHelper's readableDatabase to query the Projects table
        val cursor = dbHelper.readableDatabase.query(
            "Projects", // Table name
            null,       // Columns (null means all columns)
            "project_id = ?", // WHERE clause
            arrayOf(projectId.toString()), // Arguments for WHERE clause
            null, null, // groupBy, having
            null,       // orderBy
            "1"         // LIMIT 1 row
        )

        cursor.use { // Ensure the cursor is closed
            if (it.moveToFirst()) { // Move to the first (and only) result row
                // Map the cursor row to a Project object
                project = Project(
                    id = it.getLong(it.getColumnIndexOrThrow("project_id")),
                    userId = it.getLong(it.getColumnIndexOrThrow("user_id")), // Make sure you get the userId too
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    description = it.getString(it.getColumnIndexOrThrow("description")),
                    startDate = it.getString(it.getColumnIndexOrThrow("start_date")),
                    endDate = it.getString(it.getColumnIndexOrThrow("end_date"))
                )
            }
        }
        project // Return the Project object or null
    }
    /**
     * Actualiza un proyecto; devuelve filas afectadas.
     */
    suspend fun updateProject(project: Project): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.updateProject(
                project.id,
                project.name,
                project.description,
                project.startDate,
                project.endDate
            )
        }
    }

    /**
     * Elimina un proyecto (y sus actividades en cascade).
     */
    suspend fun deleteProject(projectId: Long): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.deleteProject(projectId)
        }
    }
    // ── ACTIVIDADES ───────────────────────────────────────────────────────

    /**
     * Crea una actividad; devuelve activity_id o -1.
     */
    suspend fun createActivity(activity: Activity): Long {
        return withContext(Dispatchers.IO) {
            dbHelper.addActivity(
                activity.projectId,
                activity.name,
                activity.description,
                activity.startDate,
                activity.endDate,
                activity.status
            )
        }
    }

    /**
     * Lee actividades de un proyecto como lista de modelos.
     */
    suspend fun getActivities(projectId: Long): List<Activity> {
        return withContext(Dispatchers.IO) {
            val cursor = dbHelper.getActivities(projectId)
            val list = mutableListOf<Activity>()
            cursor.use {
                while (it.moveToNext()) {
                    list += Activity(
                        id = it.getLong(it.getColumnIndexOrThrow("activity_id")),
                        projectId = projectId,
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        startDate = it.getString(it.getColumnIndexOrThrow("start_date")),
                        endDate = it.getString(it.getColumnIndexOrThrow("end_date")),
                        status = it.getString(it.getColumnIndexOrThrow("status"))
                    )
                }
            }
            list
        }
    }

    /**
     * Actualiza una actividad; devuelve filas afectadas.
     */
    suspend fun updateActivity(activity: Activity): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.updateActivity(
                activity.id,
                activity.name,
                activity.description,
                activity.startDate,
                activity.endDate,
                activity.status
            )
        }
    }

    /**
     * Elimina una actividad.
     */
    suspend fun deleteActivity(activityId: Long): Int {
        return withContext(Dispatchers.IO) {
            dbHelper.deleteActivity(activityId)
        }
    }

    // ── AVANCE DE PROYECTO ─────────────────────────────────────────────────

    /**
     * Porcentaje de actividades “Realizado” (0f–1f).
     */
    suspend fun getProjectProgress(projectId: Long): Float {
        return withContext(Dispatchers.IO) {
            dbHelper.getProjectProgress(projectId)
        }
    }
}

package com.example.proyectospersonalesyactividades_utn.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class dbhelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "ProjectDB"       // Nombre de la base de datos
        private const val DATABASE_VERSION = 1               // Versión inicial
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla Users
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Users (
                user_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                username    TEXT UNIQUE NOT NULL,
                password    TEXT NOT NULL,
                email       TEXT NOT NULL
            )
        """.trimIndent())  // :contentReference[oaicite:0]{index=0}

        // Tabla Projects
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Projects (
                project_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER NOT NULL,
                name         TEXT NOT NULL,
                description  TEXT,
                start_date   TEXT NOT NULL,
                end_date     TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        """.trimIndent())  // :contentReference[oaicite:1]{index=1}

        // Tabla Activities
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Activities (
                activity_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                project_id   INTEGER NOT NULL,
                name         TEXT NOT NULL,
                description  TEXT,
                start_date   TEXT NOT NULL,
                end_date     TEXT NOT NULL,
                status       TEXT NOT NULL 
                                CHECK(status IN ('Planificado','En ejecución','Realizado')),
                FOREIGN KEY(project_id) REFERENCES Projects(project_id) ON DELETE CASCADE
            )
        """.trimIndent())  // :contentReference[oaicite:2]{index=2}
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de cambio de versión, elimina y recrea
        db.execSQL("DROP TABLE IF EXISTS Activities")
        db.execSQL("DROP TABLE IF EXISTS Projects")
        db.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db)  // :contentReference[oaicite:3]{index=3}
    }

    // ───── Métodos CRUD para Users ─────

    /** Inserta un nuevo usuario, devuelve su user_id o -1 en error */
    fun addUser(username: String, password: String, email: String): Long {
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("email", email)
        }
        return writableDatabase.insert("Users", null, values)  // :contentReference[oaicite:4]{index=4}
    }

    /** Verifica credenciales, devuelve true si existen */
    fun checkUserCredentials(username: String, password: String): Boolean {
        val cursor: Cursor = readableDatabase.query(
            "Users",
            arrayOf("user_id"),
            "username = ? AND password = ?",
            arrayOf(username, password),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists  // :contentReference[oaicite:5]{index=5}
    }

    // ───── Métodos CRUD para Projects ─────

    /** Inserta un proyecto, devuelve project_id o -1 */
    fun addProject(userId: Long, name: String, description: String, startDate: String, endDate: String): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", name)
            put("description", description)
            put("start_date", startDate)
            put("end_date", endDate)
        }
        return writableDatabase.insert("Projects", null, values)  // :contentReference[oaicite:6]{index=6}
    }

    /** Obtiene todos los proyectos de un usuario */
    fun getProjects(userId: Long): Cursor {
        return readableDatabase.query(
            "Projects",
            null,
            "user_id = ?",
            arrayOf(userId.toString()),
            null, null,
            "start_date DESC"
        )  // :contentReference[oaicite:7]{index=7}
    }

    /** Actualiza un proyecto por su ID, devuelve número de filas afectadas */
    fun updateProject(
        projectId: Long,
        name: String,
        description: String,
        startDate: String,
        endDate: String
    ): Int {
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("start_date", startDate)
            put("end_date", endDate)
        }
        return writableDatabase.update(
            "Projects",
            values,
            "project_id = ?",
            arrayOf(projectId.toString())
        )  // :contentReference[oaicite:8]{index=8}
    }

    /** Elimina un proyecto (y sus activities por cascade) */
    fun deleteProject(projectId: Long): Int {
        return writableDatabase.delete(
            "Projects",
            "project_id = ?",
            arrayOf(projectId.toString())
        )  // :contentReference[oaicite:9]{index=9}
    }

    // ───── Métodos CRUD para Activities ─────

    /** Inserta una actividad, devuelve activity_id o -1 */
    fun addActivity(
        projectId: Long,
        name: String,
        description: String,
        startDate: String,
        endDate: String,
        status: String
    ): Long {
        val values = ContentValues().apply {
            put("project_id", projectId)
            put("name", name)
            put("description", description)
            put("start_date", startDate)
            put("end_date", endDate)
            put("status", status)
        }
        return writableDatabase.insert("Activities", null, values)  // :contentReference[oaicite:10]{index=10}
    }

    /** Obtiene todas las actividades de un proyecto */
    fun getActivities(projectId: Long): Cursor {
        return readableDatabase.query(
            "Activities",
            null,
            "project_id = ?",
            arrayOf(projectId.toString()),
            null, null,
            "start_date ASC"
        )  // :contentReference[oaicite:11]{index=11}
    }

    /** Actualiza una actividad, devuelve número de filas afectadas */
    fun updateActivity(
        activityId: Long,
        name: String,
        description: String,
        startDate: String,
        endDate: String,
        status: String
    ): Int {
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("start_date", startDate)
            put("end_date", endDate)
            put("status", status)
        }
        return writableDatabase.update(
            "Activities",
            values,
            "activity_id = ?",
            arrayOf(activityId.toString())
        )  // :contentReference[oaicite:12]{index=12}
    }

    /** Elimina una actividad por su ID */
    fun deleteActivity(activityId: Long): Int {
        return writableDatabase.delete(
            "Activities",
            "activity_id = ?",
            arrayOf(activityId.toString())
        )  // :contentReference[oaicite:13]{index=13}
    }

    // ───── Cálculo de avance ─────

    /**
     * Calcula el porcentaje de actividades “Realizado” sobre el total
     * Devuelve un Float entre 0.0 y 1.0
     */
    fun getProjectProgress(projectId: Long): Float {
        val doneCursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM Activities WHERE project_id = ? AND status = 'Realizado'",
            arrayOf(projectId.toString())
        )
        val totalCursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM Activities WHERE project_id = ?",
            arrayOf(projectId.toString())
        )

        doneCursor.moveToFirst()
        totalCursor.moveToFirst()
        val done = doneCursor.getInt(0)
        val total = totalCursor.getInt(0)
        doneCursor.close()
        totalCursor.close()

        return if (total > 0) done.toFloat() / total else 0f  // :contentReference[oaicite:14]{index=14}
    }
}

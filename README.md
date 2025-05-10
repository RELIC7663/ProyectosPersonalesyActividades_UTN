# Gestor de Proyectos Personales - UTN

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org)

Aplicación móvil para gestión de proyectos y actividades con autenticación de usuarios y seguimiento de progreso.

## Características Principales

- ✅ Autenticación segura de usuarios
- 🗂️ Gestión CRUD de proyectos
- 📅 Administración de actividades con fechas
- 📊 Cálculo automático de avance
- 🔒 Persistencia local con SQLite

## Estructura de la Base de Datos
![Diagrama Entidad-Relación](https://via.placeholder.com/800x400.png?text=Diagrama+ER+de+la+Base+de+Datos)

```sql
-- Tabla: Users
CREATE TABLE Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    email TEXT NOT NULL
);

-- Tabla: Projects
CREATE TABLE Projects (
    project_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    name TEXT NOT NULL,
    description TEXT,
    start_date TEXT,
    end_date TEXT,
    FOREIGN KEY(user_id) REFERENCES Users(user_id)
);

-- Tabla: Activities
CREATE TABLE Activities (
    activity_id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    description TEXT,
    start_date TEXT,
    end_date TEXT,
    status TEXT CHECK(status IN ('Planificado', 'En ejecución', 'Realizado')),
    FOREIGN KEY(project_id) REFERENCES Projects(project_id)
);

package com.example.proyectospersonalesyactividades_utn


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectospersonalesyactividades_utn.controller.DataController
import com.example.proyectospersonalesyactividades_utn.viewmodel.MainViewModel
import com.example.proyectospersonalesyactividades_utn.ui.navigation.AppNavHost
import com.example.proyectospersonalesyactividades_utn.ui.theme.ProyectosPersonalesyActividades_UTNTheme

class MainActivity : ComponentActivity() {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar DataController y ViewModel
        val controller = DataController(this)
        mainViewModel = MainViewModel(controller)

        enableEdgeToEdge()
        setContent {
            ProyectosPersonalesyActividades_UTNTheme {
                // Usamos NavHost para navegación entre pantallas
                AppNavHost(viewModel = mainViewModel)
            }
        }
    }
}

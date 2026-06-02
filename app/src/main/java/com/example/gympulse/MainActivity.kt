package com.example.gympulse
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gympulse.ui.navigation.NavGraph
import com.example.gympulse.ui.navigation.Screen
import com.example.gympulse.ui.theme.GymPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymPulseTheme {
                MainScaffold()
            }
        }
    }
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Rutas donde NO mostramos el BottomBar
    val hideBottomBar = currentRoute == Screen.NuevoWorkout.route ||
            currentRoute == Screen.CrearRutina.route ||
            currentRoute == Screen.IniciarRutina.route

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Registro.route,
                        onClick  = {
                            navController.navigate(Screen.Registro.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon     = { Icon(Icons.Default.History, contentDescription = null) },
                        label    = { Text("Registro") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Rutinas.route,
                        onClick  = {
                            navController.navigate(Screen.Rutinas.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon     = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                        label    = { Text("Rutinas") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Estadisticas.route,
                        onClick  = {
                            navController.navigate(Screen.Estadisticas.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon     = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label    = { Text("Estadísticas") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)  // ← agrega esto
        )
    }
}
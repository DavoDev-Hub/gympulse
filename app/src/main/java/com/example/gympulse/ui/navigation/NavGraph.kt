package com.example.gympulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gympulse.data.AppDatabase
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.estadisticas.EstadisticasScreen
import com.example.gympulse.ui.registro.NuevoWorkoutScreen
import com.example.gympulse.ui.registro.RegistroScreen
import com.example.gympulse.ui.rutinas.RutinasScreen

sealed class Screen(val route: String) {
    object Registro     : Screen("registro")
    object NuevoWorkout : Screen("nuevo_workout?routineId={routineId}")
    object Rutinas      : Screen("rutinas")
    object Estadisticas : Screen("estadisticas")

    companion object {
        fun nuevoWorkout(routineId: Long? = null): String {
            return "nuevo_workout?routineId=${routineId ?: -1L}"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val repository = WorkoutRepository(db)

    NavHost(
        navController = navController,
        startDestination = Screen.Registro.route,
        modifier = modifier
    ) {
        composable(Screen.Registro.route) {
            RegistroScreen(
                repository     = repository,
                onNuevoWorkout = { navController.navigate(Screen.nuevoWorkout()) }
            )
        }
        composable(
            route = Screen.NuevoWorkout.route,
            arguments = listOf(navArgument("routineId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: -1L
            NuevoWorkoutScreen(
                repository = repository,
                routineId  = if (routineId == -1L) null else routineId,
                onTerminar = { navController.popBackStack() }
            )
        }
        composable(Screen.Rutinas.route) {
            RutinasScreen(
                repository      = repository,
                onIniciarRutina = { routineId ->
                    navController.navigate(Screen.nuevoWorkout(routineId))
                }
            )
        }
        composable(Screen.Estadisticas.route) {
            EstadisticasScreen(repository = repository)
        }
    }
}
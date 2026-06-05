package com.example.gympulse
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraph
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gympulse.data.PreferencesManager
import com.example.gympulse.ui.navigation.NavGraph
import com.example.gympulse.ui.navigation.Screen
import com.example.gympulse.ui.theme.GymPulseTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = remember { PreferencesManager(this@MainActivity) }
            var showRestDayDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.format(Date())
                if (prefs.restDayDialogDismissedDate != today) {
                    val cal = Calendar.getInstance()
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    val restDayValue = if (dow == Calendar.SUNDAY) 7 else dow - 1
                    if (restDayValue in prefs.restDays) {
                        showRestDayDialog = true
                    }
                }
            }

            GymPulseTheme {
                if (showRestDayDialog) {
                    RestDayReminderDialog(
                        todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        prefs = prefs,
                        onDismiss = { showRestDayDialog = false }
                    )
                }
                MainScaffold()
            }
        }
    }
}

@Composable
fun RestDayReminderDialog(
    todayDate: String,
    prefs: PreferencesManager,
    onDismiss: () -> Unit
) {
    val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val restDayValue = if (dow == Calendar.SUNDAY) 7 else dow - 1
    val dayName = dayNames[restDayValue - 1]

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ey hoy es $dayName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Vete a descansar",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "\uD83D\uDE34",
                    fontSize = 56.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        prefs.restDayDialogDismissedDate = todayDate
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Aceptar", fontSize = 16.sp)
                }
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}
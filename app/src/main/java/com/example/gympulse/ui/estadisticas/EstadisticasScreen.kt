package com.example.gympulse.ui.estadisticas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(repository: WorkoutRepository) {
    val viewModel: EstadisticasViewModel = viewModel(
        factory = EstadisticasViewModel.Factory(repository)
    )
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val years by viewModel.yearsAvailable.collectAsStateWithLifecycle()

    val filtros = viewModel.filtros
    var filtrosExpandidos by remember { mutableStateOf(false) }
    var añoExpanded by remember { mutableStateOf(false) }
    var mesExpanded by remember { mutableStateOf(false) }
    var mostrarSemana by remember(filtros.año, filtros.mes) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { filtrosExpandidos = !filtrosExpandidos },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtros: ${generarTextoFiltro(filtros, viewModel.semanaSeleccionada)}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = if (filtrosExpandidos) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = filtrosExpandidos) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = añoExpanded,
                                        onExpandedChange = { añoExpanded = it }
                                    ) {
                                        OutlinedTextField(
                                            value = if (filtros.año == null) "Todo" else filtros.año.toString(),
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = añoExpanded) },
                                            modifier = Modifier.menuAnchor(),
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanPrimary, unfocusedBorderColor = CardDark,
                                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = añoExpanded,
                                            onDismissRequest = { añoExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Todo") },
                                                onClick = { viewModel.setFiltroAño(null); añoExpanded = false }
                                            )
                                            years.forEach { y ->
                                                DropdownMenuItem(
                                                    text = { Text(y.toString()) },
                                                    onClick = { viewModel.setFiltroAño(y); añoExpanded = false }
                                                )
                                            }
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = mesExpanded && filtros.año != null,
                                        onExpandedChange = { if (filtros.año != null) mesExpanded = it }
                                    ) {
                                        OutlinedTextField(
                                            value = if (filtros.mes == null) "Todo" else getMonthName(filtros.mes!!),
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = filtros.año != null,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mesExpanded) },
                                            modifier = Modifier.menuAnchor(),
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanPrimary, unfocusedBorderColor = CardDark,
                                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = mesExpanded,
                                            onDismissRequest = { mesExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Todo") },
                                                onClick = { viewModel.setFiltroMes(null); mesExpanded = false }
                                            )
                                            (1..12).forEach { m ->
                                                DropdownMenuItem(
                                                    text = { Text(getMonthName(m)) },
                                                    onClick = { viewModel.setFiltroMes(m); mesExpanded = false }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (filtros.año != null && filtros.mes != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = mostrarSemana,
                                        onCheckedChange = {
                                            mostrarSemana = it
                                            if (!it) viewModel.setSemana(null)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                                    )
                                    Text("Filtrar por semana", fontSize = 13.sp, color = TextSecondary)
                                }
                                if (mostrarSemana) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    CalendarWeekPicker(
                                        year = filtros.año!!,
                                        month = filtros.mes!!,
                                        selectedWeek = viewModel.semanaSeleccionada,
                                        onSelectWeek = { viewModel.setSemana(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RachaCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    valor = "${stats.rachaActual}",
                    titulo = "días consecutivos"
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.fillMaxWidth(),
                        titulo = "Entrenamientos",
                        valor = "${stats.totalWorkouts}",
                        icono = Icons.Default.FitnessCenter
                    )
                    StatCard(
                        modifier = Modifier.fillMaxWidth(),
                        titulo = "Minutos totales",
                        valor = "${stats.totalMinutos}",
                        icono = Icons.Default.Timer
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stats.tituloGrafico,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (stats.unidadGrafico == "h") {
                        Text("Eje vertical: Horas", color = TextSecondary, fontSize = 11.sp)
                    } else {
                        Text("Eje vertical: Minutos", color = TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    BarChart(data = stats.grafico, unidad = stats.unidadGrafico)
                }
            }
        }

        if (stats.ejerciciosMasFrecuentes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Ejercicios más frecuentes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        stats.ejerciciosMasFrecuentes.forEachIndexed { index, (nombre, veces) ->
                            EjercicioFrecuenteRow(
                                posicion = index + 1,
                                nombre = nombre,
                                veces = veces,
                                max = stats.ejerciciosMasFrecuentes.first().second
                            )
                            if (index < stats.ejerciciosMasFrecuentes.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CalendarWeekPicker(
    year: Int,
    month: Int,
    selectedWeek: SemanaInfo?,
    onSelectWeek: (SemanaInfo?) -> Unit
) {
    val weeks = remember(year, month) { getWeeksForMonth(year, month) }
    val days = remember(year, month) { generarDiasCalendario(year, month) }
    val rowLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    val selectedWeekIndex = weeks.indexOfFirst { it == selectedWeek }
    var selectedIdx by remember(selectedWeek) { mutableStateOf(if (selectedWeekIndex >= 0) selectedWeekIndex else -1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${getMonthName(month)} $year",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (selectedIdx in weeks.indices) {
                val w = weeks[selectedIdx]
                Text(
                    text = "Semana seleccionada: ${w.label}",
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                rowLabels.forEach { dia ->
                    Text(
                        text = dia,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            val numRows = days.size / 7
            for (row in 0 until numRows) {
                val isSelected = row == selectedIdx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable {
                            selectedIdx = row
                            if (row in weeks.indices) onSelectWeek(weeks[row])
                        }
                        .padding(vertical = 4.dp)
                ) {
                    for (col in 0 until 7) {
                        val idx = row * 7 + col
                        val dia = days.getOrNull(idx)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dia != null) {
                                Text(
                                    text = dia.toString(),
                                    fontSize = 12.sp,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = valor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = titulo,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private val GoldGradientColors = listOf(
    Color(0xFFB8860B),
    Color(0xFFDAA520),
    Color(0xFFFFD700),
    Color(0xFFF5D76E),
    Color(0xFFFFD700),
    Color(0xFFDAA520)
)

@Composable
fun RachaCard(
    modifier: Modifier = Modifier,
    valor: String,
    titulo: String
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                -0.5f at 0
                1.5f at 800
                1.5f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            GoldGradientColors,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, 0f)
                        ),
                        size = size
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (shimmerProgress in -0.4f..1.4f) {
                    val t = (shimmerProgress + 0.5f).coerceIn(0f, 1f)
                    val cx = t * size.width * 1.4f - size.width * 0.2f
                    val cy = t * size.height * 1.4f - size.height * 0.2f
                    val gradLen = maxOf(size.width, size.height) * 0.275f

                    drawRect(
                        brush = Brush.linearGradient(
                            0.0f to Color.Transparent,
                            0.3f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = 0.5f),
                            0.7f to Color.White.copy(alpha = 0.15f),
                            1.0f to Color.Transparent,
                            start = Offset(cx - gradLen, cy - gradLen),
                            end = Offset(cx + gradLen, cy + gradLen)
                        ),
                        size = size,
                        blendMode = BlendMode.Screen
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = valor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White
                )
                Text(
                    text = titulo,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BarChart(data: List<EstadisticasViewModel.BarraDato>, unidad: String) {
    val maxVal = data.maxOfOrNull { it.valor }?.takeIf { it > 0 } ?: 1

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    if (item.valor > 0) {
                        Text(
                            text = "${item.valor}",
                            fontSize = 9.sp,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    val fraccion = item.valor.toFloat() / maxVal
                    Box(
                        modifier = Modifier
                            .width(if (data.size > 7) 16.dp else 24.dp)
                            .height((fraccion * 80).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (item.valor > 0) CyanPrimary
                                else CardDark.copy(alpha = 0.3f)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = if (data.size > 7) 8.sp else 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun generarTextoFiltro(filtros: Filtros, semana: SemanaInfo?): String {
    val a = filtros.año
    val m = filtros.mes
    return when {
        a == null -> "Todo"
        m == null -> "Todo $a"
        semana != null -> semana.label
        else -> "${getMonthName(m)} $a"
    }
}

@Composable
fun EjercicioFrecuenteRow(
    posicion: Int,
    nombre: String,
    veces: Int,
    max: Int
) {
    val fraccion = veces.toFloat() / max

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "$posicion",
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(nombre, fontSize = 13.sp)
            }
            Text("$veces veces", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .background(CyanPrimary)
            )
        }
    }
}

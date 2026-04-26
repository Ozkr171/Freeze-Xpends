package com.example.freeze_xpends.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit
) {
    // Estado para manejar el mes y año actual
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Días de la semana
    val dias = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

    // Cálculos del calendario
    val daysInMonth = currentMonth.lengthOfMonth()
    // Ajustamos para que la semana empiece en Domingo (0)
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7

    // Nombre del mes en español
    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val year = currentMonth.year

    // Función simulada de gastos (Igual a tu código de React)
    fun getDummyDataForDay(day: Int): Int {
        if (day == 5 || day == 14) return 2
        if (day == 10 || day == 22) return 1
        return 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // ==========================================
        // HEADER AZUL
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BluePrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Calendario de Gastos",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ==========================================
        // CONTENIDO SCROLLABLE
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // TARJETA DEL CALENDARIO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Controles de navegación del mes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes Anterior", tint = ForegroundDark)
                        }

                        Text(
                            text = "$monthName $year",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForegroundDark
                        )

                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Mes Siguiente", tint = ForegroundDark)
                        }
                    }

                    // Encabezado de los días (Dom, Lun, Mar...)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dias.forEach { dia ->
                            Text(
                                text = dia,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = SlateMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cuadrícula de los días (6 filas max * 7 columnas)
                    val totalSlots = firstDayOfMonth + daysInMonth
                    val rows = Math.ceil(totalSlots / 7.0).toInt()

                    for (i in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (j in 0..6) {
                                val dayIndex = (i * 7) + j
                                val dayNumber = dayIndex - firstDayOfMonth + 1

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                ) {
                                    if (dayNumber in 1..daysInMonth) {
                                        val gastosCount = getDummyDataForDay(dayNumber)
                                        val isHighlighted = gastosCount > 0

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isHighlighted) GreenAccent else BackgroundGray)
                                                .clickable { /* Mostrar detalles del día */ }
                                                .padding(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                color = if (isHighlighted) Color.White else ForegroundDark,
                                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                            if (isHighlighted) {
                                                Text(
                                                    text = "$gastosCount trans.",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 9.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TARJETA DE RESUMEN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Resumen de $monthName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForegroundDark,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total de transacciones", color = SlateMuted)
                        Text("6", fontWeight = FontWeight.Medium, color = ForegroundDark)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Días con gastos", color = SlateMuted)
                        Text("4", fontWeight = FontWeight.Medium, color = ForegroundDark)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Promedio por día", color = SlateMuted)
                        Text("$3,900", fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                }
            }
        }
    }
}
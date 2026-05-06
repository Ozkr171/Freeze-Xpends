package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// --- DATOS SIMULADOS ---
// Estructura para guardar el número de transacciones y el monto gastado ese día
data class DailyExpenseData(val transactions: Int, val totalAmount: Float)

@Composable
fun CalendarScreen(onNavigateBack: () -> Unit) {
    // Empezamos en Abril 2026 como en tu captura
    var currentYearMonth by remember { mutableStateOf(YearMonth.of(2026, 4)) }

    // Simulamos los datos basados en tus bocetos (5, 10, 14, y 22 tienen gastos)
    // En la vida real, esto vendría de tu base de datos de Aiven filtrado por currentYearMonth
    val mockExpenses = remember(currentYearMonth) {
        val year = currentYearMonth.year
        val month = currentYearMonth.monthValue
        mapOf(
            LocalDate.of(year, month, 5) to DailyExpenseData(2, 7800f),
            LocalDate.of(year, month, 10) to DailyExpenseData(1, 1500f),
            LocalDate.of(year, month, 14) to DailyExpenseData(2, 3500f),
            LocalDate.of(year, month, 22) to DailyExpenseData(1, 2800f)
        )
    }

    // Cálculos para el resumen
    val totalTransactions = mockExpenses.values.sumOf { it.transactions }
    val daysWithExpenses = mockExpenses.size
    val totalAmountSpent = mockExpenses.values.sumOf { it.totalAmount.toDouble() }.toFloat()
    val averagePerDay = if (daysWithExpenses > 0) totalAmountSpent / daysWithExpenses else 0f

    // Nombre del mes en español
    val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")).replaceFirstChar { it.uppercase() }
    val year = currentYearMonth.year

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
    ) {
        // --- 1. HEADER AZUL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(PrimaryBlue),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = "Calendario de Gastos",
                modifier = Modifier.padding(start = 48.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // --- 2. TARJETA DEL CALENDARIO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Controles del mes
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = TextDark)
                        }
                        Text(
                            text = "$monthName $year",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = TextDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Días de la semana
                    val daysOfWeek = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        daysOfWeek.forEach { day ->
                            Text(text = day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lógica del Grid del Calendario
                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    // value 1 = Lunes ... 7 = Domingo. Ajustamos para que Domingo sea 0
                    val startOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
                    val daysInMonth = currentYearMonth.lengthOfMonth()

                    val totalCells = startOffset + daysInMonth
                    val rows = if (totalCells % 7 == 0) totalCells / 7 else (totalCells / 7) + 1

                    // Dibujamos el grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height((rows * 60).dp), // Altura dinámica según las semanas del mes
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Celdas vacías antes del primer día del mes
                        items(startOffset) {
                            Box(modifier = Modifier.size(40.dp))
                        }

                        // Días reales del mes
                        items(daysInMonth) { dayIndex ->
                            val currentDay = dayIndex + 1
                            val date = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, currentDay)
                            val expenseData = mockExpenses[date]

                            CalendarDayCell(
                                dayNumber = currentDay.toString(),
                                expenseData = expenseData
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. TARJETA DE RESUMEN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Resumen de $monthName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

                    Spacer(modifier = Modifier.height(16.dp))

                    SummaryRow(label = "Total de transacciones", value = totalTransactions.toString(), isHighlight = false)
                    HorizontalDivider(color = BorderSlate.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))

                    SummaryRow(label = "Días con gastos", value = daysWithExpenses.toString(), isHighlight = false)
                    HorizontalDivider(color = BorderSlate.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))

                    SummaryRow(label = "Promedio por día", value = "$${"%.0f".format(averagePerDay)}", isHighlight = true)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun CalendarDayCell(dayNumber: String, expenseData: DailyExpenseData?) {
    val hasExpenses = expenseData != null

    Box(
        modifier = Modifier.height(56.dp), // Altura fija para que los "pills" verdes tengan espacio
        contentAlignment = Alignment.TopCenter
    ) {
        if (hasExpenses) {
            // CELDA ACTIVA (Píldora verde - image_02ef78.png)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .background(AccentGreen, RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = dayNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "${expenseData!!.transactions}\ntrans.",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        } else {
            // CELDA INACTIVA (Cuadro con borde)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = dayNumber, fontSize = 14.sp, color = TextDark)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) PrimaryBlue else TextDark
        )
    }
}
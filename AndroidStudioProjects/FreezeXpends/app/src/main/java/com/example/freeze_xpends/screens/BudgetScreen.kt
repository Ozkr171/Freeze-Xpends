package com.example.freeze_xpends.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.freeze_xpends.theme.*

@Composable
fun BudgetScreen(onNavigateBack: () -> Unit) {
    // --- ESTADO GLOBAL (Simulación de datos) ---
    val budgetData = remember {
        mutableStateListOf(
            BudgetCategoryData("Vivienda", 7000f, 10000f, SecondaryRed, Icons.Default.Home),
            BudgetCategoryData("Servicios", 900f, 1500f, Color(0xFF4A4453), Icons.Default.Settings),
            BudgetCategoryData("Alimentación", 2000f, 5000f, Color(0xFF90A4AE), Icons.Default.Restaurant)
        )
    }
    var mainBudgetLimit by remember { mutableStateOf(16500f) }
    var spentMain by remember { mutableStateOf(9900f) }

    // --- ESTADOS DE NAVEGACIÓN Y DIÁLOGOS ---
    var showReport by remember { mutableStateOf(false) }
    var showEditMainDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var editingCategoryIndex by remember { mutableStateOf(-1) }
    var budgetAmountInput by remember { mutableStateOf("") }

    // --- CONTROL DE VISTAS ---
    if (showReport) {
        ReportContent(onClose = { showReport = false })
    } else {
        BudgetMainContent(
            onNavigateBack = onNavigateBack,
            onGenerateReport = { showReport = true },
            spentMain = spentMain,
            mainBudgetLimit = mainBudgetLimit,
            budgetData = budgetData,
            onEditMainBudget = {
                budgetAmountInput = mainBudgetLimit.toInt().toString()
                showEditMainDialog = true
            },
            onEditCategoryBudget = { index ->
                editingCategoryIndex = index
                budgetAmountInput = budgetData[index].limit.toInt().toString()
                showEditCategoryDialog = true
            }
        )
    }

    // --- DIÁLOGO DE EDICIÓN LÍMITE GLOBAL ---
    if (showEditMainDialog) {
        BudgetEditDialog(
            title = "LIMITE DE GASTO GLOBAL",
            initialValue = budgetAmountInput,
            onClose = { showEditMainDialog = false },
            onConfirm = { newValue ->
                val newLimit = newValue.toFloatOrNull() ?: mainBudgetLimit
                mainBudgetLimit = newLimit
                showEditMainDialog = false
            }
        )
    }

    // --- DIÁLOGO DE EDICIÓN LÍMITE CATEGORÍA ---
    if (showEditCategoryDialog && editingCategoryIndex != -1) {
        BudgetEditDialog(
            title = "LIMITE: ${budgetData[editingCategoryIndex].title.uppercase()}",
            initialValue = budgetAmountInput,
            onClose = { showEditCategoryDialog = false },
            onConfirm = { newValue ->
                val newLimit = newValue.toFloatOrNull() ?: budgetData[editingCategoryIndex].limit
                budgetData[editingCategoryIndex] = budgetData[editingCategoryIndex].copy(limit = newLimit)
                showEditCategoryDialog = false
            }
        )
    }
}

// ============================================================================
// VISTA 1: PANTALLA PRINCIPAL DE PRESUPUESTOS
// ============================================================================
@Composable
fun BudgetMainContent(
    onNavigateBack: () -> Unit,
    onGenerateReport: () -> Unit,
    spentMain: Float,
    mainBudgetLimit: Float,
    budgetData: List<BudgetCategoryData>,
    onEditMainBudget: () -> Unit,
    onEditCategoryBudget: (Int) -> Unit
) {
    val progressMain = if (mainBudgetLimit > 0) spentMain / mainBudgetLimit else 0f

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundSlate)
    ) {
        // HEADER AZUL
        Box(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
            }
            Text("PRESUPUESTOS", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            // TARJETA TOTAL PRESUPUESTADO
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL PRESUPUESTADO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$${"%.0f".format(mainBudgetLimit)}", fontSize = 40.sp, fontWeight = FontWeight.Black, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Gastado: ", fontSize = 14.sp, color = TextMuted)
                        Text("$${"%.0f".format(spentMain)}", fontSize = 14.sp, color = SecondaryRed, fontWeight = FontWeight.Bold)
                        Text(" / $${"%.0f".format(mainBudgetLimit)}", fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Editar",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp).clickable { onEditMainBudget() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LinearProgressIndicator(
                        progress = progressMain,
                        modifier = Modifier.fillMaxWidth().height(16.dp).clip(CircleShape),
                        color = SecondaryRed,
                        trackColor = BorderSlate.copy(0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MENSAJE DE INFORMACIÓN / AYUDA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PrimaryBlue.copy(0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("¿Cómo funciona el presupuesto?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                        Text(
                            "Las categorías se activan cuando tienes gastos o ingresos registrados. Edita los límites para controlar tus finanzas.",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TÍTULO Y BOTÓN DE REPORTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CATEGORÍAS ACTIVAS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextDark)

                Surface(
                    color = PrimaryBlue.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onGenerateReport() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.InsertDriveFile, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generar Reporte", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LISTA DE CATEGORÍAS
            budgetData.forEachIndexed { index, category ->
                BudgetCategoryCard(
                    category = category,
                    onEditClick = { onEditCategoryBudget(index) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================================
// VISTA 2: REPORTE AUTOMÁTICO
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportContent(onClose: () -> Unit) {
    val context = LocalContext.current
    var isDetailTab by remember { mutableStateOf(true) }

    var expandedPeriod by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Mensual") }
    val periods = listOf("Semanal", "Quincenal", "Mensual", "Bimestral", "Trimestral", "Semestral", "Anual")

    // --- LÓGICA DE MULTIPLICADOR SEGÚN EL PLAZO ---
    // Asumimos que los datos base son MENSUALES
    val multiplier = when (selectedPeriod) {
        "Semanal" -> 0.25f
        "Quincenal" -> 0.5f
        "Mensual" -> 1f
        "Bimestral" -> 2f
        "Trimestral" -> 3f
        "Semestral" -> 6f
        "Anual" -> 12f
        else -> 1f
    }

    // Datos base (Mensuales)
    val baseVivienda = 7000f
    val baseServicios = 900f
    val baseEntretenimiento = 250f
    val baseSalud = 600f

    // Datos Calculados en tiempo real
    val valVivienda = baseVivienda * multiplier
    val valServicios = baseServicios * multiplier
    val valEntretenimiento = baseEntretenimiento * multiplier
    val valSalud = baseSalud * multiplier

    val totalAmount = valVivienda + valServicios + valEntretenimiento + valSalud

    // Cálculo de Porcentajes (0.0 a 1.0)
    val pctVivienda = if (totalAmount > 0) valVivienda / totalAmount else 0f
    val pctServicios = if (totalAmount > 0) valServicios / totalAmount else 0f
    val pctEntretenimiento = if (totalAmount > 0) valEntretenimiento / totalAmount else 0f
    val pctSalud = if (totalAmount > 0) valSalud / totalAmount else 0f

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundSlate)
    ) {
        // HEADER AZUL
        Box(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
            Text("REPORTE AUTOMÁTICO", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            // TÍTULO DEL REPORTE
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChartOutline, null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Reporte de Gastos", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextDark)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp).background(Color.White, CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = TextDark, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TABS (Detalle / Gráfica Global)
            Row(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)).padding(4.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isDetailTab) BackgroundSlate else Color.Transparent).clickable { isDetailTab = true }, contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, null, tint = if(isDetailTab) TextDark else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detalle", color = if(isDetailTab) TextDark else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (!isDetailTab) BackgroundSlate else Color.Transparent).clickable { isDetailTab = false }, contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, null, tint = if(!isDetailTab) TextDark else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gráfica Global", color = if(!isDetailTab) TextDark else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CONTENEDOR BLANCO PRINCIPAL
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // DROPDOWN PLAZO
                    Text("PLAZO DEL REPORTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }) {
                        OutlinedTextField(
                            value = selectedPeriod, onValueChange = {}, readOnly = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                            shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                        )
                        ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                            periods.forEach { period ->
                                DropdownMenuItem(text = { Text(period) }, onClick = { selectedPeriod = period; expandedPeriod = false })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isDetailTab) {
                        // VISTA DETALLE DINÁMICA
                        ReportDetailCard(color = SecondaryRed, title = "VIVIENDA", total = "$${"%.0f".format(valVivienda)}", items = listOf("Renta" to "$${"%.0f".format(valVivienda)}"))
                        Spacer(modifier = Modifier.height(12.dp))
                        // Servicios divididos proporcionalmente
                        ReportDetailCard(color = Color(0xFF4A4453), title = "SERVICIOS", total = "$${"%.0f".format(valServicios)}", items = listOf("Internet" to "$${"%.0f".format(500 * multiplier)}", "Gas" to "$${"%.0f".format(400 * multiplier)}"))
                        Spacer(modifier = Modifier.height(12.dp))
                        ReportDetailCard(color = AccentGreen, title = "ENTRETENIMIENTO", total = "$${"%.0f".format(valEntretenimiento)}", items = listOf("Spotify" to "$${"%.0f".format(valEntretenimiento)}"))
                        Spacer(modifier = Modifier.height(12.dp))
                        ReportDetailCard(color = Color(0xFFF59E0B), title = "SALUD", total = "$${"%.0f".format(valSalud)}", items = listOf("Gimnasio" to "$${"%.0f".format(valSalud)}"))
                    } else {
                        // VISTA GRÁFICA DINÁMICA
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(160.dp)) {
                                // Dibujamos los arcos basados en los 360 grados del círculo
                                var startAngle = 270f // Empezar arriba
                                val strokeWidth = 40.dp.toPx()

                                val sweepVivienda = pctVivienda * 360f
                                drawArc(color = SecondaryRed, startAngle = startAngle, sweepAngle = sweepVivienda, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                                startAngle += sweepVivienda

                                val sweepServicios = pctServicios * 360f
                                drawArc(color = Color(0xFF4A4453), startAngle = startAngle, sweepAngle = sweepServicios, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                                startAngle += sweepServicios

                                val sweepEntretenimiento = pctEntretenimiento * 360f
                                drawArc(color = AccentGreen, startAngle = startAngle, sweepAngle = sweepEntretenimiento, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                                startAngle += sweepEntretenimiento

                                val sweepSalud = pctSalud * 360f
                                drawArc(color = Color(0xFFF59E0B), startAngle = startAngle, sweepAngle = sweepSalud, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("$${"%.0f".format(totalAmount)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // LEYENDA DINÁMICA
                        GraphLegendItem(color = SecondaryRed, title = "VIVIENDA", percentage = "(${"%.1f".format(pctVivienda * 100)}%)", amount = "$${"%.0f".format(valVivienda)}")
                        Spacer(modifier = Modifier.height(16.dp))
                        GraphLegendItem(color = Color(0xFF4A4453), title = "SERVICIOS", percentage = "(${"%.1f".format(pctServicios * 100)}%)", amount = "$${"%.0f".format(valServicios)}")
                        Spacer(modifier = Modifier.height(16.dp))
                        GraphLegendItem(color = AccentGreen, title = "ENTRETENIMIENTO", percentage = "(${"%.1f".format(pctEntretenimiento * 100)}%)", amount = "$${"%.0f".format(valEntretenimiento)}")
                        Spacer(modifier = Modifier.height(16.dp))
                        GraphLegendItem(color = Color(0xFFF59E0B), title = "SALUD", percentage = "(${"%.1f".format(pctSalud * 100)}%)", amount = "$${"%.0f".format(valSalud)}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Descargar */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Descargar PDF", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN DESCARGAR REPORTE (FUNCIONAL NATIVO)
            Button(
                onClick = {
                    Toast.makeText(context, "Generando PDF...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"), "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No hay app para abrir PDF", Toast.LENGTH_LONG).show() }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Descargar PDF", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

// ============================================================================
// COMPONENTES AUXILIARES Y DATA CLASSES
// ============================================================================

data class BudgetCategoryData(
    val title: String,
    val spent: Float,
    val limit: Float,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditDialog(title: String, initialValue: String, onClose: () -> Unit, onConfirm: (String) -> Unit) {
    var textInput by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextDark),
                    leadingIcon = { Text("$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "CANCELAR",
                        color = SecondaryRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onClose() }
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(
                        "ACEPTAR",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onConfirm(textInput) }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCategoryCard(category: BudgetCategoryData, onEditClick: () -> Unit) {
    val progress = if (category.limit > 0) category.spent / category.limit else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(category.title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$${"%.0f".format(category.spent)}", fontSize = 12.sp, color = SecondaryRed, fontWeight = FontWeight.Bold)
                    Text(" / $${"%.0f".format(category.limit)}", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Outlined.Edit, null, tint = TextMuted, modifier = Modifier.size(16.dp).clickable { onEditClick() })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(0.7f).height(8.dp).clip(CircleShape),
                color = category.color,
                trackColor = BorderSlate
            )
        }
    }
}

@Composable
fun ReportDetailCard(color: Color, title: String, total: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextDark, letterSpacing = 1.sp)
                }
                Text(total, fontWeight = FontWeight.Black, fontSize = 14.sp, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            items.forEach { (name, amount) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, fontSize = 12.sp, color = TextMuted)
                    if (name == "Gas") {
                        Surface(color = PrimaryBlue, shape = RoundedCornerShape(4.dp)) {
                            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    } else {
                        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun GraphLegendItem(color: Color, title: String, percentage: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextDark, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(percentage, fontSize = 12.sp, color = TextMuted)
        }
        Text(amount, fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextDark)
    }
}
package com.example.freeze_xpends.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.freeze_xpends.network.*
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class BudgetCategoryData(
    val id: Int,
    val title: String,
    val amount: Float,
    val limit: Float,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isGasto: Boolean
)

@Composable
fun BudgetScreen(
    userId: Int,
    isPremium: Boolean,
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    val budgetData = remember { mutableStateListOf<BudgetCategoryData>() }
    var mainBudgetLimit by remember { mutableStateOf(0f) }
    var spentMain by remember { mutableStateOf(0f) }

    var rawGastos by remember { mutableStateOf<List<Gasto>>(emptyList()) }
    var rawIngresos by remember { mutableStateOf<List<Ingreso>>(emptyList()) }

    var showReport by remember { mutableStateOf(false) }
    var showEditMainDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    var editingCategory by remember { mutableStateOf<BudgetCategoryData?>(null) }
    var budgetAmountInput by remember { mutableStateOf("") }

    val catColorsGastos = listOf(
        SecondaryRed,
        Color(0xFFE91E63),        // Rosa vibrante
        Color(0xFF9C27B0),        // Morado
        Color(0xFFFF9800),        // Naranja
        Color(0xFF795548),        // Café
        Color(0xFFF44336),        // Rojo Material
        Color(0xFF673AB7),        // Morado profundo
        Color(0xFFFF5722),        // Naranja profundo
        Color(0xFFD32F2F),        // Rojo oscuro
        Color(0xFFC2185B)         // Rosa oscuro
    )

    val catColorsIngresos = listOf(
        AccentGreen,
        PrimaryBlue,
        Color(0xFF009688),        // Turquesa (Teal)
        Color(0xFF8BC34A),        // Verde claro
        Color(0xFF03A9F4),        // Azul claro
        Color(0xFF4CAF50),        // Verde Material
        Color(0xFF00BCD4),        // Cyan
        Color(0xFF3F51B5),        // Indigo
        Color(0xFFCDDC39),        // Lima
        Color(0xFF00796B)         // Turquesa oscuro
    )
    val catIcons = listOf(Icons.Default.Home, Icons.Default.Settings, Icons.Default.Restaurant, Icons.Default.DirectionsCar, Icons.Default.Favorite, Icons.Default.Star)

    val currentCurrency by userViewModel.userCurrency.collectAsState()
    val userFormat by userViewModel.userFormat.collectAsState()
    val exchangeRate = when (currentCurrency) {
        "USD" -> 0.05f
        "EUR" -> 0.045f
        else -> 1.0f
    }

    val moneyFormatter = when (currentCurrency) {
        "USD" -> NumberFormat.getCurrencyInstance(Locale.US)
        "EUR" -> NumberFormat.getCurrencyInstance(Locale.FRANCE)
        else -> NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    }

    LaunchedEffect(refreshKey, currentCurrency) {
        isLoading = true
        try {
            val resPresupuesto = RetrofitClient.instance.getPresupuestoGlobal(userId)
            if (resPresupuesto.isSuccessful) mainBudgetLimit = (resPresupuesto.body()?.presupuesto_global?.toFloat() ?: 0f) * exchangeRate

            val resGastos = RetrofitClient.instance.getGastos(userId)
            // --- FILTRO: SÓLO TOMAR LOS GASTOS QUE YA ESTÁN PAGADOS ---
            rawGastos = resGastos.body()?.data?.filter { (it.completado ?: 0) == 1 } ?: emptyList()
            spentMain = rawGastos.sumOf { it.monto_gasto }.toFloat() * exchangeRate

            val resIngresos = RetrofitClient.instance.getIngresos(userId)
            // --- FILTRO: SÓLO TOMAR LOS INGRESOS QUE YA ESTÁN RECIBIDOS ---
            rawIngresos = resIngresos.body()?.data?.filter { (it.recibido ?: 1) == 1 } ?: emptyList()

            val resCatGastos = RetrofitClient.instance.getCategoriasGastos(userId)
            val listaCatGastos = resCatGastos.body()?.data ?: emptyList()

            val resCatIngresos = RetrofitClient.instance.getCategoriasIngresos(userId)
            val listaCatIngresos = resCatIngresos.body()?.data ?: emptyList()

            val tempBudgetData = mutableListOf<BudgetCategoryData>()

            listaCatGastos.forEachIndexed { index, cat ->
                val spentInCat = rawGastos.filter { it.categoria_id == cat.categoria_id }.sumOf { it.monto_gasto }.toFloat() * exchangeRate
                val limitInCat = (cat.limite_presupuesto?.toFloat() ?: 0f) * exchangeRate
                tempBudgetData.add(BudgetCategoryData(cat.categoria_id, cat.nombre_categoria, spentInCat, limitInCat, catColorsGastos[index % catColorsGastos.size], catIcons[index % catIcons.size], true))
            }

            listaCatIngresos.forEachIndexed { index, cat ->
                val receivedInCat = rawIngresos.filter { it.categoria_id == cat.categoria_id }.sumOf { it.monto }.toFloat() * exchangeRate
                tempBudgetData.add(BudgetCategoryData(cat.categoria_id, cat.nombre_categoria, receivedInCat, 0f, catColorsIngresos[index % catColorsIngresos.size], catIcons[(index+3) % catIcons.size], false))
            }

            budgetData.clear()
            budgetData.addAll(tempBudgetData)
        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando datos", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryBlue) }
    } else {
        if (showReport) {
            AdvancedReportContent(
                gastos = rawGastos, ingresos = rawIngresos, budgetData = budgetData, exchangeRate = exchangeRate, moneyFormatter = moneyFormatter, onClose = { showReport = false }
            )
        } else {
            BudgetMainContent(
                isPremium = isPremium, onNavigateBack = onNavigateBack, onNavigateToPremium = onNavigateToPremium,
                onGenerateReport = { showReport = true }, onAddCategory = { showAddCategoryDialog = true },
                spentMain = spentMain, mainBudgetLimit = mainBudgetLimit, budgetData = budgetData, moneyFormatter = moneyFormatter,
                onEditMainBudget = { budgetAmountInput = (mainBudgetLimit / exchangeRate).toInt().toString(); showEditMainDialog = true },
                onEditCategoryBudget = { cat -> editingCategory = cat; budgetAmountInput = (cat.limit / exchangeRate).toInt().toString(); showEditCategoryDialog = true }
            )
        }
    }

    if (showEditMainDialog) {
        BudgetEditDialog("LIMITE DE GASTO GLOBAL (MXN Base)", budgetAmountInput, { showEditMainDialog = false }) { newValue ->
            val newLimit = newValue.toDoubleOrNull() ?: (mainBudgetLimit / exchangeRate).toDouble()
            scope.launch {
                try {
                    val res = RetrofitClient.instance.updatePresupuestoGlobal(userId, PresupuestoGlobalRequest(newLimit))
                    if (res.isSuccessful) refreshKey++
                } catch (e: Exception) { Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show() }
            }
            showEditMainDialog = false
        }
    }

    if (showEditCategoryDialog && editingCategory != null) {
        BudgetEditDialog("LIMITE: ${editingCategory!!.title.uppercase()} (MXN Base)", budgetAmountInput, { showEditCategoryDialog = false }) { newValue ->
            val newLimit = newValue.toDoubleOrNull() ?: (editingCategory!!.limit / exchangeRate).toDouble()
            scope.launch {
                try {
                    val res = RetrofitClient.instance.updateLimiteCategoria(editingCategory!!.id, LimiteCategoriaRequest(newLimit))
                    if (res.isSuccessful) refreshKey++
                } catch (e: Exception) { Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show() }
            }
            showEditCategoryDialog = false
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(userId = userId, onClose = { showAddCategoryDialog = false }, onSuccess = { showAddCategoryDialog = false; refreshKey++ })
    }
}

// ============================================================================
// VISTA 1: PANTALLA PRINCIPAL
// ============================================================================
@Composable
fun BudgetMainContent(
    isPremium: Boolean, onNavigateBack: () -> Unit, onNavigateToPremium: () -> Unit,
    onGenerateReport: () -> Unit, onAddCategory: () -> Unit,
    spentMain: Float, mainBudgetLimit: Float, budgetData: List<BudgetCategoryData>, moneyFormatter: NumberFormat,
    onEditMainBudget: () -> Unit, onEditCategoryBudget: (BudgetCategoryData) -> Unit
) {
    val progressMain = if (mainBudgetLimit > 0) spentMain / mainBudgetLimit else 0f

    Column(modifier = Modifier.fillMaxSize().background(BackgroundSlate)) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
            Text("PRESUPUESTOS Y REPORTES", modifier = Modifier.fillMaxWidth().padding(end = 48.dp), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL PRESUPUESTADO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(moneyFormatter.format(mainBudgetLimit), fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Gastado: ", fontSize = 14.sp, color = TextMuted)
                        Text(moneyFormatter.format(spentMain), fontSize = 14.sp, color = SecondaryRed, fontWeight = FontWeight.Bold)
                        Text(" / ${moneyFormatter.format(mainBudgetLimit)}", fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Edit, null, tint = TextMuted, modifier = Modifier.size(16.dp).clickable { onEditMainBudget() })
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    LinearProgressIndicator(progress = progressMain.coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth().height(16.dp).clip(CircleShape), color = SecondaryRed, trackColor = BorderSlate.copy(0.5f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onGenerateReport, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.InsertDriveFile, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Reporte", fontWeight = FontWeight.Bold)
                }
                Button(onClick = { if (isPremium) onAddCategory() else onNavigateToPremium() }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isPremium) AccentGreen else BackgroundSlate), border = if (!isPremium) BorderStroke(1.dp, BorderSlate) else null, shape = RoundedCornerShape(12.dp)) {
                    if (isPremium) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Categoría", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(Icons.Default.Lock, null, tint = SecondaryRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Categoría", fontWeight = FontWeight.Bold, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            val catsGastos = budgetData.filter { it.isGasto }
            val catsIngresos = budgetData.filter { !it.isGasto }

            Text("CATEGORÍAS DE GASTOS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))
            catsGastos.forEach { category ->
                BudgetCategoryCard(category = category, moneyFormatter = moneyFormatter, onEditClick = { onEditCategoryBudget(category) })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("CATEGORÍAS DE INGRESOS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))
            if (catsIngresos.isEmpty()) {
                Text("No tienes categorías de ingresos.", color = TextMuted, fontSize = 12.sp)
            } else {
                catsIngresos.forEach { category ->
                    BudgetCategoryCard(category = category, moneyFormatter = moneyFormatter, onEditClick = {})
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ============================================================================
// VISTA 2: REPORTE AVANZADO
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedReportContent(gastos: List<Gasto>, ingresos: List<Ingreso>, budgetData: List<BudgetCategoryData>, exchangeRate: Float, moneyFormatter: NumberFormat, onClose: () -> Unit) {
    var isDetailTab by remember { mutableStateOf(true) }

    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var selectedPeriod by remember { mutableStateOf("Mensual") }
    var expandedPeriod by remember { mutableStateOf(false) }
    val periods = listOf("Mensual", "Bimestral", "Trimestral", "Semestral", "Anual")

    val startYearMonth = YearMonth.of(selectedYear, selectedMonth)
    val monthsToAdd = when(selectedPeriod) { "Bimestral"->2L; "Trimestral"->3L; "Semestral"->6L; "Anual"->12L; else->1L }
    val endYearMonth = startYearMonth.plusMonths(monthsToAdd - 1)
    val startDate = startYearMonth.atDay(1)
    val endDate = endYearMonth.atEndOfMonth()

    val filteredGastos = gastos.filter { try { val d = LocalDate.parse(it.fecha_gasto.substringBefore("T")); d in startDate..endDate } catch (e: Exception) { false } }
    val filteredIngresos = ingresos.filter { try { val d = LocalDate.parse(it.fecha_ingreso.substringBefore("T")); d in startDate..endDate } catch (e: Exception) { false } }

    val totalGasto = filteredGastos.sumOf { it.monto_gasto }.toFloat() * exchangeRate
    val totalIngreso = filteredIngresos.sumOf { it.monto }.toFloat() * exchangeRate
    val balance = totalIngreso - totalGasto

    Column(modifier = Modifier.fillMaxSize().background(BackgroundSlate)) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
            Text("REPORTE FINANCIERO", modifier = Modifier.fillMaxWidth().padding(end = 48.dp), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }, modifier = Modifier.weight(1.5f)) {
                    OutlinedTextField(value = selectedPeriod, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, unfocusedBorderColor = BorderSlate))
                    ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                        periods.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { selectedPeriod = p; expandedPeriod = false }) }
                    }
                }
                Row(modifier = Modifier.weight(1f).height(56.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { if(selectedMonth == 1) { selectedMonth = 12; selectedYear-- } else { selectedMonth-- } }) { Icon(Icons.Default.ChevronLeft, null) }
                    Text("${selectedMonth}/${selectedYear.toString().takeLast(2)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    IconButton(onClick = { if(selectedMonth == 12) { selectedMonth = 1; selectedYear++ } else { selectedMonth++ } }) { Icon(Icons.Default.ChevronRight, null) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Período: ${startDate.dayOfMonth} ${startDate.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))} - ${endDate.dayOfMonth} ${endDate.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))} ${endDate.year}", fontSize = 12.sp, color = TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)).padding(4.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isDetailTab) BackgroundSlate else Color.Transparent).clickable { isDetailTab = true }, contentAlignment = Alignment.Center) { Text("Balance", color = if(isDetailTab) TextDark else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (!isDetailTab) BackgroundSlate else Color.Transparent).clickable { isDetailTab = false }, contentAlignment = Alignment.Center) { Text("Gráficas", color = if(!isDetailTab) TextDark else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(24.dp)) {

                    if (isDetailTab) {
                        Text("BALANCE DEL PERIODO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(moneyFormatter.format(balance), fontSize = 32.sp, fontWeight = FontWeight.Black, color = if(balance >= 0) AccentGreen else SecondaryRed)
                        Spacer(modifier = Modifier.height(24.dp))

                        ReportDetailCard(color = AccentGreen, title = "INGRESOS TOTALES", total = moneyFormatter.format(totalIngreso))
                        Spacer(modifier = Modifier.height(12.dp))
                        ReportDetailCard(color = SecondaryRed, title = "GASTOS TOTALES", total = moneyFormatter.format(totalGasto))
                    } else {
                        // --- GRÁFICA DE INGRESOS ---
                        Text("DISTRIBUCIÓN DE INGRESOS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextDark)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (totalIngreso == 0f) {
                            Text("No hay ingresos en este periodo.", color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(120.dp)) {
                                    var startAngle = 270f
                                    val strokeWidth = 30.dp.toPx()
                                    budgetData.filter { !it.isGasto }.forEach { category ->
                                        val amountInPeriod = filteredIngresos.filter { it.categoria_id == category.id }.sumOf { it.monto }.toFloat() * exchangeRate
                                        if (amountInPeriod > 0) {
                                            val sweepAngle = (amountInPeriod / totalIngreso) * 360f
                                            drawArc(color = category.color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth))
                                            startAngle += sweepAngle
                                        }
                                    }
                                }
                                Text(moneyFormatter.format(totalIngreso), fontSize = 16.sp, fontWeight = FontWeight.Black, color = AccentGreen)
                            }
                            budgetData.filter { !it.isGasto }.forEach { category ->
                                val amountInPeriod = filteredIngresos.filter { it.categoria_id == category.id }.sumOf { it.monto }.toFloat() * exchangeRate
                                if (amountInPeriod > 0) {
                                    val pct = (amountInPeriod / totalIngreso) * 100
                                    GraphLegendItem(color = category.color, title = category.title.uppercase(), percentage = "(${"%.1f".format(pct)}%)", amount = moneyFormatter.format(amountInPeriod))
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(color = BorderSlate)
                        Spacer(modifier = Modifier.height(32.dp))

                        // --- GRÁFICA DE GASTOS ---
                        Text("DISTRIBUCIÓN DE GASTOS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextDark)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (totalGasto == 0f) {
                            Text("No hay gastos en este periodo.", color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(120.dp)) {
                                    var startAngle = 270f
                                    val strokeWidth = 30.dp.toPx()
                                    budgetData.filter { it.isGasto }.forEach { category ->
                                        val spentInPeriod = filteredGastos.filter { it.categoria_id == category.id }.sumOf { it.monto_gasto }.toFloat() * exchangeRate
                                        if (spentInPeriod > 0) {
                                            val sweepAngle = (spentInPeriod / totalGasto) * 360f
                                            drawArc(color = category.color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth))
                                            startAngle += sweepAngle
                                        }
                                    }
                                }
                                Text(moneyFormatter.format(totalGasto), fontSize = 16.sp, fontWeight = FontWeight.Black, color = SecondaryRed)
                            }
                            budgetData.filter { it.isGasto }.forEach { category ->
                                val spentInPeriod = filteredGastos.filter { it.categoria_id == category.id }.sumOf { it.monto_gasto }.toFloat() * exchangeRate
                                if (spentInPeriod > 0) {
                                    val pct = (spentInPeriod / totalGasto) * 100
                                    GraphLegendItem(color = category.color, title = category.title.uppercase(), percentage = "(${"%.1f".format(pct)}%)", amount = moneyFormatter.format(spentInPeriod))
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ============================================================================
// DIÁLOGO: CREAR CATEGORÍA
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(userId: Int, onClose: () -> Unit, onSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var isGasto by remember { mutableStateOf(true) }
    var catName by remember { mutableStateOf("") }
    var catLimit by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onClose) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NUEVA CATEGORÍA", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextDark)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth().height(40.dp).background(BackgroundSlate, RoundedCornerShape(8.dp)).padding(2.dp)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (isGasto) SecondaryRed else Color.Transparent).clickable { isGasto = true }, contentAlignment = Alignment.Center) { Text("Gasto", color = if(isGasto) Color.White else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(if (!isGasto) AccentGreen else Color.Transparent).clickable { isGasto = false }, contentAlignment = Alignment.Center) { Text("Ingreso", color = if(!isGasto) Color.White else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Nombre de categoría") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))

                if (isGasto) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = catLimit, onValueChange = { catLimit = it }, label = { Text("Límite de presupuesto (Opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("CANCELAR", color = SecondaryRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onClose() })
                    Spacer(modifier = Modifier.width(32.dp))

                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryBlue)
                    else Text("GUARDAR", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                        if (catName.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                try {
                                    val res = if (isGasto) RetrofitClient.instance.addCategoriaGasto(NuevaCategoriaGastoRequest(userId, catName, catLimit.toDoubleOrNull() ?: 0.0))
                                    else RetrofitClient.instance.addCategoriaIngreso(NuevaCategoriaIngresoRequest(userId, catName))
                                    if (res.isSuccessful) { Toast.makeText(context, "Categoría creada", Toast.LENGTH_SHORT).show(); onSuccess() }
                                } catch (e: Exception) { Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show() }
                                finally { isLoading = false }
                            }
                        }
                    })
                }
            }
        }
    }
}

// ============================================================================
// COMPONENTES AUXILIARES
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditDialog(title: String, initialValue: String, onClose: () -> Unit, onConfirm: (String) -> Unit) {
    var textInput by remember { mutableStateOf(initialValue) }
    Dialog(onDismissRequest = onClose) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = textInput, onValueChange = { textInput = it }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextDark), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue))
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("CANCELAR", color = SecondaryRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onClose() })
                    Spacer(modifier = Modifier.width(32.dp))
                    Text("ACEPTAR", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onConfirm(textInput) })
                }
            }
        }
    }
}

@Composable
fun BudgetCategoryCard(category: BudgetCategoryData, moneyFormatter: NumberFormat, onEditClick: () -> Unit) {
    val progress = if (category.isGasto && category.limit > 0) category.amount / category.limit else 0f

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, BorderSlate)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(category.title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (category.isGasto) {
                        Text(moneyFormatter.format(category.amount), fontSize = 12.sp, color = SecondaryRed, fontWeight = FontWeight.Bold)
                        Text(" / ${moneyFormatter.format(category.limit)}", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Edit, null, tint = TextMuted, modifier = Modifier.size(16.dp).clickable { onEditClick() })
                    } else {
                        Text(moneyFormatter.format(category.amount), fontSize = 14.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (category.isGasto) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(progress = progress.coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = category.color, trackColor = BorderSlate)
            }
        }
    }
}

@Composable
fun ReportDetailCard(color: Color, title: String, total: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, BorderSlate)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextDark, letterSpacing = 1.sp)
                }
                Text(total, fontWeight = FontWeight.Black, fontSize = 14.sp, color = color)
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
package com.example.freeze_xpends.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.network.ActualizarNota
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.network.EstatusGastoRequest
import com.example.freeze_xpends.network.EstatusIngresoRequest
import com.example.freeze_xpends.network.Nota
import com.example.freeze_xpends.network.NuevaNota
import com.example.freeze_xpends.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class DailyExpenseData(val transactions: Int, val totalAmount: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userId: Int,
    isPremium: Boolean,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var isLoading by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    var transactionToEdit by remember { mutableStateOf<TransaccionItem?>(null) }

    var allTransactionsMonth by remember { mutableStateOf<List<TransaccionItem>>(emptyList()) }
    var monthlyData by remember { mutableStateOf<Map<LocalDate, DailyExpenseData>>(emptyMap()) }

    val moneyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    var notas by remember { mutableStateOf<List<Nota>>(emptyList()) }

    // --- ESTADO PARA LAS NOTAS DEL MES ---
    var notasDelMes by remember { mutableStateOf<List<Nota>>(emptyList()) }

    var textoNuevaNota by remember { mutableStateOf("") }
    var cargandoNotas by remember { mutableStateOf(false) }
    var recargarNotas by remember { mutableStateOf(0) }
    var notaEnEdicion by remember { mutableStateOf<Nota?>(null) }

    LaunchedEffect(selectedDate, recargarNotas) {
        if (selectedDate == null) return@LaunchedEffect
        cargandoNotas = true
        try {
            val fechaStr = selectedDate.toString()
            val response = RetrofitClient.instance.getNotas(userId, fechaStr)
            if (response.isSuccessful) {
                notas = response.body()?.data ?: emptyList()
            }
        } catch(e: Exception) {
            Toast.makeText(context, "Error cargando notas: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cargandoNotas = false
        }
    }

    LaunchedEffect(currentYearMonth, refreshKey, recargarNotas) {
        isLoading = true
        try {
            val responseGastos = RetrofitClient.instance.getGastos(userId)
            val listaGastos = responseGastos.body()?.data ?: emptyList()

            val responseIngresos = RetrofitClient.instance.getIngresos(userId)
            val listaIngresos = responseIngresos.body()?.data ?: emptyList()

            // OBTENER NOTAS DEL MES ENTERO
            val responseNotasMes = RetrofitClient.instance.getNotasPorMes(userId, currentYearMonth.toString())
            if (responseNotasMes.isSuccessful) {
                notasDelMes = responseNotasMes.body()?.data ?: emptyList()
            }

            val tempTransactions = mutableListOf<TransaccionItem>()
            val startOfMonth = currentYearMonth.atDay(1)
            val endOfMonth = currentYearMonth.atEndOfMonth()

            fun proyectarTransacciones(
                idReal: Int, isGasto: Boolean, titulo: String, categoria: String,
                monto: Double, fechaInicioStr: String, frecuencia: String,
                isCompletedOriginal: Boolean, statusOriginal: String
            ) {
                try {
                    val fechaInicio = LocalDate.parse(fechaInicioStr)
                    var currentDate = fechaInicio
                    val freq = frecuencia.uppercase()

                    fun agregarSiPertenece() {
                        if (YearMonth.from(currentDate) == currentYearMonth) {
                            val esElOriginal = currentDate == fechaInicio
                            tempTransactions.add(
                                TransaccionItem(
                                    id = idReal,
                                    isGasto = isGasto,
                                    titulo = titulo,
                                    categoria = categoria,
                                    monto = monto,
                                    fecha = currentDate.toString(),
                                    frecuencia = frecuencia,
                                    status = if (esElOriginal) statusOriginal else if (isGasto) "PENDIENTE" else "NO RECIBIDO",
                                    isCompleted = if (esElOriginal) isCompletedOriginal else false
                                )
                            )
                        }
                    }

                    when (freq) {
                        "ÚNICO", "UNICO" -> {
                            if (YearMonth.from(fechaInicio) == currentYearMonth) agregarSiPertenece()
                        }
                        "SEMANAL" -> {
                            while (currentDate.isBefore(startOfMonth)) currentDate = currentDate.plusDays(7)
                            while (!currentDate.isAfter(endOfMonth)) { agregarSiPertenece(); currentDate = currentDate.plusDays(7) }
                        }
                        "QUINCENAL" -> {
                            while (currentDate.isBefore(startOfMonth)) currentDate = currentDate.plusDays(14)
                            while (!currentDate.isAfter(endOfMonth)) { agregarSiPertenece(); currentDate = currentDate.plusDays(14) }
                        }
                        "MENSUAL" -> {
                            while (currentDate.isBefore(startOfMonth)) currentDate = currentDate.plusMonths(1)
                            while (!currentDate.isAfter(endOfMonth)) { agregarSiPertenece(); currentDate = currentDate.plusMonths(1) }
                        }
                        "ANUAL" -> {
                            while (currentDate.isBefore(startOfMonth)) currentDate = currentDate.plusYears(1)
                            while (!currentDate.isAfter(endOfMonth)) { agregarSiPertenece(); currentDate = currentDate.plusYears(1) }
                        }
                        else -> {
                            if (YearMonth.from(fechaInicio) == currentYearMonth) agregarSiPertenece()
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            listaGastos.forEach {
                val isCompleted = it.completado == 1
                proyectarTransacciones(
                    idReal = it.gasto_id, isGasto = true, titulo = it.nombre_gasto,
                    categoria = "Gasto", monto = it.monto_gasto, fechaInicioStr = it.fecha_gasto.substringBefore("T"),
                    frecuencia = it.plazo ?: "ÚNICO", isCompletedOriginal = isCompleted, statusOriginal = if (isCompleted) "PAGADO" else "PENDIENTE"
                )
            }

            listaIngresos.forEach {
                val isCompleted = it.recibido == 1
                proyectarTransacciones(
                    idReal = it.ingreso_id, isGasto = false, titulo = it.nombre_ingreso,
                    categoria = it.nombre_categoria ?: "Ingreso", monto = it.monto, fechaInicioStr = it.fecha_ingreso.substringBefore("T"),
                    frecuencia = it.plazo ?: "ÚNICO", isCompletedOriginal = isCompleted, statusOriginal = if (isCompleted) "RECIBIDO" else "NO RECIBIDO"
                )
            }

            allTransactionsMonth = tempTransactions.sortedByDescending { it.fecha }

            val grouped = allTransactionsMonth.groupBy { LocalDate.parse(it.fecha) }
            monthlyData = grouped.mapValues { entry ->
                DailyExpenseData(entry.value.size, entry.value.sumOf { if (it.isGasto) -it.monto else it.monto })
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = BackgroundSlate,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue), contentAlignment = Alignment.CenterStart) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("Calendario de Movimientos", modifier = Modifier.padding(start = 48.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, null) }
                        Text("${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")).replaceFirstChar { it.uppercase() }} ${currentYearMonth.year}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, null) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val daysOfWeek = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        daysOfWeek.forEach { day ->
                            Text(text = day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val firstDay = currentYearMonth.atDay(1)
                    val offset = if (firstDay.dayOfWeek.value == 7) 0 else firstDay.dayOfWeek.value
                    val daysInMonth = currentYearMonth.lengthOfMonth()
                    val rows = if ((offset + daysInMonth) % 7 == 0) (offset + daysInMonth) / 7 else ((offset + daysInMonth) / 7) + 1

                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height((rows * 62).dp), userScrollEnabled = false) {
                        items(offset) { Box(modifier = Modifier.size(40.dp)) }
                        items(daysInMonth) { i ->
                            val day = i + 1
                            val date = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, day)
                            val data = monthlyData[date]

                            CalendarDayCell(
                                dayNumber = day.toString(),
                                expenseData = data,
                                onClick = {
                                    selectedDate = date
                                    transactionToEdit = null
                                    notas = emptyList()
                                    textoNuevaNota = ""
                                    notaEnEdicion = null
                                    showBottomSheet = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    val monthText = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
                    Text("Resumen de $monthText", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryRow("Transacciones", allTransactionsMonth.size.toString(), false)
                    SummaryRow("Días activos", monthlyData.size.toString(), false)

                    val totalGasto = allTransactionsMonth.filter { it.isGasto }.sumOf { it.monto }
                    val prom = if(monthlyData.isNotEmpty()) totalGasto / monthlyData.size else 0.0
                    SummaryRow("Gasto Promedio/Día", moneyFormatter.format(prom), true)
                }
            }

            // --- MEJORA: LISTADO DE NOTAS DEL MES CON FECHA COMPLETA Y ORDENADAS ---
            if (notasDelMes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("NOTAS DEL MES", fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Ordenamos las notas por fecha para que se lean como una línea del tiempo
                        notasDelMes.sortedBy { it.fecha }.forEach { nota ->
                            val dateObj = try { LocalDate.parse(nota.fecha.substringBefore("T")) } catch(e:Exception) { null }

                            val dayNumber = dateObj?.dayOfMonth?.toString() ?: ""
                            val dayName = dateObj?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))?.replaceFirstChar { it.uppercase() } ?: ""
                            val monthName = dateObj?.month?.getDisplayName(TextStyle.FULL, Locale("es", "MX"))?.replaceFirstChar { it.uppercase() } ?: ""

                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Columna de la fecha (Día de la semana + Número en círculo)
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                                    Text(dayName, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Box(modifier = Modifier.size(36.dp).background(PrimaryBlue.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Text(dayNumber, fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                // Columna del texto y la fecha descriptiva
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nota.texto, fontSize = 14.sp, color = TextDark)
                                    if (dateObj != null) {
                                        Text("$dayNumber de $monthName", fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET (DETALLES Y NOTAS) ---
    if (showBottomSheet && selectedDate != null) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false; transactionToEdit = null }, sheetState = sheetState, containerColor = Color.White) {
            if (transactionToEdit == null) {
                val transactionsOfDay = allTransactionsMonth.filter { LocalDate.parse(it.fecha) == selectedDate }

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text(
                        text = "Movimientos del ${selectedDate!!.dayOfMonth} de ${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale("es","MX"))}",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark
                    )

                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        items(transactionsOfDay) { t ->
                            val sign = if (t.isGasto) "-" else "+"
                            val color = if (t.isGasto) SecondaryRed else AccentGreen

                            TransactionCard(
                                title = t.titulo,
                                category = t.categoria,
                                freq = t.frecuencia,
                                amount = "$sign${moneyFormatter.format(t.monto)}",
                                date = t.fecha,
                                icon = if(t.isGasto) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                iconColor = color,
                                status = t.status,
                                isCompleted = t.isCompleted,
                                onToggleClick = {
                                    scope.launch {
                                        try {
                                            val nuevoEstado = if (t.isCompleted) 0 else 1
                                            if (t.isGasto) {
                                                val res = RetrofitClient.instance.updateEstatusGasto(t.id, EstatusGastoRequest(nuevoEstado))
                                                if (res.isSuccessful) refreshKey++
                                            } else {
                                                val res = RetrofitClient.instance.updateEstatusIngreso(t.id, EstatusIngresoRequest(nuevoEstado))
                                                if (res.isSuccessful) refreshKey++
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al actualizar estatus: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                onEditClick = { transactionToEdit = t }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = BorderSlate)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "📝 NOTAS DEL DÍA",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textoNuevaNota,
                            onValueChange = { textoNuevaNota = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(if (notaEnEdicion != null) "Editando nota..." else "Escribe una nota...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BorderSlate,
                                focusedBorderColor = PrimaryBlue
                            )
                        )

                        if (notaEnEdicion != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    notaEnEdicion = null
                                    textoNuevaNota = ""
                                },
                                modifier = Modifier.background(BorderSlate, RoundedCornerShape(12.dp)).size(56.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextDark)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textoNuevaNota.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            if (notaEnEdicion != null) {
                                                val req = ActualizarNota(textoNuevaNota)
                                                val res = RetrofitClient.instance.updateNota(notaEnEdicion!!.nota_id, req)
                                                if (res.isSuccessful) {
                                                    textoNuevaNota = ""
                                                    notaEnEdicion = null
                                                    recargarNotas++
                                                    Toast.makeText(context, "Nota editada ✔", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Error al editar (${res.code()})", Toast.LENGTH_LONG).show()
                                                }
                                            } else {
                                                val req = NuevaNota(userId, selectedDate.toString(), textoNuevaNota)
                                                val res = RetrofitClient.instance.addNota(req)
                                                if (res.isSuccessful) {
                                                    textoNuevaNota = ""
                                                    recargarNotas++
                                                    Toast.makeText(context, "Nota guardada ✔", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Error al guardar (${res.code()})", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.background(if (notaEnEdicion != null) AccentGreen else PrimaryBlue, RoundedCornerShape(12.dp)).size(56.dp)
                        ) {
                            Icon(if (notaEnEdicion != null) Icons.Default.Check else Icons.Default.Add, contentDescription = "Guardar", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (cargandoNotas) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryBlue)
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                            notas.forEach { nota ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(modifier = Modifier.size(6.dp).background(if (notaEnEdicion?.nota_id == nota.nota_id) AccentGreen else TextMuted, CircleShape))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(nota.texto, fontSize = 14.sp, color = TextDark)
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                notaEnEdicion = nota
                                                textoNuevaNota = nota.texto
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        val res = RetrofitClient.instance.deleteNota(nota.nota_id)
                                                        if (res.isSuccessful) recargarNotas++
                                                    } catch (e: Exception) { }
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = SecondaryRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                EditTransactionContent(
                    isPremium = isPremium,
                    isExpense = transactionToEdit!!.isGasto,
                    transaccion = transactionToEdit,
                    userId = userId,
                    onClose = { transactionToEdit = null },
                    onNavigateToPremium = { showBottomSheet = false; onNavigate("premium") },
                    onSaveSuccess = { showBottomSheet = false; transactionToEdit = null; refreshKey++ },
                    onDeleteSuccess = { showBottomSheet = false; transactionToEdit = null; refreshKey++ }
                )
            }
        }
    }
}

@Composable
fun CalendarDayCell(dayNumber: String, expenseData: DailyExpenseData?, onClick: () -> Unit) {
    Box(
        modifier = Modifier.height(60.dp).padding(2.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        contentAlignment = Alignment.TopCenter
    ) {
        if (expenseData != null) {
            val color = if (expenseData.totalAmount >= 0) AccentGreen else SecondaryRed
            Column(modifier = Modifier.fillMaxSize().background(color).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                Text(dayNumber, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text("${expenseData.transactions}\nmov.", fontSize = 8.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 9.sp)
            }
        } else {
            Box(modifier = Modifier.size(40.dp).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(dayNumber, color = TextDark, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = TextMuted)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isHighlight) PrimaryBlue else TextDark)
    }
}
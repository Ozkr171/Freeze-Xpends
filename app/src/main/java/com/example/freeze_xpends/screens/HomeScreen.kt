package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
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
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// --- CLASE AUXILIAR PARA JUNTAR GASTOS E INGRESOS EN LA LISTA ---
data class TransaccionItem(
    val id: Int,
    val isGasto: Boolean,
    val titulo: String,
    val categoria: String,
    val monto: Double,
    val fecha: String,
    val frecuencia: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPremium: Boolean,
    userName: String,
    userId: Int, // <-- NECESITAMOS EL ID PARA TRAER LOS DATOS
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var transactionTypeExpense by remember { mutableStateOf(false) }

    // --- ESTADOS PARA LOS DATOS REALES ---
    var transacciones by remember { mutableStateOf<List<TransaccionItem>>(emptyList()) }
    var totalIngresos by remember { mutableStateOf(0.0) }
    var totalGastos by remember { mutableStateOf(0.0) }
    var balanceTotal by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }

    // Formateador de dinero (ej. $1,000.00)
    val moneyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    // --- CARGAR DATOS DESDE AIVEN ---
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 1. Traer Gastos
            val responseGastos = RetrofitClient.instance.getGastos(userId)
            val listaGastos = responseGastos.body()?.data ?: emptyList()

            // 2. Traer Ingresos
            val responseIngresos = RetrofitClient.instance.getIngresos(userId)
            val listaIngresos = responseIngresos.body()?.data ?: emptyList()

            // 3. Hacer los cálculos
            val sumaGastos = listaGastos.sumOf { it.monto_gasto }
            val sumaIngresos = listaIngresos.sumOf { it.monto }

            totalGastos = sumaGastos
            totalIngresos = sumaIngresos
            balanceTotal = sumaIngresos - sumaGastos

            // 4. Transformar y juntar las listas para pintarlas
            val itemsGastos = listaGastos.map {
                TransaccionItem(
                    id = it.gasto_id, isGasto = true, titulo = it.nombre_gasto,
                    categoria = "Gasto", // Fase 2: Conectar nombre de categoría real
                    monto = it.monto_gasto, fecha = it.fecha_gasto.substringBefore("T"),
                    frecuencia = it.plazo ?: "ÚNICO", status = "PAGADO"
                )
            }
            val itemsIngresos = listaIngresos.map {
                TransaccionItem(
                    id = it.ingreso_id, isGasto = false, titulo = it.nombre_ingreso,
                    categoria = it.nombre_categoria ?: "Ingreso",
                    monto = it.monto, fecha = it.fecha_ingreso.substringBefore("T"),
                    frecuencia = "ÚNICO", status = if(it.recibido == 1) "RECIBIDO" else "PENDIENTE"
                )
            }

            // Unimos todo y lo ordenamos por fecha (del más nuevo al más viejo)
            transacciones = (itemsGastos + itemsIngresos).sortedByDescending { it.fecha }

        } catch (e: Exception) {
            println("Error al cargar datos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = { onNavigate("add-expense") },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryRed),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(60.dp).padding(bottom = 8.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
                Text(" Nuevo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        containerColor = BackgroundSlate
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // --- 1. HEADER AZUL DINÁMICO ---
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(PrimaryBlue).padding(24.dp)
                ) {
                    if (!isPremium) {
                        Surface(color = SecondaryRed, shape = RoundedCornerShape(bottomStart = 8.dp), modifier = Modifier.align(Alignment.TopEnd).offset(x = 24.dp, y = (-24).dp)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Text(" GRATIS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column {
                        Text(text = "¡Qué onda, ${userName.split(" ")[0]}!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("BALANCE TOTAL", color = Color.White.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(moneyFormatter.format(balanceTotal), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if(userName.isNotBlank()) userName.take(2).uppercase() + " " else "XX ", color = Color.White, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onNavigate("settings") }, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).size(40.dp)) {
                                    Icon(Icons.Default.Settings, null, tint = PrimaryBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard("Ingresos", moneyFormatter.format(totalIngresos), Icons.Default.TrendingUp, modifier = Modifier.weight(1f))
                            SummaryCard("Gastos", moneyFormatter.format(totalGastos), Icons.Default.TrendingDown, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // --- 2. ANUNCIO (Solo si no es Premium) ---
            if (!isPremium) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ANUNCIO PUBLICITARIO", fontSize = 10.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("Espacio para Banner 320x50", color = TextMuted.copy(0.5f))
                        }
                        TextButton(onClick = { onNavigate("premium") }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, null, tint = SecondaryRed, modifier = Modifier.size(16.dp))
                                Text(" Quitar anuncios", color = SecondaryRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- 3. TÍTULO TRANSACCIONES ---
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Transacciones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }

            // --- 4. LISTA DE MOVIMIENTOS ---
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            } else if (transacciones.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(100.dp).background(BorderSlate.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ReceiptLong, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Aún no hay movimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "Toca el botón + para registrar tu\nprimer ingreso o gasto.", fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(transacciones) { t ->
                    val sign = if (t.isGasto) "-" else "+"
                    val moneyText = "$sign${moneyFormatter.format(t.monto)}"
                    val iconColor = if (t.isGasto) SecondaryRed else AccentGreen
                    val iconImage = if (t.isGasto) Icons.Default.TrendingDown else Icons.Default.TrendingUp

                    TransactionCard(
                        title = t.titulo, category = t.categoria, freq = t.frecuencia, amount = moneyText,
                        date = t.fecha, status = t.status, icon = iconImage, iconColor = iconColor,
                        onEditClick = { transactionTypeExpense = t.isGasto; showBottomSheet = true }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Modal (Se queda igual por ahora)
    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, containerColor = Color.White) {
            EditTransactionContent(isPremium, transactionTypeExpense, onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false } }, onNavigateToPremium = { showBottomSheet = false; onNavigate("premium") }, onSaveClick = {}, onDeleteClick = {})
        }
    }
}

@Composable
fun SummaryCard(label: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(0.1f), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(label, color = Color.White.copy(0.8f), fontSize = 12.sp)
            Text(amount, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun TransactionCard(title: String, category: String, freq: String, amount: String, date: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(iconColor.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = BackgroundSlate, shape = RoundedCornerShape(4.dp)) {
                            Text(category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = TextMuted)
                        }
                        Text(" $freq", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(amount, fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (amount.startsWith("+")) AccentGreen else TextDark)
                    Text(date, fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}
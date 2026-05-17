package com.example.freeze_xpends.screens

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.network.EstatusGastoRequest
import com.example.freeze_xpends.network.EstatusIngresoRequest
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

data class TransaccionItem(
    val id: Int,
    val isGasto: Boolean,
    val titulo: String,
    val categoria: String,
    val monto: Double,
    val fecha: String,
    val frecuencia: String,
    val status: String,
    val isCompleted: Boolean,
    val imagen_uri: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPremium: Boolean,
    userName: String,
    userId: Int,
    userViewModel: UserViewModel, // <-- PASADO PARA ESCUCHAR LA DIVISA
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var transactionTypeExpense by remember { mutableStateOf(false) }

    var selectedTransaccion by remember { mutableStateOf<TransaccionItem?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    var transacciones by remember { mutableStateOf<List<TransaccionItem>>(emptyList()) }
    var totalIngresos by remember { mutableStateOf(0.0) }
    var totalGastos by remember { mutableStateOf(0.0) }
    var balanceTotal by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }

    // --- ESCUCHAR LA DIVISA PREFERIDA DEL USUARIO ---
    val currentCurrency by userViewModel.userCurrency.collectAsState()

    // MOTOR DE CONVERSIÓN (Base: MXN)
    val exchangeRate = when (currentCurrency) {
        "USD" -> 0.05  // 1 MXN = 0.05 USD
        "EUR" -> 0.045 // 1 MXN = 0.045 EUR
        else -> 1.0    // MXN se queda igual
    }

    // Configurar formateador dinámico de moneda
    val moneyFormatter = when (currentCurrency) {
        "USD" -> NumberFormat.getCurrencyInstance(Locale.US)
        "EUR" -> NumberFormat.getCurrencyInstance(Locale.FRANCE)
        else -> NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    }

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    // --- SINCRONIZACIÓN SILENCIOSA CON LA NUBE AL ABRIR LA APP ---
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getPerfil(userId)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                userViewModel.setUserData(
                    id = userId,
                    name = data?.nombre_s ?: userName,
                    email = data?.correo_electronico ?: "",
                    isPremium = isPremium,
                    currency = data?.divisa ?: "MXN",
                    photo = data?.foto_perfil
                )
            }
        } catch (e: Exception) { /* Falla silenciosa si no hay internet */ }
    }

    LaunchedEffect(refreshKey, currentCurrency) {
        isLoading = true
        try {
            val responseGastos = RetrofitClient.instance.getGastos(userId)
            val listaGastos = responseGastos.body()?.data ?: emptyList()

            val responseIngresos = RetrofitClient.instance.getIngresos(userId)
            val listaIngresos = responseIngresos.body()?.data ?: emptyList()

            // Aplicamos la conversión matemática a los totales
            totalGastos = listaGastos.sumOf { it.monto_gasto } * exchangeRate
            totalIngresos = listaIngresos.sumOf { it.monto } * exchangeRate
            balanceTotal = totalIngresos - totalGastos

            val itemsGastos = listaGastos.map {
                val isCompleted = (it.completado ?: 0) == 1
                TransaccionItem(
                    id = it.gasto_id, isGasto = true, titulo = it.nombre_gasto,
                    categoria = "Gasto",
                    monto = it.monto_gasto * exchangeRate,
                    fecha = it.fecha_gasto.substringBefore("T"),
                    frecuencia = it.plazo ?: "ÚNICO",
                    status = if(isCompleted) "PAGADO" else "PENDIENTE",
                    isCompleted = isCompleted,
                    imagen_uri = it.imagen_uri
                )
            }

            val itemsIngresos = listaIngresos.map {
                val isCompleted = (it.recibido ?: 1) == 1
                TransaccionItem(
                    id = it.ingreso_id, isGasto = false, titulo = it.nombre_ingreso,
                    categoria = it.nombre_categoria ?: "Ingreso",
                    monto = it.monto * exchangeRate,
                    fecha = it.fecha_ingreso.substringBefore("T"),
                    frecuencia = it.plazo ?: "ÚNICO",
                    status = if(isCompleted) "RECIBIDO" else "PENDIENTE",
                    isCompleted = isCompleted,
                    imagen_uri = it.imagen_uri
                )
            }

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
            item {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(PrimaryBlue).padding(24.dp)) {
                    if (!isPremium) {
                        Surface(color = SecondaryRed, shape = RoundedCornerShape(bottomStart = 8.dp), modifier = Modifier.align(Alignment.TopEnd).offset(x = 24.dp, y = (-24).dp)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Text(" GRATIS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column {
                        Text(text = "$greeting, ${userName.split(" ")[0]}!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

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
                val groupedTransactions = transacciones.groupBy { it.fecha }

                groupedTransactions.forEach { (dateStr, txList) ->
                    item {
                        val dateObj = try { LocalDate.parse(dateStr) } catch(e: Exception) { null }
                        val today = LocalDate.now()
                        val headerText = if (dateObj == today) "Hoy" else dateStr

                        Text(
                            text = "Transacciones de $headerText",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }

                    items(txList) { t ->
                        val sign = if (t.isGasto) "-" else "+"
                        val moneyText = "$sign${moneyFormatter.format(t.monto)}"
                        val iconColor = if (t.isGasto) SecondaryRed else AccentGreen
                        val iconImage = if (t.isGasto) Icons.Default.TrendingDown else Icons.Default.TrendingUp

                        TransactionCard(
                            title = t.titulo, category = t.categoria, freq = t.frecuencia, amount = moneyText,
                            date = t.fecha, icon = iconImage, iconColor = iconColor,
                            status = t.status, isCompleted = t.isCompleted,
                            imagenUri = t.imagen_uri, // <--- PASAMOS LA URI AQUÍ
                            onToggleClick = {
                                scope.launch {
                                    try {
                                        val nuevoEstado = if (t.isCompleted) 0 else 1
                                        if (t.isGasto) {
                                            // Al actualizar estatus mandamos el ID real, el backend no cambia
                                            val res = RetrofitClient.instance.updateEstatusGasto(t.id, EstatusGastoRequest(nuevoEstado))
                                            if (res.isSuccessful) refreshKey++
                                        } else {
                                            val res = RetrofitClient.instance.updateEstatusIngreso(t.id, EstatusIngresoRequest(nuevoEstado))
                                            if (res.isSuccessful) refreshKey++
                                        }
                                    } catch(e: Exception) {
                                        println("Error actualizando estatus: ${e.message}")
                                    }
                                }
                            },
                            onEditClick = {
                                selectedTransaccion = t
                                transactionTypeExpense = t.isGasto
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, containerColor = Color.White) {
            EditTransactionContent(
                isPremium = isPremium,
                isExpense = transactionTypeExpense,
                transaccion = selectedTransaccion,
                userId = userId,
                onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false } },
                onNavigateToPremium = { showBottomSheet = false; onNavigate("premium") },
                onSaveSuccess = { showBottomSheet = false; refreshKey++ },
                onDeleteSuccess = { showBottomSheet = false; refreshKey++ }
            )
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
fun TransactionCard(
    title: String, category: String, freq: String, amount: String, date: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color,
    status: String, isCompleted: Boolean,
    imagenUri: String? = null, // <--- NUEVO PARÁMETRO
    onToggleClick: () -> Unit, onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            // --- BOX ACTUALIZADO PARA MOSTRAR LA FOTO ---
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(0.1f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!imagenUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(imagenUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    Icon(icon, null, tint = iconColor)
                }
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

                if (freq.uppercase() != "ÚNICO" && freq.uppercase() != "UNICO") {
                    Spacer(modifier = Modifier.height(4.dp))
                    val btnColor = if (isCompleted) AccentGreen else SecondaryRed
                    Surface(
                        color = btnColor.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onToggleClick() }
                    ) {
                        Text(
                            text = status,
                            color = btnColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp).padding(start = 8.dp)) {
                Icon(Icons.Outlined.Edit, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}
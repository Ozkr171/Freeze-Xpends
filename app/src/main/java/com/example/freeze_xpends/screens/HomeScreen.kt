package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.freeze_xpends.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPremium: Boolean,
    userName: String, // Recibe el nombre desde la DB
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var transactionTypeExpense by remember { mutableStateOf(false) }
    val misTransacciones = listOf("Salario", "Renta")

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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // --- 1. HEADER AZUL DINÁMICO ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(PrimaryBlue)
                        .padding(24.dp)
                ) {
                    if (!isPremium) {
                        Surface(
                            color = SecondaryRed,
                            shape = RoundedCornerShape(bottomStart = 8.dp),
                            modifier = Modifier.align(Alignment.TopEnd).offset(x = 24.dp, y = (-24).dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Text(" GRATIS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column {
                        // Saludo con el primer nombre real
                        Text(
                            text = "¡Qué onda, ${userName.split(" ")[0]}!",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("BALANCE TOTAL", color = Color.White.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("$3,077", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Pequeña etiqueta de usuario
                                Text(userName.take(2).uppercase() + " ", color = Color.White, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onNavigate("settings") }, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).size(40.dp)) {
                                    Icon(Icons.Default.Settings, null, tint = PrimaryBlue)
                                }
                            }
                        }

                        Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            Text("Presupuesto: $21,250", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard("Ingresos", "$10,625", Icons.Default.TrendingUp, modifier = Modifier.weight(1f))
                            SummaryCard("Gastos", "$7,548", Icons.Default.TrendingDown, modifier = Modifier.weight(1f))
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
                    Surface(
                        modifier = Modifier.clickable { if (!isPremium) onNavigate("premium") },
                        border = BorderStroke(1.dp, BorderSlate),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isPremium) Icons.Default.DateRange else Icons.Default.Lock, null, tint = if (isPremium) PrimaryBlue else SecondaryRed, modifier = Modifier.size(14.dp))
                            Text(" Calendario", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }
            }

            // --- 4. LISTA DE MOVIMIENTOS ---
            if (misTransacciones.isEmpty()) {
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
                item {
                    TransactionCard(
                        title = "Salario Quincena", category = "Salario", freq = "QUINCENAL", amount = "+$10,625", date = "01/09/25", status = "RECIBIDO", icon = Icons.Default.TrendingUp, iconColor = AccentGreen,
                        onEditClick = { transactionTypeExpense = false; showBottomSheet = true }
                    )
                }
                item {
                    TransactionCard(
                        title = "Renta", category = "Vivienda", freq = "MENSUAL", amount = "-$7,000", date = "10/09/25", status = "PAGADO", icon = Icons.Default.AttachMoney, iconColor = PrimaryBlue,
                        onEditClick = { transactionTypeExpense = true; showBottomSheet = true }
                    )
                }
            }
        }
    }

    // --- MODAL BOTTOM SHEET PARA EDITAR ---
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black.copy(0.1f)) }
        ) {
            EditTransactionContent(
                isPremium = isPremium,
                isExpense = transactionTypeExpense,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showBottomSheet = false
                    }
                },
                onNavigateToPremium = {
                    showBottomSheet = false
                    onNavigate("premium")
                },
                onSaveClick = { /* Lógica guardar */ },
                onDeleteClick = { /* Lógica eliminar */ }
            )
        }
    }
}

// --- COMPONENTES AUXILIARES ---

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
                    Text("Vence: $date", fontSize = 10.sp, color = TextMuted)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSlate.copy(0.5f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = if(status == "PENDIENTE") SecondaryRed.copy(0.1f) else AccentGreen.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(status == "PENDIENTE") SecondaryRed else AccentGreen)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onEditClick() }
                ) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp), tint = TextMuted)
                    Text(" Editar", color = TextMuted, fontSize = 14.sp)
                }
            }
        }
    }
}
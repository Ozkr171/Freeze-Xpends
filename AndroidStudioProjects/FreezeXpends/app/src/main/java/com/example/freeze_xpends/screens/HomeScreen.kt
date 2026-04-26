package com.example.freeze_xpends.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

data class Transaccion(
    val id: Int, val concepto: String, val monto: Double, val fecha: String,
    val tipo: String, val categoria: String, val plazo: String, val estado: String
)

@Composable
fun HomeScreen(
    isPremium: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPremium: () -> Unit // Nueva ruta
) {
    val transacciones = remember {
        listOf(
            Transaccion(1, "Salario Quincena", 10625.0, "01/09/25", "ingreso", "Salario", "QUINCENAL", "RECIBIDO"),
            Transaccion(2, "Renta", 7000.0, "10/09/25", "gasto", "Vivienda", "MENSUAL", "PAGADO"),
            Transaccion(3, "Spotify", 250.0, "14/09/25", "gasto", "Entretenimiento", "MENSUAL", "PAGADO")
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd, containerColor = RedSecondary, contentColor = Color.White,
                shape = RoundedCornerShape(100.dp), icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuevo", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).padding(paddingValues)) {

            // HEADER AZUL
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text("BALANCE TOTAL", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("$3,077", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                    Text("Presupuesto: $21,250", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isPremium) "ER ERI ⭐" else "ER ERI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(onClick = onNavigateToSettings, color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.Settings, "Ajustes", tint = BluePrimary, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ingresos", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$10,625", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Gastos", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$7,548", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ETIQUETA ROJA "GRATIS" EN LA ESQUINA
                if (!isPremium) {
                    Surface(color = RedSecondary, shape = RoundedCornerShape(bottomStart = 8.dp), modifier = Modifier.align(Alignment.TopEnd)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("GRATIS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // BLOQUE DE ANUNCIOS (SOLO MODO FREE)
            if (!isPremium) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ANUNCIO PUBLICITARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.85f).height(60.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BackgroundGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Espacio para Banner 320x50", color = SlateMuted.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.clickable { onNavigateToPremium() }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = RedSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quitar anuncios", color = RedSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TÍTULO TRANSACCIONES + BOTÓN CALENDARIO (BLOQUEADO SI ES FREE)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Transacciones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForegroundDark)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if(isPremium) Color.Transparent else RedSecondary.copy(alpha=0.3f)),
                    color = if(isPremium) Color.Transparent else Color.White,
                    modifier = Modifier.clickable { if(isPremium) onNavigateToCalendar() else onNavigateToPremium() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if(isPremium) Icons.Default.CalendarToday else Icons.Default.Lock, null, tint = if(isPremium) BluePrimary else RedSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calendario", color = if(isPremium) BluePrimary else SlateMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // LISTA (Igual que antes)
            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(transacciones) { tx ->
                    val isIngreso = tx.tipo == "ingreso"
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp), border = BorderStroke(1.dp, SlateMuted.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if(isIngreso) GreenAccent.copy(alpha=0.15f) else BluePrimary.copy(alpha=0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(if (isIngreso) Icons.Default.TrendingUp else Icons.Default.AttachMoney, null, tint = if(isIngreso) GreenAccent else BluePrimary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(tx.concepto, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ForegroundDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(color = BackgroundGray, shape = RoundedCornerShape(4.dp)) { Text(tx.categoria, fontSize = 10.sp, color = SlateMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Refresh, null, tint = if(isIngreso) GreenAccent else BluePrimary, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(tx.plazo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isIngreso) GreenAccent else BluePrimary)
                                            }
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${if(isIngreso) "+" else "-"}$${String.format("%,.0f", tx.monto)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if(isIngreso) GreenAccent else ForegroundDark)
                                    Text("Vence: ${tx.fecha}", color = SlateMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = GreenAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(100.dp)) { Text(tx.estado, color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, null, tint = SlateMuted, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar", color = SlateMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
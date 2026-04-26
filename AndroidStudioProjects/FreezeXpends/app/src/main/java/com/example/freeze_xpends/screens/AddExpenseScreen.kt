package com.example.freeze_xpends.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@Composable
fun AddExpenseScreen(
    isPremium: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    var tipo by remember { mutableStateOf("gasto") }
    var monto by remember { mutableStateOf("") }
    var concepto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var plazo by remember { mutableStateOf("Único") }
    var hasReminder by remember { mutableStateOf(false) }

    val mainColor = if (tipo == "ingreso") GreenAccent else BluePrimary

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        // HEADER AZUL O VERDE SEGÚN TIPO
        Surface(modifier = Modifier.fillMaxWidth(), color = mainColor, shadowElevation = 0.dp) {
            Row(modifier = Modifier.padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White) }
                Text(if (tipo == "ingreso") "NUEVO INGRESO" else "NUEVO GASTO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }

        // FORMULARIO EN TARJETA BLANCA SÚPER REDONDEADA
        Card(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp).clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {

                // Selector Gasto / Ingreso
                Row(modifier = Modifier.fillMaxWidth().border(1.dp, BackgroundGray, RoundedCornerShape(12.dp)).padding(4.dp)) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (tipo == "gasto") RedSecondary else Color.Transparent).clickable { tipo = "gasto" }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, null, tint = if (tipo == "gasto") Color.White else SlateMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Gasto", color = if (tipo == "gasto") Color.White else SlateMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (tipo == "ingreso") GreenAccent else Color.Transparent).clickable { tipo = "ingreso" }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = if (tipo == "ingreso") Color.White else SlateMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ingreso", color = if (tipo == "ingreso") Color.White else SlateMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Monto Gigante
                Text("MONTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateMuted, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = monto, onValueChange = { monto = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    placeholder = { Text("0.00", fontSize = 28.sp, fontWeight = FontWeight.Black, color = SlateMuted) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Black, color = ForegroundDark),
                    shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = mainColor)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Concepto
                Text("CONCEPTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateMuted, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = concepto, onValueChange = { concepto = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = mainColor)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // FRECUENCIA CON CANDADO ROJO (MODO FREE IDÉNTICO AL FIGMA)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FRECUENCIA / PLAZO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateMuted, letterSpacing = 1.sp)
                    if (!isPremium) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Lock, null, tint = RedSecondary, modifier = Modifier.size(12.dp))
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = if (isPremium) plazo else "BLOQUEADO", // Texto explicativo si está bloqueado
                        onValueChange = { plazo = it },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = isPremium,
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = if(isPremium) ForegroundDark else SlateMuted) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BackgroundGray,
                            focusedBorderColor = mainColor,
                            disabledTextColor = SlateMuted, // Texto gris si está deshabilitado
                            disabledBorderColor = BackgroundGray
                        )
                    )
                    // Si no es premium, ponemos un botón invisible encima para detectar el click y mostrar el aviso
                    if (!isPremium) {
                        Box(modifier = Modifier.matchParentSize().clickable { onNavigateToPremium() })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recordatorio Simple
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, BackgroundGray, RoundedCornerShape(16.dp))
                        .clickable { hasReminder = !hasReminder }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = SlateMuted)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("ACTIVAR RECORDATORIO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                                Text("Recibe una alerta antes del pago", fontSize = 10.sp, color = SlateMuted)
                            }
                        }
                        Icon(if (hasReminder) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (hasReminder) mainColor else SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(32.dp))

                // Botón Guardar Verde
                Button(
                    onClick = { /* Guardar */ }, modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Icon(Icons.Default.AttachMoney, null)
                    Spacer(Modifier.width(8.dp))
                    Text("GUARDAR TRANSACCIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}
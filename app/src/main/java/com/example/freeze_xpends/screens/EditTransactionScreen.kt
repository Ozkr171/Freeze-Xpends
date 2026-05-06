package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionContent(
    isPremium: Boolean,
    isExpense: Boolean,
    onClose: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val primaryColor = if (isExpense) SecondaryRed else AccentGreen
    val titleText = if (isExpense) "Editar Gasto" else "Editar Ingreso"
    val iconVector = if (isExpense) Icons.Default.TrendingDown else Icons.Default.TrendingUp

    var amount by remember { mutableStateOf("10625") }
    var concept by remember { mutableStateOf("Salario Quincena") }
    var date by remember { mutableStateOf("01/09/25") }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Salario") }
    val categories = listOf("Salario", "Negocio", "Inversiones", "Regalos", "Préstamo", "Otros")

    var expandedPlazo by remember { mutableStateOf(false) }
    var selectedPlazo by remember { mutableStateOf("Quincenal") }
    val plazos = listOf("Pago Único", "Semanal", "Quincenal", "Mensual", "Anual")

    var isCompleted by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // --- 1. HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(primaryColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconVector, contentDescription = null, tint = primaryColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(titleText, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextDark)
                    Text("ID: #1", fontSize = 12.sp, color = TextMuted)
                }
            }
            // AQUI ESTÁ EL PRIMER ARREGLO
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BorderSlate.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. CONTENIDO SCROLLABLE ---
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Monto total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                leadingIcon = { Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = primaryColor) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextDark),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Concepto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            OutlinedTextField(
                value = concept, onValueChange = { concept = it },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Categoría", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
                )
                ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expandedCategory = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Plazo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (!isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Lock, null, tint = SecondaryRed, modifier = Modifier.size(12.dp))
                }
            }
            ExposedDropdownMenuBox(
                expanded = expandedPlazo,
                onExpandedChange = { if (isPremium) expandedPlazo = !expandedPlazo else onNavigateToPremium() }
            ) {
                OutlinedTextField(
                    value = selectedPlazo, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlazo) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor, unfocusedContainerColor = if (isPremium) Color.Transparent else BackgroundSlate)
                )
                ExposedDropdownMenu(expanded = expandedPlazo, onDismissRequest = { expandedPlazo = false }) {
                    plazos.forEach { plazo ->
                        DropdownMenuItem(text = { Text(plazo) }, onClick = { selectedPlazo = plazo; expandedPlazo = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Fecha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            OutlinedTextField(
                value = date, onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Estado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .border(1.dp, if (isCompleted) AccentGreen else BorderSlate, RoundedCornerShape(8.dp))
                        .background(if (isCompleted) AccentGreen.copy(0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { isCompleted = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(if (isCompleted) AccentGreen else TextMuted, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recibido", color = if (isCompleted) AccentGreen else TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .border(1.dp, if (!isCompleted) SecondaryRed else BorderSlate, RoundedCornerShape(8.dp))
                        .background(if (!isCompleted) SecondaryRed.copy(0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { isCompleted = false },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(if (!isCompleted) SecondaryRed else TextMuted, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pendiente", color = if (!isCompleted) SecondaryRed else TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- AQUI ESTAN LOS OTROS 2 ARREGLOS ---
        Button(
            onClick = { onSaveClick(); onClose() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onDeleteClick(); onClose() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E8)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Delete, null, tint = SecondaryRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isExpense) "Eliminar Gasto" else "Eliminar Ingreso", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SecondaryRed)
        }
    }
}
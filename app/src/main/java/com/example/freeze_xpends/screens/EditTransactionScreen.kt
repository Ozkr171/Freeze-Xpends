package com.example.freeze_xpends.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.network.GastoRequest
import com.example.freeze_xpends.network.IngresoRequest
import com.example.freeze_xpends.network.Categoria
import com.example.freeze_xpends.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionContent(
    isPremium: Boolean,
    isExpense: Boolean,
    transaccion: TransaccionItem?,
    userId: Int,
    onClose: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onSaveSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val primaryColor = if (isExpense) SecondaryRed else AccentGreen
    val titleText = if (isExpense) "Editar Gasto" else "Editar Ingreso"
    val iconVector = if (isExpense) Icons.Default.TrendingDown else Icons.Default.TrendingUp

    var amount by remember { mutableStateOf(transaccion?.monto?.toString() ?: "") }
    var concept by remember { mutableStateOf(transaccion?.titulo ?: "") }
    var date by remember { mutableStateOf(transaccion?.fecha ?: "") }
    var isCompleted by remember { mutableStateOf(transaccion?.status == "RECIBIDO" || transaccion?.status == "PAGADO") }

    var categories by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Categoria?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }

    var expandedPlazo by remember { mutableStateOf(false) }
    var selectedPlazo by remember { mutableStateOf(transaccion?.frecuencia ?: "Pago Único") }
    val plazos = listOf("Pago Único", "Semanal", "Quincenal", "Mensual", "Anual")

    var isReminderActive by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }

    // --- CONFIGURACIÓN DEL CALENDARIO NATIVO ---
    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(isExpense) {
        try {
            val response = if (isExpense) {
                RetrofitClient.instance.getCategoriasGastos(userId)
            } else {
                RetrofitClient.instance.getCategoriasIngresos(userId)
            }
            if (response.isSuccessful) {
                categories = response.body()?.data ?: emptyList()
                selectedCategory = categories.find { it.nombre_categoria == transaccion?.categoria } ?: categories.firstOrNull()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando categorías", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
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
                    Text("ID: #${transaccion?.id ?: "Nuevo"}", fontSize = 12.sp, color = TextMuted)
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BorderSlate.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Icono / Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (!isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Lock, null, tint = SecondaryRed, modifier = Modifier.size(12.dp))
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(100.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .clickable { if (!isPremium) onNavigateToPremium() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp).background(PrimaryBlue.copy(0.1f), CircleShape).padding(4.dp))
                    Text("SUBIR IMAGEN PERSONALIZADA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Monto total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                leadingIcon = { Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = primaryColor) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                singleLine = true,
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
                singleLine = true,
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
                    value = selectedCategory?.nombre_categoria ?: "Cargando...", onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
                )
                ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.nombre_categoria) },
                            onClick = { selectedCategory = cat; expandedCategory = false }
                        )
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
                if (isPremium) {
                    ExposedDropdownMenu(expanded = expandedPlazo, onDismissRequest = { expandedPlazo = false }) {
                        plazos.forEach { plazo ->
                            DropdownMenuItem(text = { Text(plazo) }, onClick = { selectedPlazo = plazo; expandedPlazo = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isPremium) onNavigateToPremium() else isReminderActive = !isReminderActive
                },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (isReminderActive) PrimaryBlue else BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(if (isReminderActive) PrimaryBlue.copy(0.1f) else BackgroundSlate, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsNone, null, tint = if (isReminderActive) PrimaryBlue else TextMuted)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ACTIVAR RECORDATORIO", fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextDark)
                            Text("Recibe una alerta antes del pago", fontSize = 10.sp, color = TextMuted)
                        }
                        if (!isPremium) Icon(Icons.Default.Lock, null, tint = SecondaryRed, modifier = Modifier.size(20.dp))
                    }
                    if (isReminderActive && isPremium) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = reminderDate, onValueChange = { reminderDate = it }, placeholder = { Text("dd/mm/aaaa") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, placeholder = { Text("--:--") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- FECHA DINÁMICA CON SELECTOR ---
            Text("Fecha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp).clickable { datePickerDialog.show() }) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true, // Evita abrir el teclado
                    enabled = false, // Lo hace solo interactivo mediante el click global de la Box
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, null, tint = primaryColor) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextDark,
                        disabledBorderColor = BorderSlate,
                        disabledTrailingIconColor = primaryColor
                    )
                )
            }

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
                        Text(if (isExpense) "Pagado" else "Recibido", color = if (isCompleted) AccentGreen else TextMuted, fontWeight = FontWeight.Bold)
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

        Button(
            onClick = {
                if (transaccion != null && amount.isNotBlank() && concept.isNotBlank() && selectedCategory != null) {
                    scope.launch {
                        isLoading = true
                        try {
                            val montoDouble = amount.toDoubleOrNull() ?: 0.0
                            val response = if (isExpense) {
                                RetrofitClient.instance.updateGasto(
                                    transaccion.id,
                                    GastoRequest(userId, selectedCategory!!.categoria_id, date, concept, null, if (selectedPlazo == "Pago Único") null else selectedPlazo, montoDouble, null)
                                )
                            } else {
                                RetrofitClient.instance.updateIngreso(
                                    transaccion.id,
                                    IngresoRequest(userId, selectedCategory!!.categoria_id, date, concept, null, montoDouble, if (isCompleted) 1 else 0, if (selectedPlazo == "Pago Único") "ÚNICO" else selectedPlazo, null)
                                )
                            }

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Transacción actualizada", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            } else {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally { isLoading = false }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else {
                Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (transaccion != null) {
                    scope.launch {
                        isLoading = true
                        try {
                            val response = if (isExpense) {
                                RetrofitClient.instance.deleteGasto(transaccion.id)
                            } else {
                                RetrofitClient.instance.deleteIngreso(transaccion.id)
                            }

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Transacción eliminada", Toast.LENGTH_SHORT).show()
                                onDeleteSuccess()
                            } else {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally { isLoading = false }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E8)),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            Icon(Icons.Outlined.Delete, null, tint = SecondaryRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isExpense) "Eliminar Gasto" else "Eliminar Ingreso", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SecondaryRed)
        }
    }
}
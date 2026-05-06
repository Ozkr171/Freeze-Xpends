package com.example.freeze_xpends.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.network.TransactionRequest
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    userViewModel: UserViewModel,
    isPremium: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val userId by userViewModel.userId.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var isExpense by remember { mutableStateOf(true) }
    val primaryColor = if (isExpense) SecondaryRed else AccentGreen
    val headerTitle = if (isExpense) "NUEVO GASTO" else "NUEVO INGRESO"

    var amount by remember { mutableStateOf("") }
    var concept by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-06") }

    // --- ESTADOS PREMIUM ---
    var expandedFrequency by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf("Pago Único") }
    val frequencies = listOf("Pago Único", "Semanal", "Quincenal", "Mensual", "Anual")

    var isReminderActive by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundSlate,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().height(64.dp).background(primaryColor),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text(headerTitle, modifier = Modifier.fillMaxWidth().padding(end = 48.dp), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. SELECTOR GASTO/INGRESO ---
            Row(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)).padding(4.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isExpense) SecondaryRed else Color.Transparent).clickable { isExpense = true }, contentAlignment = Alignment.Center) {
                    Text("Gasto", color = if (isExpense) Color.White else TextMuted, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (!isExpense) AccentGreen else Color.Transparent).clickable { isExpense = false }, contentAlignment = Alignment.Center) {
                    Text("Ingreso", color = if (!isExpense) Color.White else TextMuted, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. ICONO / FOTO (RESTRICTO) ---
            SectionHeader("ICONO / FOTO", Icons.Default.Image, isLocked = !isPremium)
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .clickable { if (!isPremium) onNavigateToPremium() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp).background(PrimaryBlue.copy(0.1f), CircleShape).padding(4.dp))
                    Text("SUBIR IMAGEN PERSONALIZADA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. MONTO ---
            SectionHeader("MONTO", Icons.Default.AttachMoney)
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                placeholder = { Text("0.00", fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextDark),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. CONCEPTO ---
            SectionHeader("CONCEPTO", Icons.Default.Description)
            OutlinedTextField(
                value = concept, onValueChange = { concept = it },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. FRECUENCIA (DROPDOWN PREMIUM) ---
            SectionHeader("FRECUENCIA / PLAZO", Icons.Default.Sync, isLocked = !isPremium)
            ExposedDropdownMenuBox(
                expanded = expandedFrequency,
                onExpandedChange = { if (isPremium) expandedFrequency = !expandedFrequency else onNavigateToPremium() }
            ) {
                OutlinedTextField(
                    value = selectedFrequency, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
                )
                if (isPremium) {
                    ExposedDropdownMenu(expanded = expandedFrequency, onDismissRequest = { expandedFrequency = false }) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(text = { Text(freq) }, onClick = { selectedFrequency = freq; expandedFrequency = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 6. RECORDATORIO (CARD PREMIUM) ---
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

            Spacer(modifier = Modifier.height(32.dp))

            // --- 7. BOTÓN GUARDAR (AIVEN) ---
            Button(
                onClick = {
                    if (amount.isNotBlank() && concept.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.instance.addTransaction(
                                    TransactionRequest(userId, concept, "General", amount.toDoubleOrNull() ?: 0.0, if (isExpense) "GASTO" else "INGRESO", date)
                                )
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "¡Guardado en Aiven!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally { isLoading = false }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("GUARDAR TRANSACCIÓN", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isLocked: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        if (isLocked) Icon(Icons.Default.Lock, null, tint = SecondaryRed, modifier = Modifier.size(14.dp))
    }
}
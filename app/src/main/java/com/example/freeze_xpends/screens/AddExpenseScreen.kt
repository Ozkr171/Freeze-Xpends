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
import com.example.freeze_xpends.network.GastoRequest // <-- Importamos los nuevos
import com.example.freeze_xpends.network.IngresoRequest // <-- Importamos los nuevos
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

    // --- ESTADOS DE DATOS ---
    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var concept by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-14") }
    var isLoading by remember { mutableStateOf(false) }

    // --- ESTADOS DE CATEGORÍAS ---
    var categories by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Categoria?>(null) }
    var expandedCat by remember { mutableStateOf(false) }

    // --- CARGAR CATEGORÍAS SEGÚN EL TIPO ---
    LaunchedEffect(isExpense) {
        try {
            val response = if (isExpense) {
                RetrofitClient.instance.getCategoriasGastos(userId)
            } else {
                RetrofitClient.instance.getCategoriasIngresos(userId)
            }
            if (response.isSuccessful) {
                categories = response.body()?.data ?: emptyList()
                selectedCategory = categories.firstOrNull() // Selecciona la primera por defecto
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando categorías", Toast.LENGTH_SHORT).show()
        }
    }

    // Colores dinámicos
    val primaryColor = if (isExpense) SecondaryRed else AccentGreen

    Scaffold(
        containerColor = BackgroundSlate,
        topBar = { /* ... (Header igual) ... */ }
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

            // --- 2. MONTO ---
            SectionHeader("MONTO", Icons.Default.AttachMoney)
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. CONCEPTO ---
            SectionHeader("CONCEPTO", Icons.Default.Description)
            OutlinedTextField(
                value = concept, onValueChange = { concept = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. SELECTOR DE CATEGORÍA (NUEVO) ---
            SectionHeader("CATEGORÍA", Icons.Default.Category)
            ExposedDropdownMenuBox(
                expanded = expandedCat,
                onExpandedChange = { expandedCat = !expandedCat }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.nombre_categoria ?: "Seleccionar...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.nombre_categoria) },
                            onClick = {
                                selectedCategory = cat
                                expandedCat = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. BOTÓN GUARDAR ---
            Button(
                onClick = {
                    if (amount.isNotBlank() && concept.isNotBlank() && selectedCategory != null) {
                        scope.launch {
                            isLoading = true
                            try {
                                val monto = amount.toDoubleOrNull() ?: 0.0
                                val response = if (isExpense) {
                                    RetrofitClient.instance.addGasto(
                                        GastoRequest(userId, selectedCategory!!.categoria_id, date, concept, null, "ÚNICO", monto)
                                    )
                                } else {
                                    RetrofitClient.instance.addIngreso(
                                        IngresoRequest(userId, selectedCategory!!.categoria_id, date, concept, null, monto, 1)
                                    )
                                }

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "¡Guardado con éxito!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally { isLoading = false }
                        }
                    } else {
                        Toast.makeText(context, "Faltan datos o categoría", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("GUARDAR")
            }
        }
    }
}
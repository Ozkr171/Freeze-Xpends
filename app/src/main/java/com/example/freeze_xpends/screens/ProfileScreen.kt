package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel, // Agregamos el ViewModel como parámetro
    onNavigateBack: () -> Unit
) {
    // Obtenemos el ID del usuario actual
    val userId by userViewModel.userId.collectAsState()

    // Estados para los campos (ahora se llenarán con la API)
    var name by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf("siolasi21@gmail.com") } // Hardcoded por ahora o podrías traerlo en el login
    var phone by remember { mutableStateOf("+52 123 456 7890") }

    // Estado del Dropdown de Moneda
    var expandedCurrency by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("MXN - Peso Mexicano") }
    val currencies = listOf("MXN - Peso Mexicano", "USD - Dólar Estadounidense", "EUR - Euro")

    val scope = rememberCoroutineScope()

    // --- LOGICA DE CARGA REAL ---
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getPerfil(userId)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                name = data?.nombre_s ?: "Usuario"
                // Aquí podrías actualizar más estados si tu tabla Usuario tuviera teléfono o moneda
            }
        } catch (e: Exception) {
            // Error de conexión silencioso o podrías mostrar un Toast
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
    ) {
        // --- 1. HEADER AZUL (Diseño Original) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(PrimaryBlue),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = "Mi Perfil",
                modifier = Modifier.padding(start = 48.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 2. AVATAR DINÁMICO ---
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if(name.length >= 2) name.take(2).uppercase() else "ER", // Dinámico según el nombre
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AccentGreen, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { /* Abrir galería */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. TARJETA DE FORMULARIO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Nombre completo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Teléfono", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Moneda preferida", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    ExposedDropdownMenuBox(
                        expanded = expandedCurrency,
                        onExpandedChange = { expandedCurrency = !expandedCurrency }
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCurrency,
                            onDismissRequest = { expandedCurrency = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency) },
                                    onClick = {
                                        selectedCurrency = currency
                                        expandedCurrency = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. BOTÓN GUARDAR ---
            Button(
                onClick = {
                    // Aquí después haremos el PUT para actualizar en Aiven
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit) {
    // Estados para los campos
    var name by remember { mutableStateOf("Eri") }
    var email by remember { mutableStateOf("eri@email.com") }
    var phone by remember { mutableStateOf("+52 123 456 7890") }

    // Estado del Dropdown de Moneda
    var expandedCurrency by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("MXN - Peso Mexicano") }
    val currencies = listOf("MXN - Peso Mexicano", "USD - Dólar Estadounidense", "EUR - Euro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
    ) {
        // --- 1. HEADER AZUL ---
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
            // --- 2. AVATAR CON BOTÓN DE CÁMARA (image_03dc38.png) ---
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Círculo de Iniciales
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ER",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Botón verde de cámara
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
                    // Nombre
                    Text("Nombre completo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Teléfono
                    Text("Teléfono", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Moneda Preferida (image_03dc32.png)
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
                onClick = { /* Lógica de guardado */ },
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
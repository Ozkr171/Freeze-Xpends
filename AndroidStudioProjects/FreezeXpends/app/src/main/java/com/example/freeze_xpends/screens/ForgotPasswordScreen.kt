package com.example.freeze_xpends.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) } // Para cambiar de estado al enviar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // HEADER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BluePrimary,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(top = 40.dp, bottom = 16.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text("RECUPERAR ACCESO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isSent) {
                // --- ESTADO 1: PEDIR EMAIL ---
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = BluePrimary.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.LockReset, null, tint = BluePrimary, modifier = Modifier.padding(20.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("¿Olvidaste tu contraseña?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ForegroundDark)
                Text(
                    "Ingresa tu correo electrónico para enviarte un enlace de recuperación.",
                    fontSize = 14.sp, color = SlateMuted, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Email de la cuenta", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            placeholder = { Text("tu@email.com", color = SlateMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = BluePrimary) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (email.isNotEmpty()) isSent = true
                                // Aquí luego conectaremos el servicio de NodeMailer o Firebase Auth
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("ENVIAR ENLACE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // --- ESTADO 2: CORREO ENVIADO ---
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = GreenAccent.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.MarkEmailRead, null, tint = GreenAccent, modifier = Modifier.padding(24.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("¡Revisa tu bandeja!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ForegroundDark)
                Text(
                    "Hemos enviado instrucciones a:\n$email",
                    fontSize = 14.sp, color = SlateMuted, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForegroundDark)
                ) {
                    Text("VOLVER AL LOGIN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
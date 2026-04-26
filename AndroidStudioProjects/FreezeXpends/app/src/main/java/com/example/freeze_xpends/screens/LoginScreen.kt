package com.example.freeze_xpends.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.network.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToForgot: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ESTADO PARA MOSTRAR/OCULTAR CONTRASEÑA
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = BluePrimary,
                shadowElevation = 8.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = "Login Icon",
                    modifier = Modifier.padding(20.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "FREEZE-XPENDS", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimary, letterSpacing = 1.sp)
            Text(text = "Gestiona tus gastos fácilmente", color = SlateMuted, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    if (errorMessage != null) {
                        Surface(color = RedSecondary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Text(text = errorMessage!!, color = RedSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                        }
                    }

                    Text("Email", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                        placeholder = { Text("tu@email.com", color = SlateMuted) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = BluePrimary) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary)
                    )

                    // CAMPO CONTRASEÑA CON EL OJO
                    Text("Contraseña", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
                        placeholder = { Text("••••••••", color = SlateMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = BluePrimary) },
                        // LÓGICA DE VISIBILIDAD
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = null, tint = SlateMuted)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary)
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val request = LoginRequest(correo_electronico = email, contrasena = password)
                                    val response = RetrofitClient.instance.loginUser(request)
                                    if (response.isSuccessful) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Correo o contraseña incorrectos"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Sin conexión con el servidor"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, BluePrimary)
                    ) {
                        Text("Crear Cuenta", color = BluePrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            TextButton(onClick = onNavigateToForgot) {
                Text("¿Olvidaste tu contraseña?", color = RedSecondary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToPremium) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver planes Premium", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
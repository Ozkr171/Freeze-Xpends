package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.network.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados para visibilidad de contraseña
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Función de validación de seguridad
    fun isPasswordSecure(pass: String): Boolean {
        val pattern = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$".toRegex()
        return pattern.containsMatchIn(pass)
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray).padding(horizontal = 24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = BluePrimary, shadowElevation = 4.dp) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.padding(20.dp), tint = Color.White)
            }

            Text("Crear Cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BluePrimary, modifier = Modifier.padding(top = 16.dp))
            Text("Únete a FREEZE-XPENDS", color = SlateMuted, fontSize = 14.sp)

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {

                    if (errorMessage != null) {
                        Surface(color = RedSecondary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Text(errorMessage!!, color = RedSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        }
                    }

                    Text("Nombre completo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary))

                    Text("Email", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary))

                    // CAMPO CONTRASEÑA CON OJO Y VALIDACIÓN
                    Text("Contraseña", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null, tint = SlateMuted) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary)
                    )
                    Text("Mín. 8 caracteres, 1 mayúscula y 1 número", fontSize = 10.sp, color = if(isPasswordSecure(password)) GreenAccent else SlateMuted, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))

                    Text("Confirmar contraseña", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForegroundDark)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(image, null, tint = SlateMuted) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BackgroundGray, focusedBorderColor = BluePrimary)
                    )

                    Button(
                        onClick = {
                            if (!isPasswordSecure(password)) {
                                errorMessage = "La contraseña no cumple los requisitos de seguridad"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Las contraseñas no coinciden"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val response = RetrofitClient.instance.registerUser(RegisterRequest(name, email, password))
                                    if (response.isSuccessful) onNavigateToLogin()
                                    else errorMessage = "El correo ya está registrado"
                                } catch (e: Exception) {
                                    errorMessage = "Sin conexión con el servidor"
                                } finally { isLoading = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BluePrimary)) {
                        Text("Ya tengo cuenta", color = BluePrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
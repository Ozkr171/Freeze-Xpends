package com.example.freeze_xpends.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.network.LoginRequest
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.utils.SessionManager
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    sessionManager: SessionManager,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
                .background(BackgroundSlate)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("FREEZE-XPENDS", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PrimaryBlue, letterSpacing = 2.sp)
        Text("Congela tus gastos, controla tu futuro", fontSize = 14.sp, color = TextMuted, modifier = Modifier.padding(top = 8.dp, bottom = 48.dp))

        // Campo Email
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, null) }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderSlate)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
            },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderSlate)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN CON CONEXIÓN REAL A LA API ---
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        try {
                            // AQUÍ CORREGIMOS EL ERROR DE LA DOBLE VARIABLE
                            val response = RetrofitClient.instance.loginUser(LoginRequest(email.trim(), password.trim()))

                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body?.user_id != null) {
                                    sessionManager.saveSession(
                                        userId = body.user_id,
                                        nombre = body.nombre ?: "Usuario",
                                        email = email,
                                        isPremium = body.premium == 1
                                    )
                                    userViewModel.setUserData(
                                        id = body.user_id,
                                        name = body.nombre ?: "Usuario",
                                        email = email,
                                        isPremium = body.premium == 1
                                    )
                                    Toast.makeText(context, "¡Bienvenido, ${body.nombre}!", Toast.LENGTH_SHORT).show()
                                    onNavigate("home")
                                } else {
                                    Toast.makeText(context, "Error leyendo los datos", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Llena tus datos, pa", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            else Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { onNavigate("register") }, modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
            border = BorderStroke(1.dp, PrimaryBlue)
        ) { Text("¿No tienes cuenta? Regístrate", fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(24.dp))

        Text("¿Olvidaste tu contraseña?", color = PrimaryBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigate("forgot-password") })
    }
}
package com.example.freeze_xpends.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.freeze_xpends.network.RegisterRequest
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- NUEVOS ESTADOS PARA LA API ---
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
        Text(
            text = "Crear Cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryBlue
        )

        Text(
            text = "Regístrate para empezar a congelar tus gastos",
            fontSize = 14.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Campo de Nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre Completo") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderSlate)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderSlate)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderSlate)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN CREAR CUENTA CON CONEXIÓN A API ---
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        try {
                            val response = RetrofitClient.instance.registerUser(
                                RegisterRequest(
                                    nombre_s = name.trim(),
                                    correo_electronico = email.trim(),
                                    contrasena = password.trim()
                                )
                            )

                            if (response.isSuccessful) {
                                Toast.makeText(context, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                                // Si todo sale bien, lo mandamos al Login para que inicie sesión
                                onNavigateToLogin()
                            } else {
                                // Si Node.js devuelve un 400 (correo duplicado) o 500
                                val errorMsg = if (response.code() == 400) "Ese correo ya está en uso." else "Error en el servidor al registrar."
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading // Evita que piquen 2 veces mientras carga
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            } else {
                Text("Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("¿Ya tienes cuenta? ", color = TextMuted)
            Text(
                text = "Inicia Sesión",
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { if (!isLoading) onNavigateToLogin() }
            )
        }
    }
}
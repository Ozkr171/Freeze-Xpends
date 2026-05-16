package com.example.freeze_xpends.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.network.UpdatePerfilRequest
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId by userViewModel.userId.collectAsState()

    var name by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf("Cargando...") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var expandedCurrency by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("MXN - Peso Mexicano") }
    val currencies = listOf("MXN - Peso Mexicano", "USD - Dólar Estadounidense", "EUR - Euro")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    // Le rogamos a Android que no nos quite el permiso al cerrar la app
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) { println("Aviso: Permiso persistente denegado") }
                photoUri = uri
            }
        }
    )

    // --- CARGAR DATOS REALES DE LA NUBE ---
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getPerfil(userId)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                name = data?.nombre_s ?: "Usuario"
                email = data?.correo_electronico ?: ""

                // Mapear divisa a texto amigable
                selectedCurrency = when(data?.divisa) {
                    "USD" -> "USD - Dólar Estadounidense"
                    "EUR" -> "EUR - Euro"
                    else -> "MXN - Peso Mexicano"
                }

                // Cargar foto si existe
                if (!data?.foto_perfil.isNullOrBlank()) {
                    photoUri = Uri.parse(data!!.foto_perfil)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundSlate)) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
            Text("Mi Perfil", modifier = Modifier.padding(start = 48.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // --- AVATAR DINÁMICO ---
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.fillMaxSize().background(PrimaryBlue, CircleShape), contentAlignment = Alignment.Center) {
                    if (photoUri != null && photoUri.toString().isNotBlank()) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = if(name.length >= 2) name.take(2).uppercase() else "XX", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.size(36.dp).background(AccentGreen, CircleShape).border(2.dp, Color.White, CircleShape)
                        .clickable { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- FORMULARIO ---
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Nombre completo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Moneda preferida", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    ExposedDropdownMenuBox(expanded = expandedCurrency, onExpandedChange = { expandedCurrency = !expandedCurrency }) {
                        OutlinedTextField(
                            value = selectedCurrency, onValueChange = {}, readOnly = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = PrimaryBlue)
                        )
                        ExposedDropdownMenu(expanded = expandedCurrency, onDismissRequest = { expandedCurrency = false }) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(text = { Text(currency) }, onClick = { selectedCurrency = currency; expandedCurrency = false })
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN GUARDAR ---
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            try {
                                val currencyCode = selectedCurrency.substringBefore(" ") // Extrae "MXN", "USD", etc.
                                val req = UpdatePerfilRequest(
                                    nombre_s = name,
                                    correo_electronico = email,
                                    contrasena = null, // Contraseña se queda igual
                                    divisa = currencyCode,
                                    foto_perfil = photoUri?.toString()
                                )
                                val res = RetrofitClient.instance.updatePerfil(userId, req)

                                if (res.isSuccessful) {
                                    userViewModel.setUserData(userId, name, email, userViewModel.isPremium.value, currencyCode, photoUri?.toString())
                                    Toast.makeText(context, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } else {
                                    Toast.makeText(context, "Error al guardar los cambios", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "El nombre y el correo no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
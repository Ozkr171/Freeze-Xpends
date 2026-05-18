package com.example.freeze_xpends.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
fun SettingsScreen(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val userId by userViewModel.userId.collectAsState()
    val userName by userViewModel.userName.collectAsState()
    val userEmail by userViewModel.userEmail.collectAsState()
    val userCurrency by userViewModel.userCurrency.collectAsState()
    val userFormat by userViewModel.userFormat.collectAsState()
    val userPhoto by userViewModel.userPhoto.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFormatDialog by remember { mutableStateOf(false) } // <-- NUEVO ESTADO PARA EL MODAL

    Column(modifier = Modifier.fillMaxSize().background(BackgroundSlate)) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Ajustes", modifier = Modifier.padding(start = 48.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            Box(modifier = Modifier.size(100.dp).background(PrimaryBlue, CircleShape), contentAlignment = Alignment.Center) {
                if (!userPhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(userPhoto),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = if(userName.length >= 2) userName.take(2).uppercase() else "XX", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text(userEmail, fontSize = 14.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem("Mi Perfil", Icons.Default.Person) { onNavigate("profile") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))
                    SettingsItem("Presupuestos y Reportes", Icons.Default.AccountBalanceWallet) { onNavigate("budget") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))
                    SettingsItem("Calendario", Icons.Default.CalendarMonth) { onNavigate("calendar") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))

                    // --- DISPARADOR DEL MODAL DE FORMATO ---
                    SettingsItem("Formato de Visualización", Icons.Default.Numbers) { showFormatDialog = true }

                    HorizontalDivider(color = BorderSlate.copy(0.5f))
                    SettingsItem("Soporte Técnico", Icons.Default.HeadsetMic) { onNavigate("support") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))
                    SettingsItem("Términos y Condiciones", Icons.Default.Description) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1Hy4fjBffp3oGvueWhvSTiqWRjDd7VhjB/view?usp=sharing"))
                        context.startActivity(intent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DeleteForever, null, tint = SecondaryRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Mis Datos", color = SecondaryRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, null, tint = SecondaryRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", color = SecondaryRed, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- MODAL: FORMATO DE VISUALIZACIÓN ---
    if (showFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDialog = false },
            title = { Text("Formato Numérico", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark) },
            text = {
                Column {
                    Text("Elige cómo quieres ver tus números:", color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Opción US
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (userFormat == "US") PrimaryBlue.copy(0.1f) else Color.Transparent).clickable {
                            scope.launch {
                                try {
                                    val req = UpdatePerfilRequest(nombre_s = userName, correo_electronico = userEmail, divisa = userCurrency, foto_perfil = userPhoto, formato_num = "US")
                                    val res = RetrofitClient.instance.updatePerfil(userId, req)
                                    if (res.isSuccessful) {
                                        userViewModel.setUserData(userId, userName, userEmail, userViewModel.isPremium.value, userCurrency, userPhoto, "US")
                                        Toast.makeText(context, "Formato actualizado", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) { Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show() }
                                showFormatDialog = false
                            }
                        }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = userFormat == "US", onClick = null, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Estilo US/MX (1,000.00)", color = TextDark, fontWeight = FontWeight.Medium)
                    }

                    // Opción EU
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (userFormat == "EU") PrimaryBlue.copy(0.1f) else Color.Transparent).clickable {
                            scope.launch {
                                try {
                                    val req = UpdatePerfilRequest(nombre_s = userName, correo_electronico = userEmail, divisa = userCurrency, foto_perfil = userPhoto, formato_num = "EU")
                                    val res = RetrofitClient.instance.updatePerfil(userId, req)
                                    if (res.isSuccessful) {
                                        userViewModel.setUserData(userId, userName, userEmail, userViewModel.isPremium.value, userCurrency, userPhoto, "EU")
                                        Toast.makeText(context, "Formato actualizado", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) { Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show() }
                                showFormatDialog = false
                            }
                        }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = userFormat == "EU", onClick = null, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Estilo Europa (1.000,00)", color = TextDark, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFormatDialog = false }) {
                    Text("Cerrar", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar Cuenta?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark) },
            text = { Text("Esta acción borrará permanentemente todos tus gastos, ingresos, presupuestos y tu usuario. No se puede deshacer.", color = TextMuted, fontSize = 14.sp) },
            confirmButton = { Button(onClick = { showDeleteDialog = false; onDeleteAccount() }, colors = ButtonDefaults.buttonColors(containerColor = SecondaryRed)) { Text("Sí, Eliminar", color = Color.White, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = TextMuted, fontWeight = FontWeight.Bold) } },
            containerColor = Color.White, shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 14.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = TextDark, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
    }
}
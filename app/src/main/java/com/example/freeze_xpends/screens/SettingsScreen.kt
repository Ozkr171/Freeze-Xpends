package com.example.freeze_xpends.screens

import android.net.Uri
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    // RECOLECTAMOS LOS ESTADOS REALES DESDE EL VIEWMODEL
    val userName by userViewModel.userName.collectAsState()
    val userEmail by userViewModel.userEmail.collectAsState()
    val isPremium by userViewModel.isPremium.collectAsState()
    val userPhoto by userViewModel.userPhoto.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundSlate)) {
        // --- HEADER ---
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(PrimaryBlue), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Ajustes", modifier = Modifier.padding(start = 48.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // --- CÍRCULO DE FOTO DE PERFIL DINÁMICO ---
            Box(modifier = Modifier.size(100.dp).background(PrimaryBlue, CircleShape), contentAlignment = Alignment.Center) {
                if (!userPhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(userPhoto),
                        contentDescription = "Foto de perfil en Ajustes",
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

            // --- OPCIONES DE CONFIGURACIÓN ---
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BorderSlate)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem("Mi Perfil", Icons.Default.Person) { onNavigate("profile") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))
                    SettingsItem("Presupuestos y Reportes", Icons.Default.AccountBalanceWallet) { onNavigate("budget") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))
                    SettingsItem("Calendario", Icons.Default.CalendarMonth) { onNavigate("calendar") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))

                    // --- RECUPERADOS PARA LA FASE D ---
                    SettingsItem("Formato de Visualización", Icons.Default.Numbers) { /* TODO: Fase D */ }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))

                    SettingsItem("Soporte Técnico", Icons.Default.HeadsetMic) { onNavigate("support") }
                    HorizontalDivider(color = BorderSlate.copy(0.5f))

                    // --- RECUPERADOS PARA LA FASE D ---
                    SettingsItem("Términos y Condiciones", Icons.Default.Description) { /* TODO: Fase D (PDF) */ }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÓN ELIMINAR CUENTA / DATOS ---
            Button(
                onClick = onDeleteAccount, // Ya está conectado al parámetro de la función
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DeleteForever, null, tint = SecondaryRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Mis Datos", color = SecondaryRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BOTÓN CERRAR SESIÓN ---
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
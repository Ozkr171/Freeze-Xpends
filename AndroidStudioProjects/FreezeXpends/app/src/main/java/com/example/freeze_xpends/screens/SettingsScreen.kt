package com.example.freeze_xpends.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // HEADER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BluePrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text("Ajustes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("GENERAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateMuted, modifier = Modifier.padding(bottom = 8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsItem(Icons.Default.Person, "Perfil", onNavigateToProfile)
                    Divider(color = BackgroundGray, thickness = 1.dp)
                    // AQUÍ ESTÁ EL CALENDARIO COMO EN FIGMA
                    SettingsItem(Icons.Default.DateRange, "Calendario", onNavigateToCalendar)
                    Divider(color = BackgroundGray, thickness = 1.dp)
                    SettingsItem(Icons.Default.PieChart, "Presupuestos", onNavigateToBudget)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("CUENTA Y SEGURIDAD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateMuted, modifier = Modifier.padding(bottom = 8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsItem(Icons.Default.Star, "Premium", onNavigateToPremium)
                    Divider(color = BackgroundGray, thickness = 1.dp)
                    SettingsItem(Icons.Default.Notifications, "Notificaciones", {})
                    Divider(color = BackgroundGray, thickness = 1.dp)
                    SettingsItem(Icons.Default.Shield, "Seguridad", {})
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Cerrar Sesión
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedSecondary)
            ) {
                Text("Cerrar Sesión", color = RedSecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = ForegroundDark, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SlateMuted)
    }
}
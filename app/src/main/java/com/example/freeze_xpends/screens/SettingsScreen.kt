package com.example.freeze_xpends.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    isPremium: Boolean,
    userName: String = "Eri", // Parámetro listo para recibir datos del UserViewModel
    userEmail: String = "eri@email.com", // Parámetro listo para recibir datos del UserViewModel
    onNavigate: (String) -> Unit
) {
    // --- ESTADOS PARA LOS DIÁLOGOS ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Generador de iniciales dinámicas
    val initials = if (userName.isNotBlank()) userName.take(2).uppercase() else "XX"

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
            IconButton(onClick = { onNavigate("back") }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = "Ajustes",
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
                .padding(24.dp)
        ) {
            // --- 2. TARJETA DE PERFIL ---
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate("profile") },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(60.dp).background(PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(userName, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userEmail, fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isPremium) AccentGreen.copy(0.1f) else BackgroundSlate,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isPremium) "PRO" else "FREE",
                                    color = if (isPremium) AccentGreen else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. MENÚ PRINCIPAL ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsMenuRow(icon = Icons.Outlined.Person, title = "Perfil", onClick = { onNavigate("profile") })
                    HorizontalDivider(color = BorderSlate.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsMenuRow(
                        icon = Icons.Outlined.AttachMoney,
                        title = "Presupuesto (Gráficas)",
                        isLocked = !isPremium,
                        onClick = { if (isPremium) onNavigate("budget") else onNavigate("premium") }
                    )
                    HorizontalDivider(color = BorderSlate.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsMenuRow(
                        icon = Icons.Outlined.DateRange,
                        title = "Calendario",
                        isLocked = !isPremium,
                        onClick = { if (isPremium) onNavigate("calendar") else onNavigate("premium") }
                    )
                    HorizontalDivider(color = BorderSlate.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsMenuRow(icon = Icons.Outlined.HelpOutline, title = "Soporte", onClick = { onNavigate("support") })
                    HorizontalDivider(color = BorderSlate.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate("premium") }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = SecondaryRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Hazte Premium", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. PREFERENCIAS ---
            Text("PREFERENCIAS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { if(!isPremium) onNavigate("premium") }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Formato de visualización", fontSize = 12.sp, color = if (!isPremium) TextMuted else TextDark, fontWeight = FontWeight.Bold)
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryRed, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().background(BackgroundSlate, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("100,000.10", color = TextMuted, fontSize = 14.sp)
                                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, tint = TextMuted)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderSlate.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsMenuRow(icon = Icons.Outlined.Description, title = "Términos de uso", onClick = { /* Lógica términos */ })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. BOTONES PELIGROSOS ---

            // Botón Eliminar Datos
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = BorderStroke(1.dp, SecondaryRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = SecondaryRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Datos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryRed)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- DIÁLOGOS DE CONFIRMACIÓN ---

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("¿Eliminar todos los datos?", fontWeight = FontWeight.Bold) },
                    text = { Text("Esta acción es permanente y no se puede deshacer. Se borrarán todos tus registros de la cuenta.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                // Aiven/API logic here
                            }
                        ) {
                            Text("Sí, eliminar", color = SecondaryRed, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                    }
                )
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold) },
                    text = { Text("Tendrás que ingresar tus credenciales la próxima vez que quieras entrar a la app.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                onNavigate("logout")
                            }
                        ) {
                            Text("Cerrar Sesión", color = SecondaryRed, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isLocked) TextMuted else PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = if (isLocked) TextMuted else TextDark, modifier = Modifier.weight(1f))

        if (isLocked) {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryRed, modifier = Modifier.size(16.dp))
        } else {
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}
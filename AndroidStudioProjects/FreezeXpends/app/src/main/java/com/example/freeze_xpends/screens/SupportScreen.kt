package com.example.freeze_xpends.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // VITAL para los Intents
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

@Composable
fun SupportScreen(onNavigateBack: () -> Unit) {
    // Necesitamos el contexto para abrir otras apps (correo, teléfono)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
    ) {
        // --- HEADER AZUL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(PrimaryBlue),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = "Soporte",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. HERO SECTION ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).background(PrimaryBlue.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¿Cómo podemos ayudarte?", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estamos aquí para resolver tus dudas y\nproblemas", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. OPCIONES DE CONTACTO (AHORA SON FUNCIONALES) ---

            ContactOptionCard(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Chat en vivo",
                subtitle = "Respuesta inmediata",
                onClick = {
                    // Simulación con un mensajito Toast
                    Toast.makeText(context, "Conectando con un asesor...", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ContactOptionCard(
                icon = Icons.Outlined.Email,
                title = "Email",
                subtitle = "soporte@freezexpends.com",
                onClick = {
                    // Acción nativa para abrir la app de correos
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soporte@freezexpends.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Duda sobre Freeze-xpends")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ContactOptionCard(
                icon = Icons.Outlined.Phone,
                title = "Teléfono",
                subtitle = "+52 800 123 4567",
                onClick = {
                    // Acción nativa para abrir el marcador del celular
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:+528001234567")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. PREGUNTAS FRECUENTES (AHORA EXPANDIBLES) ---
            Text(
                text = "Preguntas frecuentes",
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Start
            )

            FaqItem(
                question = "¿Cómo agregar un gasto recurrente?",
                answer = "Para agregar un gasto recurrente, ve al botón rojo de 'Nuevo', selecciona el tipo de transacción y en la sección de 'Frecuencia / Plazo' elige la opción que necesites (Semanal, Quincenal, etc.). ¡Recuerda que requieres ser Premium para plazos personalizados!"
            )
            Spacer(modifier = Modifier.height(12.dp))
            FaqItem(
                question = "¿Cómo exportar mis datos?",
                answer = "Próximamente habilitaremos la opción de exportar tus reportes en PDF y Excel desde la pantalla de ajustes para los usuarios Premium."
            )
            Spacer(modifier = Modifier.height(12.dp))
            FaqItem(
                question = "¿Qué incluye la versión Premium?",
                answer = "La versión Premium elimina los anuncios publicitarios, te da acceso a las gráficas de presupuesto, habilita el calendario, te permite configurar recordatorios de pago y no tiene límites de gastos simultáneos."
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Actualizamos ContactOptionCard para recibir la acción de "onClick"
@Composable
fun ContactOptionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, // AQUI SE EJECUTA
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(PrimaryBlue.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text(subtitle, fontSize = 12.sp, color = TextMuted)
            }
            Icon(Icons.Outlined.OpenInNew, contentDescription = "Abrir", tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

// Actualizamos FaqItem para que sea un acordeón expandible
@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, // Alterna el estado
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )
                // Cambia el icono dependiendo si está abierto o cerrado
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
            // Si está expandido, muestra la respuesta
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderSlate.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
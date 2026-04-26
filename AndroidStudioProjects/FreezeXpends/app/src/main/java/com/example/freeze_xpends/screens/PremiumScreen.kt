package com.example.freeze_xpends.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*

@Composable
fun PremiumScreen(
    onAcceptPremium: () -> Unit // Al darle aceptar, se vuelve Premium y regresa
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Icono de Corona (usamos Star como placeholder nativo)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(RedSecondary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Star, contentDescription = "Premium", tint = RedSecondary, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TUS BENEFICIOS COMO\nUSUARIO PREMIUM SON:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = ForegroundDark,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Beneficios
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BeneficioItem("-SIN ANUNCIOS PUBLICITARIOS")
                BeneficioItem("-ACCESO A TODAS LAS FUNCIONES DE\nPERSONALIZACIÓN")
                BeneficioItem("-ACCESO A LAS GRÁFICAS DE COSTO")
                BeneficioItem("-IMÁGENES Y PLAZOS PERSONALIZADOS")
                BeneficioItem("-SIN LÍMITE DE GASTOS SIMULTÁNEOS")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Planes
        PlanCard(icono = Icons.Default.Bolt, titulo = "MENSUAL", sub = "ACCESO TOTAL", precio = "$25", periodo = "/ MES")

        Spacer(modifier = Modifier.height(16.dp))

        // Plan Destacado (Anual)
        Box(modifier = Modifier.fillMaxWidth()) {
            PlanCard(icono = Icons.Default.Star, titulo = "ANUAL", sub = "ACCESO TOTAL", precio = "$125", periodo = "/ AÑO", isDestacado = true)
            // Badge flotante
            Surface(
                color = BluePrimary, shape = RoundedCornerShape(100.dp),
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-10).dp)
            ) {
                Text("¡EL MÁS ELEGIDO!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PlanCard(icono = Icons.Default.AllInclusive, titulo = "DE POR VIDA", sub = "ACCESO TOTAL", precio = "$150", periodo = "PAGO ÚNICO")

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Aceptar
        Button(
            onClick = onAcceptPremium,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("ACEPTAR", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("CANCELA EN CUALQUIER MOMENTO DESDE LOS AJUSTES", color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BeneficioItem(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = BluePrimary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForegroundDark)
    }
}

@Composable
fun PlanCard(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, sub: String, precio: String, periodo: String, isDestacado: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isDestacado) BorderStroke(2.dp, BluePrimary) else BorderStroke(1.dp, BackgroundGray)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, null, tint = if(isDestacado) Color(0xFFF59E0B) else BluePrimary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(titulo, fontWeight = FontWeight.Black, fontSize = 18.sp, color = ForegroundDark)
                    Text(sub, color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(precio, fontWeight = FontWeight.Black, fontSize = 20.sp, color = ForegroundDark)
                Text(periodo, color = SlateMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
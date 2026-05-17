package com.example.freeze_xpends.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeze_xpends.theme.*
import kotlinx.coroutines.launch
import com.example.freeze_xpends.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onPurchaseSuccess: () -> Unit,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado para saber cuál plan está seleccionado (Default: Anual)
    var selectedPlan by remember { mutableStateOf("ANUAL") }

    // Estados para la pasarela de pago
    var showPaymentSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mapa de precios para mostrar en el ticket de compra
    val priceMap = mapOf(
        "MENSUAL" to "$25.00 MXN",
        "ANUAL" to "$125.00 MXN",
        "LIFETIME" to "$150.00 MXN"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fondo blanco como en la cap
    ) {
        // --- 1. HEADER AZUL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(PrimaryBlue),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onNavigate) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = "PREMIUM",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 2. ICONO CORONA (DIBUJADO NATIVAMENTE) ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFFEE2E2), CircleShape), // Rosa/Rojo muy claro
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CrownIcon, // Llama a la función de abajo
                    contentDescription = "Corona Premium",
                    tint = Color(0xFFEF4444), // Rojo suave
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TUS BENEFICIOS COMO\nUSUARIO PREMIUM SON:",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. TARJETA DE BENEFICIOS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    val benefits = listOf(
                        "-SIN ANUNCIOS PUBLICITARIOS",
                        "-ACCESO A TODAS LAS FUNCIONES DE PERSONALIZACION",
                        "-ACCESO A LAS GRAFICAS DE COSTO",
                        "-IMAGENES Y PLAZOS PERSONALIZADOS",
                        "-SIN LÍMITE DE GASTOS SIMULTÁNEOS"
                    )
                    benefits.forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFBFDBFE), // Azul claro de la cap
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = benefit,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. OPCIONES DE SUSCRIPCIÓN ---

            // MENSUAL
            SubscriptionPlanCard(
                title = "MENSUAL",
                price = "$25",
                period = "/ MES",
                icon = Icons.Default.Bolt,
                iconTint = Color(0xFF3B82F6),
                isSelected = selectedPlan == "MENSUAL",
                onClick = { selectedPlan = "MENSUAL" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ANUAL (Con Badge)
            Box {
                SubscriptionPlanCard(
                    title = "ANUAL",
                    price = "$125",
                    period = "/ AÑO",
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFF59E0B),
                    isSelected = selectedPlan == "ANUAL",
                    onClick = { selectedPlan = "ANUAL" }
                )
                // Badge "¡EL MÁS ELEGIDO!"
                Surface(
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = (-12).dp)
                ) {
                    Text(
                        "¡EL MÁS ELEGIDO!",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DE POR VIDA
            SubscriptionPlanCard(
                title = "DE POR VIDA",
                price = "$150",
                period = "PAGO ÚNICO",
                icon = Icons.Default.AllInclusive,
                iconTint = Color(0xFF6366F1),
                isSelected = selectedPlan == "LIFETIME",
                onClick = { selectedPlan = "LIFETIME" }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- 5. BOTÓN ACEPTAR (AHORA ABRE EL MODAL DE PAGO) ---
            Button(
                onClick = { showPaymentSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ACEPTAR", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
            }

            Text(
                text = "CANCELA EN CUALQUIER MOMENTO DESDE LOS AJUSTES",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
        }
    }

    // --- 6. PASARELA DE PAGO FALSA (GOOGLE PLAY STYLE) ---
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Cabecera Google Play
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Confirmar Suscripción", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detalles de la App
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Usamos Image para jalar tu logo real
                    Image(
                        painter = painterResource(id = R.drawable.logo_fx), // <-- CAMBIA ESTO POR EL NOMBRE DE TU LOGO
                        contentDescription = "Logo Freeze-Xpends",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue), // Le dejo un fondo azul por si tu logo es transparente
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Freeze-Xpends Premium", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                        Text("Suscripción ${selectedPlan.lowercase().replaceFirstChar { it.uppercase() }}", fontSize = 14.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = BorderSlate)
                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = "Tarjeta", tint = TextMuted, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Mastercard **** 4291", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Icon(Icons.Default.Star, contentDescription = "Seleccionar", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de compra
                Button(
                    onClick = {
                        scope.launch {
                            sheetState.hide() // Ocultamos la pasarela con animación
                            showPaymentSheet = false
                            Toast.makeText(context, "Pago exitoso. ¡Bienvenido a Premium!", Toast.LENGTH_LONG).show()
                            onPurchaseSuccess() // Ahora sí le decimos a Node.js que somos Premium
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen), // Verde de compra
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Suscribirse por ${priceMap[selectedPlan]}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp)) // Espacio final del bottom sheet
            }
        }
    }
}

// --- COMPONENTES Y VECTORES AUXILIARES ---

@Composable
fun SubscriptionPlanCard(
    title: String, price: String, period: String,
    icon: ImageVector,
    iconTint: Color, isSelected: Boolean, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, if (isSelected) PrimaryBlue else Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(iconTint.copy(0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                Text("ACCESO TOTAL", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(price, fontWeight = FontWeight.Black, fontSize = 22.sp, color = TextDark)
                Text(period, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Corona dibujada nativamente con vectores de Compose
val CrownIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Crown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color(0xFFEF4444)),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 21f)
            lineTo(18f, 21f)
            moveTo(5f, 17f)
            lineTo(3f, 7f)
            lineTo(9f, 11f)
            lineTo(12f, 3f)
            lineTo(15f, 11f)
            lineTo(21f, 7f)
            lineTo(19f, 17f)
            close()
        }
    }.build()
package com.example.freeze_xpends.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeze_xpends.network.RetrofitClient
import com.example.freeze_xpends.network.GastoRequest
import com.example.freeze_xpends.network.IngresoRequest
import com.example.freeze_xpends.network.RecordatorioRequest
import com.example.freeze_xpends.network.Categoria
import com.example.freeze_xpends.theme.*
import com.example.freeze_xpends.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    userViewModel: UserViewModel,
    isPremium: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val userId by userViewModel.userId.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var isExpense by rememberSaveable { mutableStateOf(true) }
    val primaryColor = if (isExpense) SecondaryRed else AccentGreen
    val headerTitle = if (isExpense) "NUEVO GASTO" else "NUEVO INGRESO"

    var amount by rememberSaveable { mutableStateOf("") }
    var concept by rememberSaveable { mutableStateOf("") }

    var date by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
        )
    }

    var categories by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Categoria?>(null) }
    var expandedCat by remember { mutableStateOf(false) }

    var expandedFrequency by remember { mutableStateOf(false) }
    var selectedFrequency by rememberSaveable { mutableStateOf("Pago Único") }
    val frequencies = listOf("Pago Único", "Semanal", "Quincenal", "Mensual", "Anual")

    var isReminderActive by rememberSaveable { mutableStateOf(false) }
    var reminderDate by rememberSaveable { mutableStateOf("") }
    var reminderTime by rememberSaveable { mutableStateOf("") }

    // --- PERMISO DE NOTIFICACIONES PARA ANDROID 13+ ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Se requiere permiso para las notificaciones", Toast.LENGTH_SHORT).show()
            isReminderActive = false
        }
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedImageUri = uri
            } catch (e: Exception) {
                e.printStackTrace()
                selectedImageUri = uri
            }
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val reminderCalendar = Calendar.getInstance()
    val reminderDatePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> reminderDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth) },
        reminderCalendar.get(Calendar.YEAR), reminderCalendar.get(Calendar.MONTH), reminderCalendar.get(Calendar.DAY_OF_MONTH)
    )

    val reminderTimePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute -> reminderTime = String.format("%02d:%02d", hourOfDay, minute) },
        reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), true
    )

    LaunchedEffect(isExpense) {
        try {
            val response = if (isExpense) RetrofitClient.instance.getCategoriasGastos(userId)
            else RetrofitClient.instance.getCategoriasIngresos(userId)

            if (response.isSuccessful) {
                categories = response.body()?.data ?: emptyList()
                selectedCategory = categories.firstOrNull()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando categorías", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundSlate,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().height(64.dp).background(primaryColor),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text(headerTitle, modifier = Modifier.fillMaxWidth().padding(end = 48.dp), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderSlate, RoundedCornerShape(12.dp)).padding(4.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isExpense) SecondaryRed else Color.Transparent).clickable { isExpense = true }, contentAlignment = Alignment.Center) {
                    Text("Gasto", color = if (isExpense) Color.White else TextMuted, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (!isExpense) AccentGreen else Color.Transparent).clickable { isExpense = false }, contentAlignment = Alignment.Center) {
                    Text("Ingreso", color = if (!isExpense) Color.White else TextMuted, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("ICONO / FOTO DEL RECIBO", Icons.Default.Image, isLocked = !isPremium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isPremium) {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else onNavigateToPremium()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Foto seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp).background(PrimaryBlue.copy(0.1f), CircleShape).padding(4.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("SUBIR IMAGEN DEL RECIBO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("MONTO", Icons.Default.AttachMoney)
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                placeholder = { Text("0.00", fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextDark),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("CONCEPTO", Icons.Default.Description)
            OutlinedTextField(
                value = concept, onValueChange = { concept = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("FECHA DE INICIO", Icons.Default.DateRange)
            Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, null, tint = primaryColor) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextDark, disabledBorderColor = BorderSlate, disabledTrailingIconColor = primaryColor)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("CATEGORÍA", Icons.Default.Category)
            ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = !expandedCat }) {
                OutlinedTextField(
                    value = selectedCategory?.nombre_categoria ?: "Seleccionar...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
                )
                ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.nombre_categoria) },
                            onClick = { selectedCategory = cat; expandedCat = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("FRECUENCIA / PLAZO", Icons.Default.Sync, isLocked = !isPremium)
            ExposedDropdownMenuBox(
                expanded = expandedFrequency,
                onExpandedChange = { if (isPremium) expandedFrequency = !expandedFrequency else onNavigateToPremium() }
            ) {
                OutlinedTextField(
                    value = selectedFrequency, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderSlate, focusedBorderColor = primaryColor)
                )
                if (isPremium) {
                    ExposedDropdownMenu(expanded = expandedFrequency, onDismissRequest = { expandedFrequency = false }) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(text = { Text(freq) }, onClick = { selectedFrequency = freq; expandedFrequency = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isPremium) onNavigateToPremium()
                    else {
                        isReminderActive = !isReminderActive
                        // Pedir permiso si se activa y es Android 13+
                        if (isReminderActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (isReminderActive) PrimaryBlue else BorderSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(if (isReminderActive) PrimaryBlue.copy(0.1f) else BackgroundSlate, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsNone, null, tint = if (isReminderActive) PrimaryBlue else TextMuted)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ACTIVAR RECORDATORIO", fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextDark)
                            Text("Recibe una alerta antes del pago", fontSize = 10.sp, color = TextMuted)
                        }
                        if (!isPremium) Icon(Icons.Default.Lock, null, tint = SecondaryRed, modifier = Modifier.size(20.dp))
                    }
                    if (isReminderActive && isPremium) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f).clickable { reminderDatePickerDialog.show() }) {
                                OutlinedTextField(
                                    value = reminderDate, onValueChange = {}, readOnly = true, enabled = false,
                                    placeholder = { Text("Fecha", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextDark, disabledBorderColor = BorderSlate)
                                )
                            }
                            Box(modifier = Modifier.weight(1f).clickable { reminderTimePickerDialog.show() }) {
                                OutlinedTextField(
                                    value = reminderTime, onValueChange = {}, readOnly = true, enabled = false,
                                    placeholder = { Text("Hora", fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextDark, disabledBorderColor = BorderSlate)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (amount.isNotBlank() && concept.isNotBlank() && selectedCategory != null) {
                        scope.launch {
                            isLoading = true
                            try {
                                val montoDouble = amount.toDoubleOrNull() ?: 0.0
                                val isUnico = selectedFrequency == "Pago Único"
                                val plazoEnviar = if (isUnico) "ÚNICO" else selectedFrequency
                                val estatusInicial = if (isUnico) 1 else 0
                                val imagenString = selectedImageUri?.toString()

                                val response = if (isExpense) {
                                    RetrofitClient.instance.addGasto(
                                        GastoRequest(user_id = userId, categoria_id = selectedCategory!!.categoria_id, fecha_gasto = date, nombre_gasto = concept, descripcion = null, plazo = plazoEnviar, monto_gasto = montoDouble, imagen_uri = imagenString)
                                    )
                                } else {
                                    RetrofitClient.instance.addIngreso(
                                        IngresoRequest(user_id = userId, categoria_id = selectedCategory!!.categoria_id, fecha_ingreso = date, nombre_ingreso = concept, descripcion = null, monto = montoDouble, recibido = estatusInicial, plazo = plazoEnviar, imagen_uri = imagenString)
                                    )
                                }

                                if (response.isSuccessful) {
                                    if (isReminderActive && isPremium && reminderDate.isNotBlank() && reminderTime.isNotBlank()) {
                                        val fechaYHora = "${reminderDate} ${reminderTime}:00"
                                        RetrofitClient.instance.addRecordatorio(RecordatorioRequest(userId, fechaYHora, concept))

                                        // --- MAGIA: PROGRAMAR ALARMA LOCAL ---
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                        try {
                                            val dateObj = sdf.parse(fechaYHora)
                                            if (dateObj != null) {
                                                com.example.freeze_xpends.utils.scheduleNotification(
                                                    context = context,
                                                    timeInMillis = dateObj.time,
                                                    title = "¡Recordatorio: $concept!",
                                                    message = "Tienes este movimiento programado para hoy.",
                                                    notificationId = (1..100000).random()
                                                )
                                            }
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }

                                    Toast.makeText(context, "¡Transacción guardada!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } else {
                                    Toast.makeText(context, "Error en el servidor", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Datos inválidos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("GUARDAR TRANSACCIÓN", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isLocked: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }
        if (isLocked) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = SecondaryRed, modifier = Modifier.size(14.dp))
        }
    }
}
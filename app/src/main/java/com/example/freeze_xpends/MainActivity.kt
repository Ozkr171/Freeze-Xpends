package com.example.freeze_xpends

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freeze_xpends.screens.*
import com.example.freeze_xpends.utils.SessionManager
import com.example.freeze_xpends.viewmodels.UserViewModel

import kotlinx.coroutines.launch
import com.example.freeze_xpends.network.RetrofitClient

class MainActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        // Cargar sesión persistente si existe
        if (sessionManager.isLoggedIn()) {
            userViewModel.setUserData(
                id = sessionManager.getUserId(),
                name = sessionManager.getNombre() ?: "Usuario",
                email = sessionManager.getEmail(),
                isPremium = sessionManager.isPremium()
            )
        }

        setContent {
            val isPremium by userViewModel.isPremium.collectAsState()
            val userName by userViewModel.userName.collectAsState()

            AppNavigation(
                userViewModel = userViewModel,
                isPremium = isPremium,
                userName = userName,
                sessionManager = sessionManager
            )
        }
    }
}

@Composable
fun AppNavigation(
    userViewModel: UserViewModel,
    isPremium: Boolean,
    userName: String,
    sessionManager: SessionManager
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val navController = rememberNavController()

    // RECOLECTAMOS EL ID DE FORMA SEGURA PARA TODO EL NAVHOST
    val currentUserId by userViewModel.userId.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (sessionManager.isLoggedIn()) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                userViewModel = userViewModel,
                sessionManager = sessionManager,
                onNavigate = { route ->
                    if (route == "home") {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate(route)
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }

        composable("forgot-password") {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("home") {
            HomeScreen(
                isPremium = isPremium,
                userName = userName,
                userId = currentUserId,
                userViewModel = userViewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("settings") {
            SettingsScreen(
                userViewModel = userViewModel, // <-- PASAMOS EL VIEWMODEL COMPLETO
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    sessionManager.clearSession()
                    userViewModel.clearData()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    scope.launch {
                        try {
                            // 1. Le decimos a Node.js que borre al usuario de Aiven (Cascada)
                            val res = RetrofitClient.instance.deleteUsuario(currentUserId)

                            if (res.isSuccessful) {
                                Toast.makeText(context, "Cuenta eliminada para siempre 💥", Toast.LENGTH_LONG).show()

                                // 2. Limpiamos los datos locales del celular
                                sessionManager.clearSession()
                                userViewModel.clearData()

                                // 3. Lo mandamos a patadas a la pantalla de Login
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("add-expense") {
            AddExpenseScreen(
                userViewModel = userViewModel,
                isPremium = isPremium,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.navigate("premium") }
            )
        }

        composable(route = "calendar") {
            CalendarScreen(
                userId = currentUserId, // <-- Usamos el estado seguro
                isPremium = isPremium,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(route = "budget") {
            BudgetScreen(
                userId = currentUserId,
                isPremium = isPremium,  
                onNavigateBack = { navController.popBackStack() },
                userViewModel = userViewModel,
                onNavigateToPremium = { navController.navigate("premium") }
            )
        }

        composable("support") { SupportScreen(onNavigateBack = { navController.popBackStack() }) }

        composable("premium") {
            PremiumScreen(
                onPurchaseSuccess = {
                    // Actualizamos la sesión con el ViewModel
                    userViewModel.setUserData(
                        id = userViewModel.userId.value,
                        name = userViewModel.userName.value,
                        email = userViewModel.userEmail.value,
                        isPremium = true
                    )
                    sessionManager.saveSession(
                        userId = userViewModel.userId.value,
                        nombre = userViewModel.userName.value,
                        email = userViewModel.userEmail.value,
                        isPremium = true
                    )
                    navController.popBackStack()
                },
                onNavigate = { navController.popBackStack() }
            )
        }
    }
}
package com.example.freeze_xpends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freeze_xpends.screens.*
import com.example.freeze_xpends.utils.SessionManager
import com.example.freeze_xpends.viewmodels.UserViewModel

class MainActivity : ComponentActivity() {

    // Inicializamos el ViewModel y el SessionManager
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        // Cargar sesión persistente si existe
        if (sessionManager.isLoggedIn()) {
            userViewModel.setUsuario(
                id = sessionManager.getUserId(),
                nombreStr = sessionManager.getNombre() ?: "Usuario",
                premium = sessionManager.isPremium()
            )
        }

        setContent {
            // Observamos el estado del premium desde el ViewModel
            val isPremium by userViewModel.isPremium.collectAsState()
            val userName by userViewModel.nombre.collectAsState()

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
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        // Si ya está logueado, mándalo directo al home, si no, al login
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
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("settings") {
            SettingsScreen(
                isPremium = isPremium,
                userName = userName,
                onNavigate = { route ->
                    when (route) {
                        "back" -> navController.popBackStack()
                        "logout" -> {
                            sessionManager.clearSession()
                            userViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        else -> navController.navigate(route)
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
                userViewModel = userViewModel, // <--- No olvides pasarle esto
                isPremium = isPremium,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.navigate("premium") }
            )
        }
        // ... El resto de tus rutas (calendar, budget, etc) se quedan igual ...
        composable("calendar") { CalendarScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("budget") { BudgetScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("support") { SupportScreen(onNavigateBack = { navController.popBackStack() }) }

        composable("premium") {
            PremiumScreen(
                onPurchaseSuccess = {
                    userViewModel.setPremium(true)
                    sessionManager.saveSession(
                        userViewModel.userId.value,
                        userViewModel.nombre.value,
                        true
                    )
                    navController.popBackStack()
                },
                onNavigate = { navController.popBackStack() }
            )
        }
    }
}
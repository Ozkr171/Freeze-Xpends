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

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        // Cargar sesión persistente si existe
        if (sessionManager.isLoggedIn()) {
            userViewModel.setUserData( // <-- CORREGIDO A setUserData
                id = sessionManager.getUserId(),
                name = sessionManager.getNombre() ?: "Usuario",
                email = sessionManager.getEmail(), // Recuperamos el email persistente
                isPremium = sessionManager.isPremium()
            )
        }

        setContent {
            val isPremium by userViewModel.isPremium.collectAsState()
            val userName by userViewModel.userName.collectAsState() // <-- CORREGIDO DE 'nombre' A 'userName'

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
            val currentUserId = userViewModel.userId.collectAsState().value

            HomeScreen(
                isPremium = isPremium,
                userName = userName,
                userId = currentUserId,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("settings") {
            val userEmail by userViewModel.userEmail.collectAsState()

            SettingsScreen(
                userEmail = userEmail,
                userName = userName,
                isPremium = isPremium, // <-- CORREGIDO EL ERROR ROJO
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }, // <-- CORREGIDO EL ERROR ROJO
                onLogout = {
                    sessionManager.clearSession()
                    userViewModel.clearData()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {  }
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

        composable("calendar") { CalendarScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("budget") { BudgetScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("support") { SupportScreen(onNavigateBack = { navController.popBackStack() }) }

        composable("premium") {
            PremiumScreen(
                onPurchaseSuccess = {
                    // Actualizamos usando setUserData
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
package com.example.freeze_xpends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freeze_xpends.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppNavigation() }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isPremium by remember { mutableStateOf(false) }


    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgot = { navController.navigate("forgot_password") }, // <--- Conecta aquí
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onNavigateToPremium = { navController.navigate("premium") }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("register") {
            RegisterScreen(onNavigateToLogin = { navController.navigate("login") })
        }
        composable("home") {
            HomeScreen(
                isPremium = isPremium,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToAdd = { navController.navigate("add_expense") },
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToPremium = { navController.navigate("premium") } // Nuevo
            )
        }
        composable("premium") {
            PremiumScreen(
                onAcceptPremium = {
                    isPremium = true // Se activa la magia
                    navController.popBackStack() // Te regresa a donde estabas
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { },
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToBudget = { },
                onNavigateToPremium = { navController.navigate("premium") },
                onLogout = { navController.navigate("login") { popUpTo("home") { inclusive = true } } }
            )
        }
        composable("calendar") {
            CalendarScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("add_expense") {
            AddExpenseScreen(
                isPremium = isPremium,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.navigate("premium") }
            )
        }
    }
}
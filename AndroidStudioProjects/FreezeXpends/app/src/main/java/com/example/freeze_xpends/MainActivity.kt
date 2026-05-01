package com.example.freeze_xpends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freeze_xpends.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Switch de estado global: Empieza en false (Free)
    // rememberSaveable hace que no se resetee si giras la pantalla
    var isPremium by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // --- AUTH ---
        // --- AUTH ---
        composable(route = "login") {
            LoginScreen(
                onNavigate = { route ->
                    // Si vamos al home, limpiamos el login para que el botón de "Atrás" no los regrese aquí
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

        // Dentro de tu NavHost en MainActivity.kt
        composable("forgot-password") {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        // --- APP PRINCIPAL ---
        composable("home") {
            HomeScreen(
                isPremium = isPremium, // Pasamos el estado para quitar anuncios/candados
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("add-expense") {
            AddExpenseScreen(
                isPremium = isPremium, // Le pasas el estado global
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.navigate("premium") } // Lo manda a comprar si toca un candado
            )
        }

        composable("settings") {
            SettingsScreen(
                isPremium = isPremium,
                onNavigate = { route ->
                    if (route == "back") navController.popBackStack()
                    else navController.navigate(route)
                }
            )
        }
        composable("profile") {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("support") {
            SupportScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("budget") { BudgetScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("calendar") { CalendarScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("calendar") {
            CalendarScreen(onNavigateBack = { navController.popBackStack() })
        }
        // --- PREMIUM LOGIC ---
        composable("premium") {
            PremiumScreen(
                onPurchaseSuccess = {
                    isPremium = true
                    navController.popBackStack()
                },
                onNavigate = { navController.popBackStack() }

            )
        }
    }
}
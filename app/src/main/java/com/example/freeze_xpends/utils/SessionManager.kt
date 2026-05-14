package com.example.freeze_xpends.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("freeze_xpends_prefs", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, nombre: String, email: String, isPremium: Boolean) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("nombre", nombre)
            putString("email", email) // <-- AHORA GUARDAMOS EL EMAIL
            putBoolean("is_premium", isPremium)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun getNombre(): String? = prefs.getString("nombre", null)

    fun getEmail(): String = prefs.getString("email", "") ?: "" // <-- AHORA LO PODEMOS LEER

    fun isPremium(): Boolean = prefs.getBoolean("is_premium", false)

    fun isLoggedIn(): Boolean = getUserId() != -1

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
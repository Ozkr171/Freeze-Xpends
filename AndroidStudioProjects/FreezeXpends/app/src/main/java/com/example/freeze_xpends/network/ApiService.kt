package com.example.freeze_xpends.network

import retrofit2.http.Body
import retrofit2.http.POST

// Modelos para el Login (Deben coincidir con tu index.js)
data class LoginRequest(
    val correo_electronico: String, // Antes tenías 'email'
    val contrasena: String          // Antes tenías 'pass'
)

data class LoginResponse(
    val mensaje: String,
    val user_id: Int?,
    val nombre: String?,
    val premium: Int?
)

// Modelos para el Registro
data class RegisterRequest(
    val nombre_s: String,           // Campo exacto de tu index.js
    val correo_electronico: String,
    val contrasena: String
)

data class RegisterResponse(
    val mensaje: String,
    val user_id: Int?
)

interface ApiService {
    @POST("api/login") // Ruta completa según tu backend
    suspend fun loginUser(@Body request: LoginRequest): retrofit2.Response<LoginResponse>

    @POST("api/registro") // Ruta completa según tu backend
    suspend fun registerUser(@Body request: RegisterRequest): retrofit2.Response<RegisterResponse>
}
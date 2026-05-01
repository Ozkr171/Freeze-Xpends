package com.example.freeze_xpends.network

import retrofit2.Response
import retrofit2.http.*

// MODELOS DE DATOS
data class LoginRequest(val correo_electronico: String, val contrasena: String)
data class LoginResponse(val mensaje: String, val user_id: Int?, val nombre: String?, val premium: Int?)

data class RegisterRequest(val nombre_s: String, val correo_electronico: String, val contrasena: String)
data class RegisterResponse(val mensaje: String, val user_id: Int?)

data class ForgotPasswordRequest(val correo_electronico: String)
data class ResetPasswordRequest(val correo_electronico: String, val codigo: String, val nueva_contrasena: String)

data class Gasto(val monto_gasto: Double, val concepto: String, val categoria: String)
data class GastoResponse(val gastos: List<Gasto>)
data class Ingreso(val monto: Double)

interface ApiService {
    @POST("api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/registro")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Map<String, String>>

    @POST("api/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Map<String, String>>

    @GET("api/gastos/{user_id}")
    suspend fun getGastos(@Path("user_id") userId: Int): Response<GastoResponse>

    @GET("api/ingresos/{user_id}")
    suspend fun getIngresos(@Path("user_id") userId: Int): Response<List<Ingreso>>
}
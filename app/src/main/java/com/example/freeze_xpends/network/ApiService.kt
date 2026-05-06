package com.example.freeze_xpends.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- WRAPPER GLOBAL (Coincide con la respuesta del index.js) ---
data class ApiResponse<T>(
    val mensaje: String,
    val data: T? = null
)

// --- MODELOS REQUEST ---
data class LoginResponse(
    val mensaje: String,
    val user_id: Int?,
    val nombre: String?,
    val premium: Int?
)

data class TransactionRequest(
    val user_id: Int,
    val titulo: String,
    val categoria: String,
    val monto: Double,
    val tipo: String, // "INGRESO" o "GASTO"
    val fecha: String // Formato YYYY-MM-DD
)

data class SimpleResponse(
    val mensaje: String
)

data class LoginRequest(val correo_electronico: String, val contrasena: String)

data class RegisterRequest(val nombre_s: String, val correo_electronico: String, val contrasena: String)

data class ForgotPasswordRequest(val correo_electronico: String)

data class ResetPasswordRequest(val correo_electronico: String, val codigo: String, val nueva_contrasena: String)

// --- MODELOS RESPONSE (Lo que viene dentro de 'data') ---
data class UserData(
    val user_id: Int,
    val nombre: String?,   // Para el endpoint de login
    val nombre_s: String?, // Para el endpoint de perfil
    val premium: Int       // MySQL manda 0 o 1
)

data class Categoria(
    val categoria_id: Int,
    val user_id: Int,
    val nombre_categoria: String
)

data class Gasto(val monto_gasto: Double, val concepto: String, val categoria: String)

data class Ingreso(val monto: Double)

// --- INTERFAZ RETROFIT ---
interface ApiService {

    @POST("api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/registro")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ApiResponse<UserData>>

    @GET("api/usuarios/{user_id}")
    suspend fun getPerfil(@Path("user_id") userId: Int): Response<ApiResponse<UserData>>

    @GET("api/categorias/gastos/{user_id}")
    suspend fun getCategoriasGastos(@Path("user_id") userId: Int): Response<ApiResponse<List<Categoria>>>

    @GET("api/categorias/ingresos/{user_id}")
    suspend fun getCategoriasIngresos(@Path("user_id") userId: Int): Response<ApiResponse<List<Categoria>>>

    // Rutas anteriores adaptadas al nuevo formato
    @GET("api/gastos/{user_id}")
    suspend fun getGastos(@Path("user_id") userId: Int): Response<ApiResponse<List<Gasto>>>

    @GET("api/ingresos/{user_id}")
    suspend fun getIngresos(@Path("user_id") userId: Int): Response<ApiResponse<List<Ingreso>>>

    // (Pendientes de implementar en el backend - Fase 4)
    @POST("api/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<String>>

    @POST("api/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<String>>

    @POST("api/transacciones/add")
    suspend fun addTransaction(@Body request: TransactionRequest): Response<SimpleResponse>
}
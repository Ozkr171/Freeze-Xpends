package com.example.freeze_xpends.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- WRAPPER GLOBAL ---
data class ApiResponse<T>(
    val mensaje: String,
    val data: T? = null
)

// --- MODELOS DE LOGIN ---
data class LoginResponse(
    val mensaje: String,
    val data: LoginData?
)

data class LoginData(
    val user_id: Int,
    val nombre: String?,
    val premium: Int
)

// --- MODELOS REQUEST ---
data class GastoRequest(
    val user_id: Int,
    val categoria_id: Int, // Ya no es opcional para reportes sanos
    val fecha_gasto: String,
    val nombre_gasto: String,
    val descripcion: String?,
    val plazo: String?,
    val monto_gasto: Double
)

data class IngresoRequest(
    val user_id: Int,
    val categoria_id: Int, // Ya no es opcional para reportes sanos
    val fecha_ingreso: String,
    val nombre_ingreso: String,
    val descripcion: String?,
    val monto: Double,
    val recibido: Int
)

data class LoginRequest(val correo_electronico: String, val contrasena: String)
data class RegisterRequest(val nombre_s: String, val correo_electronico: String, val contrasena: String)

// --- MODELOS RESPONSE ---
data class Categoria(
    val categoria_id: Int,
    val user_id: Int,
    val nombre_categoria: String,
    val tipo: String? // "GASTO" o "INGRESO"
)

data class Gasto(
    val gasto_id: Int,
    val user_id: Int,
    val categoria_id: Int?,
    val fecha_gasto: String,
    val nombre_gasto: String,
    val monto_gasto: Double
)

data class Ingreso(
    val ingreso_id: Int,
    val user_id: Int,
    val categoria_id: Int?,
    val fecha_ingreso: String,
    val nombre_ingreso: String,
    val monto: Double,
    val recibido: Int,
    val nombre_categoria: String?
)

// --- INTERFAZ RETROFIT ---
interface ApiService {

    @POST("api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/registro")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ApiResponse<LoginData>>

    // CATEGORÍAS
    @GET("api/categorias/gastos/{user_id}")
    suspend fun getCategoriasGastos(@Path("user_id") userId: Int): Response<ApiResponse<List<Categoria>>>

    @GET("api/categorias/ingresos/{user_id}")
    suspend fun getCategoriasIngresos(@Path("user_id") userId: Int): Response<ApiResponse<List<Categoria>>>

    // TRANSACCIONES
    @GET("api/gastos/{user_id}")
    suspend fun getGastos(@Path("user_id") userId: Int): Response<ApiResponse<List<Gasto>>>

    @GET("api/ingresos/{user_id}")
    suspend fun getIngresos(@Path("user_id") userId: Int): Response<ApiResponse<List<Ingreso>>>

    @POST("api/gastos")
    suspend fun addGasto(@Body request: GastoRequest): Response<ApiResponse<Any>>

    @POST("api/ingresos")
    suspend fun addIngreso(@Body request: IngresoRequest): Response<ApiResponse<Any>>
}
package com.example.freeze_xpends.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// --- WRAPPER GLOBAL ---
data class ApiResponse<T>(
    val mensaje: String,
    val data: T? = null
)

// --- MODELOS DE LOGIN ---
data class LoginResponse(
    val mensaje: String,
    val user_id: Int?,
    val nombre: String?,
    val premium: Int?
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
    val monto_gasto: Double,
    val imagen_uri: String? = null
)

data class IngresoRequest(
    val user_id: Int,
    val categoria_id: Int, // Ya no es opcional para reportes sanos
    val fecha_ingreso: String,
    val nombre_ingreso: String,
    val descripcion: String?,
    val monto: Double,
    val recibido: Int,
    val imagen_uri: String? = null
)

data class LoginRequest(val correo_electronico: String, val contrasena: String)
data class RegisterRequest(val nombre_s: String, val correo_electronico: String, val contrasena: String)

// --- MODELOS RESPONSE ---
data class Categoria(
    val categoria_id: Int,
    val user_id: Int,
    val nombre_categoria: String,
    val limite_presupuesto: Double? = 0.0,
    val tipo: String? // "GASTO" o "INGRESO"
)

data class Gasto(
    val gasto_id: Int,
    val user_id: Int,
    val categoria_id: Int?,
    val fecha_gasto: String,
    val nombre_gasto: String,
    val descripcion: String?, // Restaurado
    val plazo: String?,       // <-- EL CULPABLE DEL ERROR
    val monto_gasto: Double,
    val imagen_uri: String?   // Restaurado
)
data class UserData(
    val user_id: Int,
    val nombre: String?,
    val nombre_s: String?,
    val premium: Int
)
data class Ingreso(
    val ingreso_id: Int,
    val user_id: Int,
    val categoria_id: Int?,
    val fecha_ingreso: String,
    val nombre_ingreso: String,
    val descripcion: String?, // Restaurado
    val monto: Double,
    val recibido: Int,
    val imagen_uri: String?,  // Restaurado
    val nombre_categoria: String?
)

data class PresupuestoGlobalResponse(val presupuesto_global: Double)
data class PresupuestoGlobalRequest(val presupuesto_global: Double)
data class LimiteCategoriaRequest(val limite_presupuesto: Double)
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

    @GET("api/usuarios/{user_id}")
    suspend fun getPerfil(@Path("user_id") userId: Int): Response<ApiResponse<UserData>>

    // --- RUTAS DE EDICIÓN Y BORRADO ---
    @PUT("api/gastos/{id}")
    suspend fun updateGasto(@Path("id") id: Int, @Body request: GastoRequest): Response<ApiResponse<Any>>

    @PUT("api/ingresos/{id}")
    suspend fun updateIngreso(@Path("id") id: Int, @Body request: IngresoRequest): Response<ApiResponse<Any>>

    @DELETE("api/gastos/{id}")
    suspend fun deleteGasto(@Path("id") id: Int): Response<ApiResponse<Any>>

    @DELETE("api/ingresos/{id}")
    suspend fun deleteIngreso(@Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/usuarios/{user_id}/presupuesto")
    suspend fun getPresupuestoGlobal(@Path("user_id") userId: Int): Response<PresupuestoGlobalResponse>

    @PUT("api/usuarios/{user_id}/presupuesto")
    suspend fun updatePresupuestoGlobal(@Path("user_id") userId: Int, @Body request: PresupuestoGlobalRequest): Response<ApiResponse<Any>>

    @PUT("api/categorias/gastos/{categoria_id}/presupuesto")
    suspend fun updateLimiteCategoria(@Path("categoria_id") categoriaId: Int, @Body request: LimiteCategoriaRequest): Response<ApiResponse<Any>>

}
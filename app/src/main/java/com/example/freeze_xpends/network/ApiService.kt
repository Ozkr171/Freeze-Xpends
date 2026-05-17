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
    val plazo: String? = "ÚNICO",
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
    val plazo: String? = "ÚNICO",
    val monto_gasto: Double,
    val imagen_uri: String?,   // Restaurado
    val completado: Int? = 0
)
data class UserData(
    val user_id: Int,
    val nombre: String?,
    val nombre_s: String?,
    val correo_electronico: String?,
    val premium: Int,
    val divisa: String?,
    val foto_perfil: String?
)
data class Ingreso(
    val ingreso_id: Int,
    val user_id: Int,
    val categoria_id: Int?,
    val fecha_ingreso: String,
    val nombre_ingreso: String,
    val descripcion: String?,
    val monto: Double,
    val recibido: Int? = 1,
    val plazo: String? = "ÚNICO",
    val imagen_uri: String?,
    val nombre_categoria: String?
)

data class UpdatePerfilRequest(
    val nombre_s: String,
    val correo_electronico: String,
    val contrasena: String? = null,
    val divisa: String,
    val foto_perfil: String?
)


data class PresupuestoGlobalResponse(val presupuesto_global: Double)
data class PresupuestoGlobalRequest(val presupuesto_global: Double)
data class LimiteCategoriaRequest(val limite_presupuesto: Double)
data class NuevaCategoriaGastoRequest(val user_id: Int, val nombre_categoria: String, val limite_presupuesto: Double = 0.0)
data class NuevaCategoriaIngresoRequest(val user_id: Int, val nombre_categoria: String)
data class EstatusGastoRequest(val completado: Int)
data class EstatusIngresoRequest(val recibido: Int)

data class Nota(val nota_id: Int, val user_id: Int, val fecha: String, val texto: String)
data class NuevaNota(val user_id: Int, val fecha: String, val texto: String)
data class ActualizarNota(val texto: String)

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

    @PUT("api/gastos/{id}/estatus")
    suspend fun updateEstatusGasto(@Path("id") id: Int, @Body request: EstatusGastoRequest): Response<ApiResponse<Any>>

    @PUT("api/ingresos/{id}/estatus")
    suspend fun updateEstatusIngreso(@Path("id") id: Int, @Body request: EstatusIngresoRequest): Response<ApiResponse<Any>>

    @POST("api/categorias/gastos")
    suspend fun addCategoriaGasto(@Body request: NuevaCategoriaGastoRequest): Response<ApiResponse<Any>>

    @POST("api/categorias/ingresos")
    suspend fun addCategoriaIngreso(@Body request: NuevaCategoriaIngresoRequest): Response<ApiResponse<Any>>

    @PUT("api/usuarios/{user_id}")
    suspend fun updatePerfil(@Path("user_id") userId: Int, @Body request: UpdatePerfilRequest): Response<ApiResponse<Any>>

    @GET("api/notas/{user_id}/{fecha}")
    suspend fun getNotas(@Path("user_id") userId: Int, @Path("fecha") fecha: String): Response<ApiResponse<List<Nota>>>

    @POST("api/notas")
    suspend fun addNota(@Body request: NuevaNota): Response<ApiResponse<Any>>

    @DELETE("api/notas/{nota_id}")
    suspend fun deleteNota(@Path("nota_id") notaId: Int): Response<ApiResponse<Any>>

    @PUT("api/notas/{nota_id}")
    suspend fun updateNota(@Path("nota_id") notaId: Int, @Body request: ActualizarNota): Response<ApiResponse<Any>>

    @DELETE("api/usuarios/{user_id}")
    suspend fun deleteUsuario(@Path("user_id") userId: Int): Response<ApiResponse<Any>>
}
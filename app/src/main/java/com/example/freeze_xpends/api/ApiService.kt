package com.example.freeze_xpends.api



import com.example.freezexpends.models.Gasto
import com.example.freezexpends.models.Ingreso
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==========================================
    // USUARIOS (Login y Registro)
    // ==========================================
    @POST("api/registro")
    suspend fun registrarUsuario(@Body datosRegistro: Map<String, String>): Response<Map<String, Any>>

    @POST("api/login")
    suspend fun iniciarSesion(@Body credenciales: Map<String, String>): Response<Map<String, Any>>

    @PUT("api/usuarios/{user_id}/premium")
    suspend fun actualizarPremium(
        @Path("user_id") userId: Int,
        @Body premiumStatus: Map<String, Int>
    ): Response<Map<String, String>>


    // ==========================================
    // GASTOS
    // ==========================================
    @GET("api/gastos/{user_id}")
    suspend fun obtenerGastos(@Path("user_id") userId: Int): Response<List<Gasto>>

    @POST("api/gastos")
    suspend fun crearGasto(@Body nuevoGasto: Gasto): Response<Map<String, Any>>

    @PUT("api/gastos/{gasto_id}")
    suspend fun editarGasto(
        @Path("gasto_id") gastoId: Int,
        @Body gastoEditado: Gasto
    ): Response<Map<String, String>>

    @DELETE("api/gastos/{gasto_id}")
    suspend fun eliminarGasto(@Path("gasto_id") gastoId: Int): Response<Map<String, String>>


    // ==========================================
    // INGRESOS
    // ==========================================
    @GET("api/ingresos/{user_id}")
    suspend fun obtenerIngresos(@Path("user_id") userId: Int): Response<List<Ingreso>>

    @POST("api/ingresos")
    suspend fun crearIngreso(@Body nuevoIngreso: Ingreso): Response<Map<String, Any>>

    @PUT("api/ingresos/{ingreso_id}")
    suspend fun editarIngreso(
        @Path("ingreso_id") ingresoId: Int,
        @Body ingresoEditado: Ingreso
    ): Response<Map<String, String>>

    @DELETE("api/ingresos/{ingreso_id}")
    suspend fun eliminarIngreso(@Path("ingreso_id") ingresoId: Int): Response<Map<String, String>>
}
package com.example.freezexpends.models

data class Gasto(
    val gasto_id: Int = 0,
    val user_id: Int,
    val categoria_id: Int,
    val fecha_gasto: String,
    val nombre_gasto: String,
    val descripcion: String? = null,
    val monto_gasto: Double,
    val plazo: String? = null,
    val completado: Int = 0,
    val nombre_categoria: String? = null
)
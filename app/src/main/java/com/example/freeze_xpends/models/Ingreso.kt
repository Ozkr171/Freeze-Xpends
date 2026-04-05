package com.example.freezexpends.models

data class Ingreso(
    val ingreso_id: Int = 0,
    val user_id: Int,
    val categoria_id: Int,
    val fecha_ingreso: String,
    val nombre_ingreso: String,
    val descripcion: String? = null,
    val monto: Double,
    val recibido: Int = 0,
    val nombre_categoria: String? = null // Para recibir el texto limpio del JOIN de MySQL
)
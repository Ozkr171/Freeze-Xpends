package com.example.freezexpends.models // Revisa que esta ruta sea la tuya

data class Usuario(
    val user_id: Int = 0,
    val nombre_s: String,
    val correo_electronico: String,
    val contrasena: String? = null,
    val premium: Int = 0, // 0 = Gratuito, 1 = Premium
    val divisa: String = "MXN"
)
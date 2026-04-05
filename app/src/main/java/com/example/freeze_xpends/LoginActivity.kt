package com.example.freeze_xpends

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.example.freeze_xpends.api.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Enlazamos con los IDs del nuevo diseño XML
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)

        // 2. Lógica del botón Iniciar Sesión
        btnIngresar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, completa tus credenciales", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llamada a la API
            lifecycleScope.launch {
                try {
                    val credenciales = mapOf(
                        "correo_electronico" to correo,
                        "contrasena" to contrasena
                    )

                    val response = RetrofitClient.apiService.iniciarSesion(credenciales)

                    if (response.isSuccessful) {
                        val body = response.body()
                        val userIdDouble = body?.get("user_id") as? Double
                        val userId = userIdDouble?.toInt() ?: -1

                        if (userId != -1) {
                            // Guardamos la sesión en el teléfono
                            val sharedPreferences = getSharedPreferences("SesionApp", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putInt("USER_ID", userId)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, "¡Acceso concedido!", Toast.LENGTH_SHORT).show()

                            // Salto a la pantalla principal
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Datos incorrectos. Verifica tu email o contraseña.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error de conexión. ¿Está encendido el servidor?", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

        // 3. Lógica del botón Crear Cuenta (Para cuando hagas esa pantalla)
        btnCrearCuenta.setOnClickListener {
            // Aquí mandarías a la RegisterActivity
            // val intent = Intent(this, RegisterActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Pantalla de registro próximamente", Toast.LENGTH_SHORT).show()
        }
    }
}
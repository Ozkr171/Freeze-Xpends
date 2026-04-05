package com.example.freezexpends

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.freeze_xpends.R
import com.example.freeze_xpends.api.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Enlazamos con el XML
        val etNombre = findViewById<TextInputEditText>(R.id.etNombreRegistro)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoRegistro)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasenaRegistro)
        val btnRegistrar = findViewById<Button>(R.id.btnCompletarRegistro)
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

        // 2. ¿Qué pasa al darle "Registrarme"?
        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Validar que no dejen espacios en blanco
            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviar a la API
            lifecycleScope.launch {
                try {
                    // Armamos el paquetito de datos tal como lo pide tu Node.js
                    val datosRegistro = mapOf(
                        "nombre_s" to nombre,
                        "correo_electronico" to correo,
                        "contrasena" to contrasena
                    )

                    val response = RetrofitClient.apiService.registrarUsuario(datosRegistro)

                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()

                        // Como ya se registró, lo mandamos de vuelta al Login para que entre
                        finish() // Esto cierra la pantalla de registro y te deja en el Login
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error: El correo podría ya estar en uso", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

        // 3. ¿Qué pasa si toca "Ya tengo cuenta"?
        tvVolverLogin.setOnClickListener {
            finish() // Simplemente cierra el registro y vuelve al Login
        }
    }
}
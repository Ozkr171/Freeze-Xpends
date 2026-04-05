package com.example.freeze_xpends

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freeze_xpends.api.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton

import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    // Variables globales para los elementos de la interfaz
    private lateinit var tvBalanceTotal: TextView
    private lateinit var tvTotalIngresos: TextView
    private lateinit var tvTotalGastos: TextView
    private lateinit var rvTransacciones: RecyclerView
    private lateinit var llEstadoVacio: LinearLayout
    private lateinit var fabNuevo: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ==========================================
        // 1. VERIFICAR SESIÓN (LA LIBRETITA)
        // ==========================================
        val sharedPreferences = getSharedPreferences("SesionApp", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        // Si nadie ha iniciado sesión, lo pateamos de vuelta al Login
        if (userId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return // Detenemos la ejecución aquí
        }

        // ==========================================
        // 2. ENLAZAR LA VISTA (XML)
        // ==========================================
        tvBalanceTotal = findViewById(R.id.tvBalanceTotal)
        tvTotalIngresos = findViewById(R.id.tvTotalIngresos)
        tvTotalGastos = findViewById(R.id.tvTotalGastos)
        rvTransacciones = findViewById(R.id.rvTransacciones)
        llEstadoVacio = findViewById(R.id.llEstadoVacio)
        fabNuevo = findViewById(R.id.fabNuevo)

        // Inicializar RecyclerView (necesitarás un Adapter real luego)
        rvTransacciones.layoutManager = LinearLayoutManager(this)

        // ==========================================
        // 3. DESCARGAR DATOS REALES DESDE LA API
        // ==========================================
        // Usamos Corrutinas para no congelar la app
        lifecycleScope.launch {
            try {
                // Hacemos las llamadas a Node.js en paralelo
                val responseGastos = RetrofitClient.apiService.obtenerGastos(userId)
                val responseIngresos = RetrofitClient.apiService.obtenerIngresos(userId)

                if (responseGastos.isSuccessful && responseIngresos.isSuccessful) {
                    // Extraemos las listas que nos mandó la base de datos
                    val listaGastos = responseGastos.body() ?: emptyList()
                    val listaIngresos = responseIngresos.body() ?: emptyList()

                    // ==========================================
                    // 4. LÓGICA DE ESTADO VACÍO (TU PREGUNTA)
                    // ==========================================
                    // Si ambas listas están vacías, aplicamos tu lógica
                    if (listaGastos.isEmpty() && listaIngresos.isEmpty()) {
                        // Balance y Totales a $0 (como dice tu lógica)
                        actualizarTotales(0.0, 0.0, 0.0)

                        // Mostramos el mensaje de "Estado Vacío" y ocultamos la lista
                        llEstadoVacio.visibility = View.VISIBLE
                        rvTransacciones.visibility = View.GONE
                    } else {
                        // ¡Tenemos datos! Ocultamos el mensaje vacío y mostramos la lista
                        llEstadoVacio.visibility = View.GONE
                        rvTransacciones.visibility = View.VISIBLE

                        // Las matemáticas reales
                        val totalGastos = listaGastos.sumOf { it.monto_gasto }
                        val totalIngresos = listaIngresos.sumOf { it.monto }
                        val balanceNeto = totalIngresos - totalGastos

                        // Actualizar los números en la pantalla
                        actualizarTotales(balanceNeto, totalIngresos, totalGastos)

                        // TODO: Aquí conectarías el Adapter real al RecyclerView con tus listas pobladas
                        // rvTransacciones.adapter = TransaccionesAdapter(listaGastos, listaIngresos)
                    }

                } else {
                    Toast.makeText(this@MainActivity, "Error al sincronizar datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Si el servidor de Node está apagado o no hay internet
                Toast.makeText(this@MainActivity, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        // ==========================================
        // 5. LÓGICA DEL BOTÓN '+ NUEVO'
        // ==========================================
        fabNuevo.setOnClickListener {
            // Aquí abrirías la pantalla para agregar un nuevo Gasto o Ingreso
            // val intent = Intent(this, NuevaTransaccionActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Abrir formulario para nueva transacción", Toast.LENGTH_SHORT).show()
        }
    }

    // Función auxiliar para formatear los números en dólares con 2 decimales
    private fun actualizarTotales(balance: Double, ingresos: Double, gastos: Double) {
        val locale = Locale("es", "MX") // O la que prefieras para la divisa
        val format = java.text.NumberFormat.getCurrencyInstance(locale)

        tvBalanceTotal.text = format.format(balance)
        tvTotalIngresos.text = format.format(ingresos)
        tvTotalGastos.text = format.format(gastos)
    }
}
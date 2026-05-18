package com.example.freeze_xpends.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
  
    private const val IS_DEV = false

    private const val DEV_URL = "http://192.168.0.59:3000/"

    private const val PROD_URL = "https://freezexpends-api.onrender.com/"

    private val BASE_URL = if (IS_DEV) DEV_URL else PROD_URL

    // --- 2. CONFIGURACIÓN DEL CLIENTE AVANZADO ---
    private val okHttpClient: OkHttpClient by lazy {
        // El interceptor nos imprimirá en el Logcat todo el tráfico de la API
        val logging = HttpLoggingInterceptor().apply {
            level = if (IS_DEV) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Tiempo máximo para conectar
            .readTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo para recibir datos
            .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo máximo para enviar datos
            .build()
    }

    // --- 3. INSTANCIA DE RETROFIT ---
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Inyectamos nuestro cliente configurado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
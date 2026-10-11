package pe.colegio.asistencia.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pe.colegio.asistencia.data.session.SessionManager
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⚠️ CAMBIAR POR LA IP DE TU PC
    private const val BASE_URL = "http://192.168.0.19/asistencia-api/"

    private lateinit var sessionManager: SessionManager

    /**
     * Inicializa el SessionManager.
     * Llamar desde MainActivity antes de usar RetrofitClient.api
     */
    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))   // ← NUEVO: agrega el Bearer
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
package pe.colegio.asistencia.data.api

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.Interceptor
import okhttp3.Response
import pe.colegio.asistencia.data.session.SessionManager

/**
 * Interceptor que agrega automáticamente el header
 * Authorization: Bearer <token> a las peticiones que lo necesiten.
 *
 * NO agrega el header en endpoints públicos (login).
 */
class AuthInterceptor(private val session: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Endpoints públicos que NO requieren token
        val esPublico = path.contains("/auth/login")

        return if (esPublico) {
            // No agregar Authorization
            chain.proceed(request)
        } else {
            // Leer el token desde DataStore
            // runBlocking porque el interceptor es síncrono
            val token = runBlocking {
                session.tokenFlow.first()
            }

            if (token.isNullOrEmpty()) {
                chain.proceed(request)
            } else {
                val nuevaPeticion = request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(nuevaPeticion)
            }
        }
    }
}

package com.example.miappmodular.network

import com.example.miappmodular.model.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp que añade automáticamente el token de autenticación JWT
 * a todas las peticiones HTTP que requieran autorización.
 *
 * Este interceptor implementa el patrón de "Token Injection", permitiendo
 * centralizar la lógica de autenticación en un solo lugar en vez de tener
 * que añadir manualmente el header en cada llamada a la API.
 *
 * **Funcionamiento:**
 * 1. Intercepta todas las peticiones HTTP antes de enviarlas al servidor
 * 2. Recupera el authToken del SessionManager (si existe)
 * 3. Añade el header `Authorization: Bearer {token}` si el token existe
 * 4. Permite que la petición continúe normalmente
 *
 * **Endpoints que requieren autenticación:**
 * - `GET /auth/me` - Obtener datos del usuario autenticado
 * - Cualquier endpoint futuro que requiera el token JWT
 *
 * **Nota sobre runBlocking:**
 * Usamos `runBlocking` porque los interceptores de OkHttp no son funciones
 * suspendidas, pero necesitamos llamar a `getAuthToken()` que sí es suspend.
 * Esta es una práctica común y aceptada para interceptores que necesitan
 * acceder a datos asíncronos. El impacto en rendimiento es mínimo porque
 * DataStore usa caché en memoria.
 *
 * Ejemplo de uso (en RetrofitClient):
 * ```kotlin
 * val sessionManager = SessionManager(context)
 * val authInterceptor = AuthInterceptor(sessionManager)
 *
 * val okHttpClient = OkHttpClient.Builder()
 *     .addInterceptor(authInterceptor)
 *     .addInterceptor(loggingInterceptor)
 *     .build()
 * ```
 *
 * **Orden de interceptores:**
 * Es importante añadir AuthInterceptor ANTES de LoggingInterceptor
 * para que los logs muestren el header Authorization correctamente.
 *
 * **Alternativa sin runBlocking:**
 * Para evitar `runBlocking`, se podría:
 * 1. Mantener el token en memoria con Flow y collectAsState
 * 2. Usar un cache in-memory del token actualizado reactivamente
 * Esto añadiría complejidad sin beneficio significativo en este caso.
 *
 * @property sessionManager Gestor de sesión que proporciona el authToken.
 *
 * @see SessionManager
 * @see RetrofitClient
 * @see AuthApiService
 */
class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    /**
     * Intercepta y modifica la petición HTTP para añadir autenticación.
     *
     * Este método es llamado automáticamente por OkHttp antes de cada
     * petición HTTP. No debe ser invocado manualmente.
     *
     * **Flujo de ejecución:**
     * 1. Obtiene el token del SessionManager (operación suspend)
     * 2. Si existe token, crea una nueva petición con header Authorization
     * 3. Si no hay token, deja la petición sin modificar
     * 4. Procede con la petición (modificada o no) en la cadena de interceptores
     *
     * **Header generado:**
     * ```
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * ```
     *
     * @param chain Cadena de interceptores de OkHttp.
     * @return Response del servidor tras ejecutar la petición.
     *
     * @throws IOException Si hay error de red durante la petición.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Recuperar el token de forma síncrona usando runBlocking
        val token = runBlocking {
            sessionManager.getAuthToken()
        }

        // Si no hay token, continuar con la petición original sin modificar
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Crear nueva petición con el header Authorization
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        // Continuar con la petición autenticada
        return chain.proceed(authenticatedRequest)
    }
}
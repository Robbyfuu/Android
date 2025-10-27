package com.example.miappmodular.network

import android.content.Context
import com.example.miappmodular.model.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Objeto singleton que configura y proporciona el cliente Retrofit para la API de Xano.
 *
 * Este object implementa el patrón Singleton usando `object` de Kotlin para garantizar
 * una única instancia compartida de Retrofit y OkHttpClient en toda la aplicación.
 * Todas las propiedades se inicializan de forma lazy (solo cuando se acceden).
 *
 * **Configuración:**
 * - **Base URL:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/`
 * - **Convertidor JSON:** Gson para serialización/deserialización automática
 * - **Timeouts:** 30 segundos para conexión, lectura y escritura
 * - **Logging:** Registra todo el cuerpo de requests/responses en desarrollo
 *
 * **Patrón Lazy:**
 * Las propiedades `okHttpClient`, `retrofit` y `authApiService` usan `by lazy`,
 * lo que significa que se crean solo la primera vez que se acceden y se reutilizan
 * en accesos subsecuentes. Esto optimiza el uso de memoria y rendimiento.
 *
 * Ejemplo de uso en Repository:
 * ```kotlin
 * class UserRepository {
 *     private val apiService = RetrofitClient.authApiService
 *
 *     suspend fun registerUser(request: SignUpRequest): Result<AuthResponse> {
 *         return try {
 *             val response = apiService.signUp(request)
 *             if (response.isSuccessful) {
 *                 Result.success(response.body()!!)
 *             } else {
 *                 Result.failure(Exception("HTTP ${response.code()}"))
 *             }
 *         } catch (e: Exception) {
 *             Result.failure(e)
 *         }
 *     }
 * }
 * ```
 *
 * Ejemplo con Dependency Injection (Hilt):
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     @Provides
 *     @Singleton
 *     fun provideAuthApiService(): AuthApiService {
 *         return RetrofitClient.authApiService
 *     }
 * }
 * ```
 *
 * ⚠️ **Nota de producción:**
 * El logging interceptor registra cuerpos completos de requests/responses (nivel BODY).
 * En producción, cambiar a `Level.NONE` o `Level.BASIC` para evitar logs sensibles.
 *
 * @see AuthApiService
 * @see com.example.miappmodular.repository.UserRepository
 */
object RetrofitClient {

    /**
     * URL base de la API de Xano.
     *
     * Todas las rutas definidas en [AuthApiService] se concatenan con esta base.
     * Ejemplo: `@POST("auth/login")` se convierte en
     * `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/auth/login`
     */
    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/"

    /**
     * SessionManager para manejar tokens de autenticación.
     *
     * Debe ser inicializado llamando [initialize] antes de usar el cliente.
     */
    private lateinit var sessionManager: SessionManager

    /**
     * Inicializa el RetrofitClient con el contexto de la aplicación.
     *
     * Este método debe ser llamado una vez al inicio de la aplicación,
     * preferiblemente en Application.onCreate() o antes de usar cualquier
     * servicio de la API que requiera autenticación.
     *
     * @param context Contexto de la aplicación (preferiblemente ApplicationContext).
     *
     * Ejemplo de uso:
     * ```kotlin
     * class MyApplication : Application() {
     *     override fun onCreate() {
     *         super.onCreate()
     *         RetrofitClient.initialize(this)
     *     }
     * }
     * ```
     */
    fun initialize(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    /**
     * Cliente HTTP OkHttp configurado con interceptores y timeouts.
     *
     * **Configuración:**
     * - **AuthInterceptor:** Añade automáticamente el header Authorization con el JWT token.
     *   Se ejecuta ANTES que LoggingInterceptor para que los logs muestren el header correctamente.
     *
     * - **HttpLoggingInterceptor:** Registra todas las peticiones y respuestas HTTP
     *   con nivel BODY (incluye headers, body JSON, códigos de respuesta).
     *   Útil para debugging pero debe desactivarse en producción por seguridad.
     *
     * - **Timeouts (30 segundos c/u):**
     *   - `connectTimeout`: Máximo tiempo para establecer conexión TCP
     *   - `readTimeout`: Máximo tiempo esperando respuesta del servidor
     *   - `writeTimeout`: Máximo tiempo para enviar datos al servidor
     *
     * **Orden de interceptores:**
     * 1. AuthInterceptor - Añade autenticación
     * 2. LoggingInterceptor - Registra la petición completa (con headers de auth)
     *
     * **Lazy initialization:**
     * Se crea solo cuando se accede por primera vez. Thread-safe por defecto en Kotlin.
     *
     * @see okhttp3.OkHttpClient
     * @see okhttp3.logging.HttpLoggingInterceptor
     * @see AuthInterceptor
     */
    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthInterceptor(sessionManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instancia singleton de Retrofit.
     *
     * Retrofit es la biblioteca que convierte interfaces Kotlin (como [AuthApiService])
     * en clientes HTTP funcionales, manejando automáticamente:
     * - Serialización JSON → Objetos Kotlin (con Gson)
     * - Objetos Kotlin → JSON en request bodies
     * - Manejo de URLs, headers y parámetros
     * - Integración con corrutinas (suspend functions)
     *
     * **Configuración:**
     * - **BaseUrl:** [BASE_URL]
     * - **Client:** [okHttpClient] con logging y timeouts
     * - **Converter:** [GsonConverterFactory] para JSON ↔ Objetos
     *
     * **Lazy initialization:**
     * Se construye solo la primera vez que se necesita.
     *
     * @see retrofit2.Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Servicio de autenticación generado automáticamente por Retrofit.
     *
     * Retrofit toma la interfaz [AuthApiService] y genera dinámicamente
     * una implementación completa en tiempo de ejecución usando Reflection
     * y Dynamic Proxy.
     *
     * Este servicio contiene todos los endpoints de autenticación:
     * - `signUp()`: Registro de nuevos usuarios
     * - `login()`: Autenticación de usuarios existentes
     * - `userActually()`: Obtener datos del usuario autenticado
     *
     * **Uso:**
     * ```kotlin
     * val apiService = RetrofitClient.authApiService
     * val response = apiService.login(LoginRequest(email, password))
     * ```
     *
     * **Lazy initialization:**
     * La interfaz se implementa solo cuando se accede por primera vez.
     *
     * @see AuthApiService
     */
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}

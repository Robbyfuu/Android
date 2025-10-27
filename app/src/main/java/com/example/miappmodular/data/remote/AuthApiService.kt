package com.example.miappmodular.data.remote

import com.example.miappmodular.data.remote.dto.AuthResponse
import com.example.miappmodular.data.remote.dto.LoginRequest
import com.example.miappmodular.data.remote.dto.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

/**
 * Interfaz de servicio Retrofit para endpoints de autenticación de Xano.
 *
 * Esta interfaz define el contrato de la API REST para autenticación de usuarios.
 * Retrofit genera automáticamente la implementación de todos los métodos en tiempo
 * de ejecución basándose en las anotaciones HTTP.
 *
 * **Arquitectura:**
 * - Esta interfaz NO debe ser llamada directamente desde ViewModels o UI
 * - Todos los endpoints deben invocarse a través del [UserRepository]
 * - El Repository maneja lógica de negocio, caché y transformación de datos
 *
 * **Base URL:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/`
 *
 * **Métodos suspend:**
 * Todos los endpoints son funciones `suspend` para integrarse con corrutinas de Kotlin.
 * Deben ejecutarse desde un scope de corrutina como `viewModelScope` o `lifecycleScope`.
 *
 * Ejemplo de uso en Repository:
 * ```kotlin
 * class UserRepository(private val apiService: AuthApiService) {
 *     suspend fun registerUser(name: String, email: String, password: String): Result<User> {
 *         return try {
 *             val request = SignUpRequest(name, email, password)
 *             val response = apiService.signUp(request)
 *
 *             if (response.isSuccessful && response.body() != null) {
 *                 // Procesar respuesta exitosa
 *                 Result.success(mapToUser(response.body()!!))
 *             } else {
 *                 Result.failure(Exception("Error ${response.code()}"))
 *             }
 *         } catch (e: Exception) {
 *             Result.failure(e)
 *         }
 *     }
 * }
 * ```
 *
 * @see com.example.miappmodular.repository.UserRepository
 * @see com.example.miappmodular.network.RetrofitClient
 * @see retrofit2.Response
 */
interface AuthApiService {

    /**
     * Registra un nuevo usuario en el sistema Xano.
     *
     * **Endpoint:** `POST /auth/signup`
     *
     * **URL completa:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/auth/signup`
     *
     * Crea una nueva cuenta de usuario y retorna un token de autenticación JWT
     * junto con los datos básicos del usuario creado.
     *
     * **Códigos de respuesta esperados:**
     * - `200 OK`: Usuario creado exitosamente
     * - `400 Bad Request`: Datos inválidos (email ya registrado, contraseña débil, etc.)
     * - `500 Internal Server Error`: Error del servidor de Xano
     *
     * @param request Objeto [SignUpRequest] con name, email y password.
     * @return [Response]<[AuthResponse]> con authToken, id, name, email y createdAt.
     *         Usar `response.isSuccessful` y `response.body()` para procesar.
     *
     * @see SignUpRequest
     * @see AuthResponse
     * @see com.example.miappmodular.repository.UserRepository.registerUser
     */
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    /**
     * Autentica un usuario existente con email y contraseña.
     *
     * **Endpoint:** `POST /auth/login`
     *
     * **URL completa:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/auth/login`
     *
     * Valida las credenciales del usuario y retorna un token de autenticación JWT
     * que debe usarse en peticiones subsecuentes que requieran autorización.
     *
     * **Códigos de respuesta esperados:**
     * - `200 OK`: Autenticación exitosa
     * - `401 Unauthorized`: Credenciales inválidas (email o contraseña incorrectos)
     * - `404 Not Found`: Usuario no encontrado
     * - `500 Internal Server Error`: Error del servidor de Xano
     *
     * @param request Objeto [LoginRequest] con email y password.
     * @return [Response]<[AuthResponse]> con authToken y datos del usuario autenticado.
     *
     * @see LoginRequest
     * @see AuthResponse
     * @see com.example.miappmodular.repository.UserRepository.loginUser
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * Obtiene la información del usuario autenticado actualmente.
     *
     * **Endpoint:** `GET /auth/me`
     *
     * **URL completa:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/auth/me`
     *
     * Este endpoint requiere un token de autenticación válido en los headers.
     * Útil para:
     * - Validar que el token JWT aún es válido
     * - Refrescar los datos del usuario desde el servidor
     * - Sincronizar cambios realizados en otros dispositivos
     *
     * **Códigos de respuesta esperados:**
     * - `200 OK`: Datos del usuario recuperados exitosamente
     * - `401 Unauthorized`: Token inválido o expirado
     * - `404 Not Found`: Usuario no encontrado (posiblemente eliminado)
     *
     * @return [Response]<[AuthResponse]> con los datos actualizados del usuario.
     *
     * @see AuthResponse
     */
    @GET("auth/me")
    suspend fun userActually (): Response <AuthResponse>
}

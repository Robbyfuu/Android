package com.example.miappmodular.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object que representa la respuesta del servidor de autenticación.
 *
 * Esta clase encapsula la información devuelta por la API de Xano tras un
 * login o registro exitoso. Contiene el token de autenticación JWT y los
 * datos básicos del usuario autenticado.
 *
 * Todas las propiedades son nullable para manejar respuestas parciales o
 * errores del servidor de forma segura.
 *
 * Ejemplo de respuesta JSON:
 * ```json
 * {
 *   "authToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "id": 123,
 *   "name": "Juan Pérez",
 *   "email": "juan.perez@example.com",
 *   "created_at": 1704067200000
 * }
 * ```
 *
 * Ejemplo de uso:
 * ```kotlin
 * val response = authApiService.login(loginRequest)
 * if (response.isSuccessful) {
 *     val authResponse = response.body()
 *     authResponse?.authToken?.let { token ->
 *         // Guardar token y proceder con la sesión
 *         sessionManager.saveUserSession(
 *             userId = authResponse.id.toString(),
 *             email = authResponse.email ?: "",
 *             name = authResponse.name ?: "",
 *             rememberMe = true
 *         )
 *     }
 * }
 * ```
 *
 * @property authToken Token JWT de autenticación. Se usa para autorizar peticiones
 *                     subsecuentes a la API. Nullable si falla la autenticación.
 * @property id Identificador único del usuario en la base de datos de Xano.
 *              Nullable si la respuesta está incompleta.
 * @property name Nombre completo del usuario registrado en el sistema.
 *                Nullable si no se proporciona.
 * @property email Dirección de correo electrónico del usuario autenticado.
 *                 Nullable si no se proporciona.
 * @property createdAt Timestamp Unix (milisegundos) de la fecha de creación de la cuenta.
 *                     Se puede convertir a Date con `Date(createdAt)`.
 *                     Nullable si no se proporciona.
 * @property roleUser Rol del usuario en el sistema (ej: "admin", "user", "moderator").
 *                    Define permisos y nivel de acceso del usuario. Nullable si no
 *                    se especifica o si el backend no implementa roles.
 *
 * @see com.example.miappmodular.model.dto.LoginRequest
 * @see com.example.miappmodular.model.dto.SignUpRequest
 * @see com.example.miappmodular.network.AuthApiService
 * @see com.example.miappmodular.model.SessionManager
 */
data class AuthResponse(
    @SerializedName("authToken")
    val authToken: String?,

    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("created_at")
    val createdAt: Long?,

    @SerializedName("role")
    val roleUser : String?
)
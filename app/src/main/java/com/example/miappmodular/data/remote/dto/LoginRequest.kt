package com.example.miappmodular.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object para la autenticación de usuarios existentes.
 *
 * Esta clase representa el cuerpo de la petición HTTP enviada al endpoint
 * de login de la API de Xano. Contiene las credenciales necesarias para
 * autenticar un usuario y obtener un token de sesión.
 *
 * Ejemplo de uso:
 * ```kotlin
 * val request = LoginRequest(
 *     email = "usuario@example.com",
 *     password = "MiPassword123!"
 * )
 * val response = authApiService.login(request)
 * ```
 *
 * Estructura JSON enviada:
 * ```json
 * {
 *   "email": "usuario@example.com",
 *   "password": "MiPassword123!"
 * }
 * ```
 *
 * @property email Dirección de correo electrónico del usuario registrado.
 *                 Debe ser un email válido y existente en el sistema.
 * @property password Contraseña del usuario. Se envía en texto plano por HTTPS,
 *                    el servidor se encarga de verificarla contra el hash almacenado.
 *
 * @see com.example.miappmodular.model.dto.AuthResponse
 * @see com.example.miappmodular.network.AuthApiService.login
 * @see com.example.miappmodular.viewmodel.LoginViewModel
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

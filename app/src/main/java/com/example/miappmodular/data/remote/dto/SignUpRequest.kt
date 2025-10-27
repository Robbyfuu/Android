package com.example.miappmodular.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object para el registro de nuevos usuarios.
 *
 * Esta clase representa el cuerpo de la petición HTTP enviada al endpoint
 * de registro (signup) de la API. Contiene la información necesaria para
 * crear una nueva cuenta de usuario en el sistema.
 *
 * Ejemplo de uso:
 * ```kotlin
 * val request = SignUpRequest(
 *     name = "Juan Pérez",
 *     email = "juan.perez@example.com",
 *     password = "SecurePass123!"
 * )
 * ```
 *
 * Estructura JSON enviada:
 * ```json
 * {
 *   "name": "Juan Pérez",
 *   "email": "juan.perez@example.com",
 *   "password": "SecurePass123!"
 * }
 * ```
 *
 * @property name Nombre completo del usuario. Debe contener al menos 3 caracteres.
 * @property email Dirección de correo electrónico del usuario. Debe ser un email válido
 *                 y único en el sistema.
 * @property password Contraseña del usuario. Debe cumplir con los requisitos de seguridad:
 *                    mínimo 6 caracteres, al menos una letra mayúscula, una minúscula y un número.
 *
 * @see com.example.miappmodular.model.dto.SignUpResponse
 * @see com.example.miappmodular.network.ApiService.signUp
 */
data class SignUpRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

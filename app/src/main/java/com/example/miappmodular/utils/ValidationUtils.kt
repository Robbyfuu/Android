package com.example.miappmodular.utils

object ValidationUtils {

    /**
     * Valida formato de email
     */
     fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Formato de email inválido"
            else -> null
        }
    }

    /**
     * Valida formato de contraseña
     */
     fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 8 -> "Mínimo 8 caracteres"
            !password.any { it.isUpperCase() } -> "Debe contener mayúsculas"
            !password.any { it.isLowerCase() } -> "Debe contener minúsculas"
            !password.any { it.isDigit() } -> "Debe contener números"
            else -> null
        }
    }

    /**
     * Valida formato de nombre
     */
    fun isValidName(name: String): String? {
        return when {
            name.isEmpty() -> "El nombre es requerido"
            name.length < 3 -> "Mínimo 3 caracteres"
            else -> null
        }
    }
}
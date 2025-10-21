package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.repository.UserRepository
import com.example.miappmodular.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Estados posibles del login
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val generalError: String? = null
)

/**
 * ViewModel con gestión de estado reactivo
 */
class LoginViewModel  (application: Application): AndroidViewModel(application) {

    // Estado interno mutable
    private val userRepository = UserRepository(application)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el email y valida en tiempo real
     */
    fun onEmailChange(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = newEmail,
                emailError = if (newEmail.isNotEmpty()) ValidationUtils.validateEmail(newEmail) else null
            )
        }
    }

    /**
     * Actualiza la contraseña y valida en tiempo real
     */
    fun onPasswordChange(newPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = newPassword,
                passwordError = if (newPassword.isNotEmpty()) ValidationUtils.validatePassword(newPassword) else null
            )
        }
    }

    /**
     * Realiza el proceso de login
     */
    fun login(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.loginUser(email, password, rememberMe)
                .onSuccess { user ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        isSuccess = true
                    ) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        generalError = exception.message
                    ) }
                }
        }
    }

    /**
     * Limpia los errores
     */
    fun clearErrors() {
        _uiState.update { it.copy(
            emailError = null,
            passwordError = null,
            generalError = null
        ) }
    }

}
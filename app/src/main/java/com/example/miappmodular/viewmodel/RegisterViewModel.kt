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


data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
            val generalError: String? = null
)

/**
 * ViewModel para el registro de usuarios con estados reactivos
 */
class RegisterViewModel (application: Application): AndroidViewModel(application) {

    // Aquí irían las propiedades y métodos para manejar el estado del registro
    private  val userRepository = UserRepository(application)
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el nombre y valida en tiempo real
     */
    fun onNameChange(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = newName,
                nameError = if (newName.isNotEmpty()) ValidationUtils.isValidName(newName) else null
            )
        }
    }

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
     * Actualiza la confirmación de contraseña y valida en tiempo real
     */
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = newConfirmPassword,
                confirmPasswordError = if (newConfirmPassword.isNotEmpty() && newConfirmPassword != currentState.password) {
                    "Las contraseñas no coinciden"
                } else null
            )
        }
    }

    /**
     * Realiza el registro del usuario
     */
    fun register(){
        val currentState = _uiState.value
        register(currentState.name, currentState.email, currentState.password)
    }

    private fun register (name: String, email:String, password: String){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.registerUser(name, email, password)
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
     * Limpia el estado de errores generales
     */
    fun clearErrors() {
        _uiState.update { it.copy(
            nameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        ) }
    }


}
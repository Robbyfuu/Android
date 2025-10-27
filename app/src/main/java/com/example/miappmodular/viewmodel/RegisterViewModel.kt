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
 * Estado de la pantalla de registro de usuario.
 *
 * Incluye 4 campos de entrada y sus respectivos errores de validación,
 * además de estados de loading y success para la UI reactiva.
 *
 * @property name Nombre completo del usuario.
 * @property email Correo electrónico.
 * @property password Contraseña.
 * @property confirmPassword Confirmación de contraseña (debe coincidir con password).
 * @property nameError Error de validación del nombre.
 * @property emailError Error de validación del email.
 * @property passwordError Error de validación de la contraseña.
 * @property confirmPasswordError Error si las contraseñas no coinciden.
 * @property isLoading Indica si el registro está en progreso.
 * @property isSuccess Indica si el registro fue exitoso.
 * @property generalError Error general del proceso (ej: "Email ya registrado").
 */
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
 * ViewModel para la pantalla de registro con validaciones complejas.
 *
 * Coordina el registro de nuevos usuarios validando:
 * - Nombre (mínimo 3 caracteres)
 * - Email (formato válido RFC 5322)
 * - Contraseña (mínimo 8 chars, mayús, minús, número)
 * - Confirmación (debe coincidir con contraseña)
 *
 * **Flujo de registro:**
 * 1. Usuario escribe en los 4 campos con validación en tiempo real
 * 2. Usuario presiona "Registrarse" → `register()`
 * 3. Validación final de todos los campos
 * 4. Si válido: Llama a `userRepository.registerUser()`
 * 5. Si exitoso: `isSuccess = true` → UI navega a Home
 * 6. Si falla: `generalError` con mensaje del servidor
 *
 * @see RegisterUiState
 * @see UserRepository.registerUser
 */
class RegisterViewModel(application: Application): AndroidViewModel(application) {

    private  val userRepository = UserRepository(application)
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el nombre y valida en tiempo real.
     *
     * Validación:
     * - Mínimo 3 caracteres
     * - No vacío
     */
    fun onNameChange(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = newName,
                nameError = if (newName.isNotEmpty()) ValidationUtils.isValidName(newName) else null
            )
        }
    }

    /**
     * Actualiza el email y valida en tiempo real.
     *
     * Validación:
     * - Formato válido según RFC 5322
     * - `allowEmpty = true` para no mostrar error mientras se escribe
     */
    fun onEmailChange(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = newEmail,
                emailError = ValidationUtils.validateEmail(newEmail, allowEmpty = true)
            )
        }
    }

    /**
     * Actualiza la contraseña y valida en tiempo real.
     *
     * Validación (solo si no está vacío):
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
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
     * Actualiza la confirmación de contraseña y valida coincidencia.
     *
     * Muestra error si:
     * - No está vacía Y no coincide con `password`
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
     * Inicia el proceso de registro del usuario.
     *
     * Realiza validación final de TODOS los campos antes de enviar al Repository.
     * Si hay algún error, actualiza el estado y retorna sin llamar a la API.
     */
    fun register(){
        val currentState = _uiState.value

        // Validar todos los campos
        val nameError = ValidationUtils.isValidName(currentState.name)
        val emailError = ValidationUtils.validateEmail(currentState.email)
        val passwordError = ValidationUtils.validatePassword(currentState.password)

        // Validar que las contraseñas coincidan y que la confirmación no esté vacía
        val confirmPasswordError = when {
            currentState.confirmPassword.isBlank() -> "Confirma tu contraseña"
            currentState.password != currentState.confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }

        // Si hay algún error, actualizar el estado y no continuar
        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.update { it.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            ) }
            return
        }

        register(currentState.name, currentState.email, currentState.password)
    }

    /**
     * Método privado que ejecuta el registro en el Repository.
     *
     * Se separa del público para mantener la validación centralizada
     * en el método público `register()`.
     */
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
     * Limpia todos los mensajes de error del estado.
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

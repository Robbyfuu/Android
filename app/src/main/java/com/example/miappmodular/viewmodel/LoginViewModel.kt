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
 * Data class que representa el estado completo de la pantalla de login.
 *
 * Este modelo encapsula todos los datos necesarios para renderizar la UI
 * de login de forma reactiva. Sigue el patrón de UI State inmutable,
 * donde cada cambio genera una nueva instancia del estado.
 *
 * **Arquitectura:**
 * ```
 * LoginScreen → LoginViewModel → LoginUiState
 *       ↓             ↓                ↓
 *   Observe      Update          Immutable State
 * ```
 *
 * @property email Valor actual del campo de email. String vacío por defecto.
 * @property password Valor actual del campo de contraseña. String vacío por defecto.
 * @property emailError Mensaje de error de validación del email. Null si es válido.
 * @property passwordError Mensaje de error de validación de contraseña. Null si es válida.
 * @property isLoading Indica si hay una operación de login en progreso.
 *                     Útil para mostrar spinner y deshabilitar botones.
 * @property isSuccess Indica si el login fue exitoso. Se usa para navegar a Home.
 * @property generalError Error general del proceso de login (ej: "Credenciales incorrectas",
 *                        "Error de conexión"). Null si no hay errores.
 *
 * @see LoginViewModel
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
 * ViewModel para la pantalla de login con gestión de estado reactivo.
 *
 * Este ViewModel implementa el patrón MVVM, coordinando la lógica de negocio
 * de autenticación entre la UI (LoginScreen) y el Repository (UserRepository).
 * Maneja validaciones, estado de carga, y comunicación con la capa de datos.
 *
 * **Responsabilidades:**
 * - Validar credenciales en tiempo real mientras el usuario escribe
 * - Coordinar el proceso de login con [UserRepository]
 * - Exponer estado reactivo mediante [StateFlow] para la UI
 * - Manejar errores de red y autenticación
 * - Gestionar estado de carga (loading spinners)
 *
 * **Flujo de login:**
 * 1. Usuario escribe email/password → `onEmailChange()` / `onPasswordChange()`
 * 2. Validación en tiempo real con [ValidationUtils]
 * 3. Usuario presiona "Iniciar Sesión" → `login()`
 * 4. Validación final de todos los campos
 * 5. Si válido: Llama a `userRepository.loginUser()`
 * 6. Actualiza `uiState` según resultado (success/failure)
 * 7. UI observa cambios y navega o muestra error
 *
 * Ejemplo de uso en Composable:
 * ```kotlin
 * @Composable
 * fun LoginScreen(
 *     viewModel: LoginViewModel = viewModel(),
 *     onNavigateToHome: () -> Unit,
 *     onNavigateToRegister: () -> Unit
 * ) {
 *     val uiState by viewModel.uiState.collectAsState()
 *
 *     // Navegar al Home cuando login es exitoso
 *     LaunchedEffect(uiState.isSuccess) {
 *         if (uiState.isSuccess) {
 *             onNavigateToHome()
 *         }
 *     }
 *
 *     Column {
 *         ShadcnInput(
 *             value = uiState.email,
 *             onValueChange = viewModel::onEmailChange,
 *             error = uiState.emailError
 *         )
 *
 *         ShadcnButton(
 *             onClick = { viewModel.login(uiState.email, uiState.password, rememberMe = true) },
 *             loading = uiState.isLoading
 *         ) {
 *             Text("Iniciar Sesión")
 *         }
 *
 *         uiState.generalError?.let { error ->
 *             Text(error, color = Color.Red)
 *         }
 *     }
 * }
 * ```
 *
 * **Nota sobre AndroidViewModel:**
 * Usamos [AndroidViewModel] en lugar de [ViewModel] porque necesitamos
 * el [Application] context para inicializar [UserRepository] (que requiere
 * contexto para Room y DataStore). El context se pasa automáticamente por
 * el sistema de ViewModels.
 *
 * @param application Contexto de la aplicación, inyectado automáticamente.
 *
 * @see LoginUiState
 * @see UserRepository
 * @see ValidationUtils
 * @see com.example.miappmodular.ui.screens.LoginScreen
 */
class LoginViewModel(application: Application): AndroidViewModel(application) {

    /** Repository para operaciones de autenticación y gestión de usuarios */
    private val userRepository = UserRepository(application)

    /** Estado interno mutable (privado). Solo el ViewModel puede modificarlo. */
    private val _uiState = MutableStateFlow(LoginUiState())

    /**
     * Estado público inmutable expuesto a la UI.
     *
     * La UI observa este StateFlow y se re-renderiza automáticamente
     * cuando el estado cambia. Usar `collectAsState()` en Composables.
     */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el email y realiza validación en tiempo real.
     *
     * Se invoca cada vez que el usuario escribe en el campo de email.
     * La validación usa `allowEmpty = true` para no mostrar error mientras
     * el usuario escribe (experiencia de usuario más amigable).
     *
     * **Validación aplicada:**
     * - Formato de email válido según RFC 5322
     * - Solo muestra error si el email no está vacío y es inválido
     *
     * Ejemplo de uso:
     * ```kotlin
     * TextField(
     *     value = uiState.email,
     *     onValueChange = viewModel::onEmailChange
     * )
     * ```
     *
     * @param newEmail Nuevo valor del campo de email.
     *
     * @see ValidationUtils.validateEmail
     * @see onPasswordChange
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
     * Actualiza la contraseña y realiza validación en tiempo real.
     *
     * Se invoca cada vez que el usuario escribe en el campo de contraseña.
     * La validación solo se aplica si el campo no está vacío, para evitar
     * mostrar errores prematuramente mientras el usuario escribe.
     *
     * **Validación aplicada (solo si no está vacío):**
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     *
     * Ejemplo de uso:
     * ```kotlin
     * TextField(
     *     value = uiState.password,
     *     onValueChange = viewModel::onPasswordChange,
     *     visualTransformation = PasswordVisualTransformation()
     * )
     * ```
     *
     * @param newPassword Nuevo valor del campo de contraseña.
     *
     * @see ValidationUtils.validatePassword
     * @see onEmailChange
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
     * Ejecuta el proceso de autenticación de usuario.
     *
     * Este método realiza el flujo completo de login:
     * 1. Valida credenciales antes de enviar al servidor
     * 2. Si hay errores de validación, actualiza el estado y retorna
     * 3. Si es válido, inicia proceso asíncrono con Repository
     * 4. Actualiza estado según resultado (success → navegar, failure → mostrar error)
     *
     * **Casos de uso:**
     * - Éxito: `isSuccess = true` → UI navega a HomeScreen
     * - Credenciales inválidas: `generalError = "Credenciales incorrectas"`
     * - Sin conexión: `generalError = "Error de conexión: ..."`
     * - Email no existe: `generalError` del servidor
     *
     * **Validación previa:**
     * - Email no vacío y con formato válido
     * - Contraseña no vacía (no se valida complejidad en login, solo en registro)
     *
     * Ejemplo de uso:
     * ```kotlin
     * Button(onClick = {
     *     viewModel.login(
     *         email = uiState.email,
     *         password = uiState.password,
     *         rememberMe = rememberMeChecked
     *     )
     * }) {
     *     Text(if (uiState.isLoading) "Iniciando..." else "Iniciar Sesión")
     * }
     * ```
     *
     * @param email Email del usuario (debe ser válido).
     * @param password Contraseña del usuario (no debe estar vacía).
     * @param rememberMe Si true, la sesión persiste tras cerrar la app.
     *
     * @see UserRepository.loginUser
     * @see LoginUiState.isSuccess
     * @see LoginUiState.generalError
     */
    fun login(email: String, password: String, rememberMe: Boolean) {
        // Validar campos antes de proceder
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = if (password.isBlank()) "La contraseña es obligatoria" else null

        // Si hay errores, actualizar estado y no continuar
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(
                emailError = emailError,
                passwordError = passwordError
            ) }
            return
        }

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
     * Limpia todos los mensajes de error del estado.
     *
     * Útil para limpiar errores cuando:
     * - El usuario intenta nuevamente tras un error
     * - Se muestra un error en Snackbar y se desea cerrarlo
     * - El usuario navega de vuelta a la pantalla de login
     *
     * **Errores limpiados:**
     * - `emailError`
     * - `passwordError`
     * - `generalError`
     *
     * Ejemplo de uso:
     * ```kotlin
     * Snackbar(
     *     message = uiState.generalError ?: "",
     *     onDismiss = { viewModel.clearErrors() }
     * )
     * ```
     *
     * @see login
     */
    fun clearErrors() {
        _uiState.update { it.copy(
            emailError = null,
            passwordError = null,
            generalError = null
        ) }
    }
}

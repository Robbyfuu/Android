package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.AppDependencies
import com.example.miappmodular.model.entity.User
import com.example.miappmodular.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Estado de la pantalla de perfil de usuario.
 *
 * @property isLoading Indica si los datos del perfil se están cargando.
 * @property user Objeto User con todos los datos del perfil. Null si aún no se carga.
 * @property error Mensaje de error si falla la carga del perfil.
 * @property formattedCreatedAt Fecha de creación formateada (ej: "enero 2024").
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val formattedCreatedAt: String = ""
)

/**
 * ViewModel para la pantalla de perfil de usuario.
 *
 * Carga y muestra los datos del perfil del usuario autenticado actualmente.
 * Formatea fechas para mostrar en la UI de forma legible.
 *
 * **Inicialización:**
 * El perfil se carga automáticamente en el bloque `init {}`, por lo que
 * la UI solo necesita observar el `uiState` sin llamar manualmente a load.
 *
 * **Formato de fecha:**
 * Usa SimpleDateFormat con locale español para mostrar fechas como:
 * - "enero 2024"
 * - "diciembre 2023"
 *
 * Ejemplo de uso:
 * ```kotlin
 * @Composable
 * fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
 *     val uiState by viewModel.uiState.collectAsState()
 *
 *     when {
 *         uiState.isLoading -> LoadingSpinner()
 *         uiState.error != null -> ErrorMessage(uiState.error!!)
 *         uiState.user != null -> {
 *             ProfileCard(
 *                 name = uiState.user!!.name,
 *                 email = uiState.user!!.email,
 *                 memberSince = uiState.formattedCreatedAt
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * @see ProfileUiState
 * @see UserRepository.getCurrentUser
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Obtener el contenedor de dependencias (singleton compartido)
    private val dependencies = AppDependencies.getInstance(application)

    // Obtener el UserRepository del contenedor
    // Ventaja: Si el usuario se carga en LoginViewModel, la caché está disponible aquí
    private val userRepository = dependencies.userRepository
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Carga el perfil del usuario autenticado desde el Repository.
     *
     * Llamado automáticamente al inicializar el ViewModel.
     * Formatea la fecha de creación al formato "mes año" en español.
     *
     * Posibles resultados:
     * - Usuario encontrado: `user != null`, `formattedCreatedAt` con fecha
     * - Usuario no encontrado: `error = "No se encontró el usuario"`
     * - Error de carga: `error` con mensaje de excepción
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val user = userRepository.getCurrentUser()

                if (user != null) {
                    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                    val formattedDate = dateFormat.format(user.createdAt)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            formattedCreatedAt = formattedDate,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontró el usuario"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar el perfil"
                    )
                }
            }
        }
    }

    /**
     * Recarga el perfil del usuario.
     *
     * Útil para:
     * - Pull-to-refresh en la UI
     * - Refrescar datos tras actualizar el perfil
     * - Reintentar tras un error de carga
     */
    fun refresh() {
        loadUserProfile()
    }
}

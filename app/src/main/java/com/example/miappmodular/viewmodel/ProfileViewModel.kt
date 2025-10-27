package com.example.miappmodular.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.AppDependencies
import com.example.miappmodular.data.local.entity.User
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
 * @property avatarUri URI de la imagen de avatar del usuario (de cámara o galería).
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val formattedCreatedAt: String = "",
    val avatarUri: Uri? = null
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

    // Obtener el AvatarRepository del contenedor para persistencia del avatar
    private val avatarRepository = dependencies.avatarRepository

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadSavedAvatar()
    }

    /**
     * Carga el avatar guardado desde DataStore.
     *
     * Se ejecuta al inicializar el ViewModel para restaurar
     * el avatar que el usuario seleccionó previamente.
     *
     * **Flow collection:**
     * Usa `collect` en lugar de `collectAsState` porque estamos
     * en el ViewModel (no en Compose). El Flow se observa
     * continuamente y actualiza el estado cuando cambia.
     *
     * **Ciclo de vida:**
     * La coroutine se lanza en viewModelScope, por lo que se cancela
     * automáticamente cuando el ViewModel es limpiado.
     */
    private fun loadSavedAvatar() {
        viewModelScope.launch {
            avatarRepository.getAvatarUri().collect { savedUri ->
                _uiState.update { it.copy(avatarUri = savedUri) }
            }
        }
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

    /**
     * Actualiza la URI del avatar del usuario con persistencia en DataStore.
     *
     * Guarda la URI de la imagen seleccionada por el usuario desde
     * la cámara o galería. La URI se persiste en DataStore para
     * sobrevivir al cierre de la app.
     *
     * **Flujo de actualización:**
     * 1. El usuario selecciona una imagen (cámara o galería)
     * 2. Esta función guarda el URI en DataStore
     * 3. DataStore emite el nuevo valor a través del Flow
     * 4. El Flow se recolecta en loadSavedAvatar()
     * 5. El estado se actualiza automáticamente
     *
     * **Ventaja de este patrón:**
     * No necesitamos actualizar manualmente `_uiState` aquí porque
     * el Flow de `getAvatarUri()` ya está siendo observado y
     * actualizará el estado automáticamente cuando cambie en DataStore.
     * Esto evita duplicación y garantiza que el estado siempre refleja
     * lo que está guardado.
     *
     * **Persistencia:**
     * El avatar se mantiene incluso después de:
     * - Cerrar la app
     * - Reiniciar el dispositivo
     * - Navegar entre pantallas
     *
     * @param uri URI de la imagen seleccionada (puede ser null para eliminar avatar)
     */
    fun updateAvatar(uri: Uri?) {
        viewModelScope.launch {
            avatarRepository.saveAvatarUri(uri)
            // El estado se actualiza automáticamente vía Flow en loadSavedAvatar()
        }
    }
}

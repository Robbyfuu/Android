package com.example.miappmodular.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extensión para crear el DataStore de avatar.
 *
 * **Patrón Singleton:**
 * Usa una extensión de Context para garantizar una única instancia del DataStore.
 * El nombre "avatar_preferences" es único en la aplicación.
 */
private val Context.avatarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "avatar_preferences"
)

/**
 * Repositorio para gestionar la persistencia del URI del avatar del usuario.
 *
 * **Tecnología:**
 * Usa Jetpack DataStore (Preferences) para almacenamiento key-value asíncrono.
 * DataStore es la alternativa moderna y type-safe a SharedPreferences.
 *
 * **Ventajas de DataStore:**
 * - Operaciones completamente asíncronas (no bloquea el hilo principal)
 * - Type-safe con Kotlin coroutines y Flow
 * - Manejo automático de errores
 * - Consistencia de datos garantizada
 * - Mejor rendimiento que SharedPreferences
 *
 * **Almacenamiento:**
 * Guarda el URI como String usando una key preferences.
 * El Flow permite observar cambios reactivamente.
 *
 * **Arquitectura:**
 * Este repository sigue el patrón Repository de Android Architecture Components:
 * - Abstrae la fuente de datos (DataStore)
 * - Proporciona API limpia al ViewModel
 * - Maneja la lógica de persistencia
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * class ProfileViewModel(application: Application) : AndroidViewModel(application) {
 *     private val avatarRepository = AvatarRepository(application)
 *
 *     init {
 *         viewModelScope.launch {
 *             avatarRepository.getAvatarUri().collect { uri ->
 *                 _uiState.update { it.copy(avatarUri = uri) }
 *             }
 *         }
 *     }
 *
 *     fun updateAvatar(uri: Uri?) {
 *         viewModelScope.launch {
 *             avatarRepository.saveAvatarUri(uri)
 *         }
 *     }
 * }
 * ```
 *
 * @param context Contexto de la aplicación para acceder al DataStore
 *
 * @see androidx.datastore.preferences.core.Preferences
 * @see kotlinx.coroutines.flow.Flow
 */
class AvatarRepository(private val context: Context) {

    companion object {
        /**
         * Key para almacenar el URI del avatar en DataStore.
         *
         * Private para encapsular detalles de implementación.
         * El nombre "avatar_uri_key" es descriptivo y único.
         */
        private val AVATAR_URI_KEY = stringPreferencesKey("avatar_uri_key")
    }

    /**
     * Obtiene el URI del avatar como Flow reactivo.
     *
     * **Flow reactivo:**
     * Emite el valor inicial y luego cada vez que cambia en DataStore.
     * Perfecto para integrarse con StateFlow en ViewModels.
     *
     * **Transformación:**
     * - `data[AVATAR_URI_KEY]` obtiene el String guardado
     * - `?.let { Uri.parse(it) }` convierte String a Uri solo si no es null
     * - El Flow emite `Uri?` (puede ser null si no hay avatar guardado)
     *
     * **Manejo de errores:**
     * DataStore maneja errores internamente. Si hay corrupción de datos,
     * DataStore intenta recuperarse o devuelve valores por defecto (null).
     *
     * @return Flow que emite el Uri del avatar o null si no hay ninguno guardado
     */
    fun getAvatarUri(): Flow<Uri?> {
        return context.avatarDataStore.data.map { preferences ->
            preferences[AVATAR_URI_KEY]?.let { uriString ->
                Uri.parse(uriString)
            }
        }
    }

    /**
     * Guarda el URI del avatar en DataStore.
     *
     * **Operación suspend:**
     * Esta función debe llamarse desde una coroutine (ej: viewModelScope).
     * DataStore.edit() es una suspend function que no bloquea el hilo principal.
     *
     * **Transaccionalidad:**
     * `edit {}` garantiza que todas las operaciones dentro del bloque
     * se ejecuten de forma atómica. No hay estados intermedios inconsistentes.
     *
     * **Casos de uso:**
     * - `uri != null`: Guarda el URI como String
     * - `uri == null`: Elimina el avatar (llamando a clearAvatarUri)
     *
     * **Persistencia:**
     * Los cambios se escriben inmediatamente al disco.
     * Sobreviven al cierre de la app y reinicio del dispositivo.
     *
     * **Ejemplo:**
     * ```kotlin
     * viewModelScope.launch {
     *     avatarRepository.saveAvatarUri(selectedUri)
     * }
     * ```
     *
     * @param uri URI de la imagen a guardar, o null para eliminar
     */
    suspend fun saveAvatarUri(uri: Uri?) {
        if (uri != null) {
            context.avatarDataStore.edit { preferences ->
                preferences[AVATAR_URI_KEY] = uri.toString()
            }
        } else {
            // Si uri es null, eliminar el avatar guardado
            clearAvatarUri()
        }
    }

    /**
     * Elimina el URI del avatar de DataStore.
     *
     * **Uso:**
     * Llamar cuando el usuario quiere eliminar su avatar
     * o cuando se cierra sesión.
     *
     * **Implementación:**
     * Usa `remove()` para eliminar la key del preferences.
     * Después de esto, `getAvatarUri()` emitirá `null`.
     *
     * **Ejemplo de uso:**
     * ```kotlin
     * // Botón "Eliminar avatar"
     * Button(onClick = {
     *     viewModelScope.launch {
     *         avatarRepository.clearAvatarUri()
     *     }
     * })
     * ```
     */
    suspend fun clearAvatarUri() {
        context.avatarDataStore.edit { preferences ->
            preferences.remove(AVATAR_URI_KEY)
        }
    }
}

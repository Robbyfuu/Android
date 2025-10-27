package com.example.miappmodular.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Extension property para crear DataStore con configuración lazy y singleton.
 *
 * DataStore reemplaza SharedPreferences como solución moderna para persistencia
 * de preferencias. Ofrece:
 * - API basada en coroutinas (type-safe)
 * - Transacciones atómicas
 * - Manejo de errores robusto
 * - Observabilidad con Flow
 *
 * El nombre "session_preferences" identifica el archivo de preferencias en disco.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_preferences"
)

/**
 * Gestor centralizado de sesión de usuario y preferencias de la aplicación.
 *
 * Esta clase maneja la persistencia segura de datos de sesión usando DataStore,
 * reemplazando el enfoque tradicional de SharedPreferences. Proporciona APIs
 * reactivas con [Flow] para observar cambios en tiempo real.
 *
 * **Responsabilidades:**
 * - Almacenar/recuperar datos de sesión del usuario autenticado
 * - Gestionar estado de login (isLoggedIn)
 * - Manejar preferencia "Recordarme"
 * - Almacenar preferencias de tema (light/dark/system)
 * - Proporcionar APIs suspend para operaciones de escritura
 * - Exponer Flows para observar cambios reactivamente
 *
 * **Ventajas de DataStore sobre SharedPreferences:**
 * - Thread-safe por diseño (sin necesidad de `commit()` vs `apply()`)
 * - Type-safe con Preferences keys
 * - Soporte nativo para coroutines
 * - Manejo de errores con excepciones en lugar de fallos silenciosos
 *
 * Ejemplo de uso en ViewModel:
 * ```kotlin
 * class LoginViewModel(
 *     private val sessionManager: SessionManager
 * ) : ViewModel() {
 *     val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
 *         .stateIn(viewModelScope, SharingStarted.Eagerly, false)
 *
 *     fun login(email: String, password: String, rememberMe: Boolean) {
 *         viewModelScope.launch {
 *             // ... autenticación exitosa ...
 *             sessionManager.saveUserSession(
 *                 userId = user.id,
 *                 email = user.email,
 *                 name = user.name,
 *                 rememberMe = rememberMe
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * @property context Contexto de Android para acceder a DataStore.
 *                   Preferiblemente usar Application context.
 *
 * @see UserSession
 * @see com.example.miappmodular.repository.UserRepository
 */
class SessionManager(private val context: Context) {

    companion object {
        /** Clave para almacenar el ID del usuario autenticado */
        private val KEY_USER_ID = stringPreferencesKey("user_id")

        /** Clave para almacenar el email del usuario autenticado */
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")

        /** Clave para almacenar el nombre del usuario autenticado */
        private val KEY_USER_NAME = stringPreferencesKey("user_name")

        /** Clave para almacenar el token de autenticación JWT */
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")

        /** Clave para el flag de estado de autenticación */
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

        /** Clave para la preferencia "Recordarme" */
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")

        /** Clave para la preferencia de tema: "light", "dark" o "system" */
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    /**
     * Persiste la sesión del usuario tras autenticación exitosa.
     *
     * Almacena todos los datos necesarios para mantener la sesión activa
     * incluso tras cerrar y reabrir la aplicación (si rememberMe = true).
     *
     * Esta función es atómica: todos los valores se escriben en una sola
     * transacción, evitando estados inconsistentes.
     *
     * @param userId Identificador único del usuario (UUID).
     * @param email Correo electrónico del usuario autenticado.
     * @param name Nombre completo del usuario.
     * @param authToken Token JWT de autenticación para autorizar peticiones a la API.
     * @param rememberMe Si es true, la sesión persiste indefinidamente.
     *                   Si es false, se limpia al llamar [clearSession].
     *
     * @see clearSession
     * @see isLoggedIn
     */
    suspend fun saveUserSession(
        userId: String,
        email: String,
        name: String,
        authToken: String,
        rememberMe: Boolean = false
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_NAME] = name
            preferences[KEY_AUTH_TOKEN] = authToken
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_REMEMBER_ME] = rememberMe
        }
    }

    /**
     * Flow que emite el estado de autenticación del usuario.
     *
     * Emite `true` si el usuario ha iniciado sesión, `false` en caso contrario.
     * Se actualiza automáticamente cuando se llama a [saveUserSession] o [clearSession].
     *
     * Ejemplo de observación en Composable:
     * ```kotlin
     * val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = false)
     *
     * if (isLoggedIn) {
     *     HomeScreen()
     * } else {
     *     LoginScreen()
     * }
     * ```
     *
     * @return Flow que emite true si el usuario está autenticado, false si no.
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    /**
     * Flow que emite los datos completos de la sesión del usuario autenticado.
     *
     * Emite un objeto [UserSession] con todos los datos del usuario si está
     * autenticado, o `null` si no hay sesión activa.
     *
     * Útil para mostrar información del usuario en la UI sin hacer queries
     * a la base de datos.
     *
     * Ejemplo de uso:
     * ```kotlin
     * val userSession by sessionManager.userSession.collectAsState(initial = null)
     *
     * userSession?.let { session ->
     *     Text("Bienvenido, ${session.name}")
     *     Text(session.email)
     * }
     * ```
     *
     * @return Flow que emite [UserSession] si hay sesión, null si no.
     *
     * @see UserSession
     */
    val userSession: Flow<UserSession?> = context.dataStore.data.map { preferences ->
        if (preferences[KEY_IS_LOGGED_IN] == true) {
            UserSession(
                userId = preferences[KEY_USER_ID] ?: "",
                email = preferences[KEY_USER_EMAIL] ?: "",
                name = preferences[KEY_USER_NAME] ?: "",
                rememberMe = preferences[KEY_REMEMBER_ME] ?: false
            )
        } else null
    }

    /**
     * Guarda únicamente el token de autenticación.
     *
     * Este método es útil para guardar el token inmediatamente después
     * del login/registro, antes de obtener los datos completos del usuario.
     * Permite que AuthInterceptor tenga acceso al token para peticiones
     * autenticadas subsecuentes (como GET /auth/me).
     *
     * @param token Token JWT de autenticación.
     *
     * @see saveUserSession
     * @see getAuthToken
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
        }
    }

    /**
     * Obtiene el token de autenticación almacenado.
     *
     * Esta función suspendida recupera el authToken de DataStore.
     * Útil para peticiones manuales a la API que requieran el token.
     *
     * @return El token JWT si existe, null si no hay sesión activa.
     *
     * @see saveUserSession
     */
    suspend fun getAuthToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_AUTH_TOKEN]
        }.first()
    }

    /**
     * Cierra la sesión del usuario y limpia los datos de sesión.
     *
     * Elimina todos los datos del usuario y marca [isLoggedIn] como false.
     *
     * **Comportamiento con "Recordarme":**
     * - Si `rememberMe` era `false`: Limpia TODAS las preferencias (incluyendo tema).
     * - Si `rememberMe` era `true`: Solo elimina datos de sesión, preserva otras preferencias.
     *
     * Esta función es idempotente: se puede llamar múltiples veces sin efectos adversos.
     *
     * Ejemplo de uso en logout:
     * ```kotlin
     * fun logout() {
     *     viewModelScope.launch {
     *         sessionManager.clearSession()
     *         navController.navigate("login") {
     *             popUpTo(0) { inclusive = true }
     *         }
     *     }
     * }
     * ```
     *
     * @see saveUserSession
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_AUTH_TOKEN)
            preferences[KEY_IS_LOGGED_IN] = false
            if (preferences[KEY_REMEMBER_ME] != true) {
                preferences.clear()
            }
        }
    }

    /**
     * Guarda la preferencia de tema del usuario.
     *
     * @param mode Modo de tema: "light", "dark" o "system" (sigue el tema del sistema).
     *
     * @see themeMode
     */
    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    /**
     * Flow que emite la preferencia de tema del usuario.
     *
     * Valores posibles:
     * - "light": Modo claro forzado
     * - "dark": Modo oscuro forzado
     * - "system": Sigue la preferencia del sistema (valor por defecto)
     *
     * @return Flow que emite el modo de tema actual.
     */
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "system"
    }
}

/**
 * Data class que representa los datos de sesión de un usuario autenticado.
 *
 * Este modelo se usa para transportar datos de sesión de forma type-safe
 * entre [SessionManager] y otros componentes de la app.
 *
 * Ejemplo de uso:
 * ```kotlin
 * sessionManager.userSession.collect { session: UserSession? ->
 *     session?.let {
 *         println("Usuario: ${it.name} (${it.email})")
 *         println("Recordarme: ${it.rememberMe}")
 *     }
 * }
 * ```
 *
 * @property userId Identificador único (UUID) del usuario autenticado.
 * @property email Correo electrónico del usuario.
 * @property name Nombre completo del usuario.
 * @property rememberMe Indica si el usuario eligió "Recordarme" en el login.
 *
 * @see SessionManager
 */
data class UserSession(
    val userId: String,
    val email: String,
    val name: String,
    val rememberMe: Boolean
)
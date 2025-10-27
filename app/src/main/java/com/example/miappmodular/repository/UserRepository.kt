package com.example.miappmodular.repository

import android.content.Context
import com.example.miappmodular.data.local.SessionManager
import com.example.miappmodular.data.local.database.AppDatabase
import com.example.miappmodular.data.remote.dto.LoginRequest
import com.example.miappmodular.data.remote.dto.SignUpRequest
import com.example.miappmodular.data.local.entity.User
import com.example.miappmodular.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Date

/**
 * Repository que coordina operaciones de usuario entre fuentes de datos remotas y locales.
 *
 * Esta clase implementa el patrón Repository de la arquitectura MVVM/Clean Architecture,
 * actuando como única fuente de verdad (Single Source of Truth) para datos de usuario.
 * Coordina tres fuentes de datos:
 * 1. **API remota:** Xano (autenticación, registro, sincronización)
 * 2. **Base de datos local:** Room (caché persistente, modo offline)
 * 3. **Sesión:** DataStore (estado de autenticación, preferencias)
 *
 * **Responsabilidades:**
 * - Abstraer la complejidad de múltiples fuentes de datos
 * - Coordinar llamadas a API y sincronización con BD local
 * - Transformar DTOs de red a entidades de dominio
 * - Manejar errores de red y persistencia
 * - Gestionar sesión de usuario (login/logout)
 * - Proporcionar APIs suspend y Flow para consumo reactivo
 *
 * **Arquitectura de datos:**
 * ```
 * ViewModel → UserRepository → {AuthApiService, UserDao, SessionManager}
 *                  ↓
 *           Result<User> / Flow<List<User>>
 * ```
 *
 * **Manejo de errores:**
 * Todos los métodos suspend retornan `Result<T>` que encapsula:
 * - `Result.success(data)`: Operación exitosa
 * - `Result.failure(exception)`: Error con mensaje descriptivo
 *
 * Ejemplo de uso en ViewModel:
 * ```kotlin
 * class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
 *     fun login(email: String, password: String, rememberMe: Boolean) {
 *         viewModelScope.launch {
 *             when (val result = userRepository.loginUser(email, password, rememberMe)) {
 *                 is Result.Success -> {
 *                     _uiState.value = LoginUiState(success = true)
 *                     navigateToHome()
 *                 }
 *                 is Result.Failure -> {
 *                     _uiState.value = LoginUiState(error = result.exception.message)
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ⚠️ **Nota de seguridad:**
 * Actualmente usa SHA-256 para hashing de contraseñas, que es insuficiente para producción.
 * Se recomienda migrar a BCrypt, Argon2 o PBKDF2 con salt.
 *
 * @param context Contexto de Android para inicializar Room y SessionManager.
 *                Preferiblemente usar Application context para evitar leaks.
 *
 * @see com.example.miappmodular.model.entity.User
 * @see com.example.miappmodular.model.dao.UserDao
 * @see com.example.miappmodular.network.AuthApiService
 * @see com.example.miappmodular.model.SessionManager
 */
class UserRepository(context: Context) {

    /** DAO de Room para operaciones CRUD en tabla "users" */
    private val userDao = AppDatabase.getDatabase(context).userDao()

    /** Gestor de sesión para persistir estado de autenticación */
    private val sessionManager = SessionManager(context)

    /** Servicio Retrofit para llamadas a API de Xano */
    private val apiService = RetrofitClient.authApiService

    /**
     * Registra un nuevo usuario en el sistema integrando API remota y BD local.
     *
     * **Flujo de ejecución:**
     * 1. Envía petición POST a API de Xano (`/auth/signup`)
     * 2. Si es exitoso, crea entidad [User] con datos de la respuesta
     * 3. Inserta usuario en base de datos local (Room)
     * 4. Guarda sesión activa en DataStore
     * 5. Retorna el usuario creado
     *
     * **Manejo de errores:**
     * - Email ya registrado: HTTP 400 → Result.failure("Error en el registro")
     * - Sin conexión: IOException → Result.failure("Error de conexión: ...")
     * - Servidor caído: HTTP 500 → Result.failure del errorBody
     *
     * Ejemplo de uso:
     * ```kotlin
     * viewModelScope.launch {
     *     val result = userRepository.registerUser("Juan Pérez", "juan@example.com", "Pass123!")
     *
     *     result.onSuccess { user ->
     *         println("Usuario creado: ${user.name}")
     *         navigateToHome()
     *     }.onFailure { exception ->
     *         showError(exception.message ?: "Error desconocido")
     *     }
     * }
     * ```
     *
     * @param name Nombre completo del usuario (mínimo 3 caracteres).
     * @param email Email válido y único en el sistema.
     * @param password Contraseña segura (mínimo 8 chars, mayús, minús, número).
     * @return [Result]<[User]> con el usuario creado o error.
     *
     * @see SignUpRequest
     * @see AuthResponse
     */
    suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Llamar a la API de Xano
            val request = SignUpRequest(name, email, password)
            val response = apiService.signUp(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar el token PRIMERO para que el AuthInterceptor pueda usarlo
                // en la llamada a userActually()
                sessionManager.saveAuthToken(authResponse.authToken ?: "")

                // Ahora obtener los datos completos del usuario con el token en el header
                val currentUser = apiService.userActually().body()!!

                // Guardar la sesión completa con todos los datos del usuario
                sessionManager.saveUserSession(
                    userId = currentUser.id?.toString() ?: java.util.UUID.randomUUID().toString(),
                    email = currentUser.email ?: email,
                    name = currentUser.name ?: name,
                    authToken = authResponse.authToken ?: "",
                    rememberMe = false
                )


                // Crear usuario local con los datos completos de la API
                val user = User(
                    id = currentUser.id?.toString() ?: java.util.UUID.randomUUID().toString(),
                    name = currentUser.name ?: name,
                    email = currentUser.email ?: email,
                    passwordHash = hashPassword(password),
                    createdAt = Date(currentUser.createdAt ?: System.currentTimeMillis())
                )

                // Guardar en base de datos local
                userDao.insertUser(user)

                Result.success(user)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error en el registro"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Autentica un usuario existente contra la API de Xano.
     *
     * **Flujo de ejecución:**
     * 1. Envía credenciales a API de Xano (`POST /auth/login`)
     * 2. Si es exitoso, crea/actualiza entidad [User] con datos de respuesta
     * 3. Actualiza `lastLogin` a la fecha/hora actual
     * 4. Inserta/actualiza usuario en base de datos local (upsert)
     * 5. Guarda sesión con preferencia "Recordarme"
     * 6. Retorna el usuario autenticado
     *
     * **Diferencia con registerUser:**
     * Login actualiza un usuario existente en lugar de crear uno nuevo.
     * Si el usuario no existe localmente, se crea desde los datos de la API.
     *
     * **Manejo de "Recordarme":**
     * - `rememberMe = true`: La sesión persiste indefinidamente
     * - `rememberMe = false`: La sesión se limpia al cerrar sesión manualmente
     *
     * Ejemplo de uso:
     * ```kotlin
     * val result = userRepository.loginUser(
     *     email = "usuario@example.com",
     *     password = "MiPassword123!",
     *     rememberMe = true
     * )
     *
     * result.onSuccess { user ->
     *     println("Bienvenido, ${user.name}!")
     *     println("Último acceso: ${user.lastLogin}")
     * }.onFailure {
     *     showError("Credenciales incorrectas")
     * }
     * ```
     *
     * @param email Correo electrónico del usuario registrado.
     * @param password Contraseña del usuario (se envía a API por HTTPS).
     * @param rememberMe Si true, la sesión persiste tras cerrar la app.
     * @return [Result]<[User]> con el usuario autenticado o error.
     *
     * @see registerUser
     * @see logout
     */
    suspend fun loginUser(
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Llamar a la API de Xano
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar el token PRIMERO para que el AuthInterceptor pueda usarlo
                // en la llamada a userActually()
                sessionManager.saveAuthToken(authResponse.authToken ?: "")

                // Ahora obtener los datos completos del usuario con el token en el header
                val currentUser = apiService.userActually().body()!!

                // Guardar la sesión completa con todos los datos del usuario
                sessionManager.saveUserSession(
                    userId = currentUser.id?.toString() ?: java.util.UUID.randomUUID().toString(),
                    email = currentUser.email ?: email,
                    name = currentUser.name ?: "",
                    authToken = authResponse.authToken ?: "",
                    rememberMe = rememberMe
                )



                // Crear o actualizar usuario local con los datos completos de la API
                val user = User(
                    id = currentUser.id?.toString() ?: java.util.UUID.randomUUID().toString(),
                    name = currentUser.name ?: "",
                    email = currentUser.email ?: email,
                    passwordHash = hashPassword(password),
                    createdAt = Date(currentUser.createdAt ?: System.currentTimeMillis())
                )

                // Guardar/actualizar en base de datos local
                userDao.insertUser(user)

                Result.success(user)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Credenciales incorrectas"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * Limpia todos los datos de sesión de DataStore, marcando al usuario como
     * no autenticado. Los datos del usuario permanecen en la base de datos local.
     *
     * **Comportamiento:**
     * - Elimina userId, email, name de DataStore
     * - Marca `isLoggedIn = false`
     * - Si `rememberMe` era false, limpia TODAS las preferencias (incluyendo tema)
     * - Si `rememberMe` era true, preserva preferencias no relacionadas con sesión
     *
     * Ejemplo de uso:
     * ```kotlin
     * fun onLogoutClick() {
     *     viewModelScope.launch {
     *         userRepository.logout()
     *         navController.navigate("login") {
     *             popUpTo(0) { inclusive = true }
     *         }
     *     }
     * }
     * ```
     *
     * @see loginUser
     * @see SessionManager.clearSession
     */
    suspend fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Obtiene el usuario autenticado actualmente desde la base de datos local.
     *
     * Primero consulta la sesión activa en DataStore para obtener el userId,
     * luego consulta Room para obtener el objeto User completo.
     *
     * **Casos de uso:**
     * - Cargar perfil del usuario en pantalla de perfil
     * - Mostrar nombre/email en la UI
     * - Verificar si hay sesión activa
     *
     * Ejemplo de uso:
     * ```kotlin
     * viewModelScope.launch {
     *     val currentUser = userRepository.getCurrentUser()
     *     if (currentUser != null) {
     *         _userName.value = currentUser.name
     *         _userEmail.value = currentUser.email
     *     } else {
     *         navigateToLogin()
     *     }
     * }
     * ```
     *
     * @return [User] si hay sesión activa, `null` si no hay usuario autenticado.
     *
     * @see SessionManager.userSession
     * @see UserDao.getUserById
     */
    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val session = sessionManager.userSession.firstOrNull()
        session?.let {
            userDao.getUserById(it.userId)
        }
    }

    /**
     * Obtiene un Flow reactivo de todos los usuarios almacenados localmente.
     *
     * El Flow emite automáticamente una nueva lista cada vez que los datos
     * cambian en la base de datos (insert, update, delete).
     *
     * Los usuarios están ordenados por fecha de creación descendente
     * (más recientes primero).
     *
     * **Casos de uso:**
     * - Pantalla administrativa de usuarios
     * - Selector de perfiles múltiples
     * - Debugging/testing
     *
     * Ejemplo de observación en ViewModel:
     * ```kotlin
     * val allUsers: StateFlow<List<User>> = userRepository.getAllUsers()
     *     .stateIn(
     *         scope = viewModelScope,
     *         started = SharingStarted.WhileSubscribed(5000),
     *         initialValue = emptyList()
     *     )
     * ```
     *
     * @return [Flow] que emite la lista actualizada de usuarios.
     *
     * @see UserDao.getAllUsers
     */
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    /**
     * Actualiza los datos de perfil de un usuario en la base de datos local.
     *
     * Útil para actualizar información como nombre, email, imagen de perfil, etc.
     * Room identifica el usuario a actualizar por su Primary Key (id).
     *
     * **Nota:** Esta operación solo actualiza la BD local, NO sincroniza con la API.
     * Para sincronización completa, implementar un endpoint PATCH en el backend.
     *
     * Ejemplo de uso:
     * ```kotlin
     * val updatedUser = currentUser.copy(
     *     name = "Nuevo Nombre",
     *     profileImagePath = "/storage/profile.jpg"
     * )
     *
     * userRepository.updateUserProfile(updatedUser).onSuccess {
     *     showMessage("Perfil actualizado")
     * }.onFailure {
     *     showError("Error al actualizar perfil")
     * }
     * ```
     *
     * @param user Objeto [User] con los datos actualizados. Debe mantener el mismo `id`.
     * @return [Result]<[Unit]> indicando éxito o error.
     *
     * @see UserDao.updateUser
     */
    suspend fun updateUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina TODOS los usuarios de la base de datos local (operación TRUNCATE).
     *
     * ⚠️ **OPERACIÓN DESTRUCTIVA E IRREVERSIBLE**
     *
     * Borra todos los registros de la tabla "users" y cierra la sesión actual.
     * Útil para:
     * - Testing y debugging
     * - "Cerrar sesión en todos los dispositivos"
     * - Reset completo de la aplicación
     *
     * **Precauciones:**
     * - Los datos NO se pueden recuperar una vez eliminados
     * - Los usuarios tendrán que registrarse nuevamente
     * - Considerar mostrar confirmación al usuario antes de ejecutar
     *
     * Ejemplo de uso seguro:
     * ```kotlin
     * fun onDeleteAllUsersClick() {
     *     showConfirmDialog(
     *         message = "¿Seguro que deseas eliminar TODOS los usuarios? Esta acción no se puede deshacer.",
     *         onConfirm = {
     *             viewModelScope.launch {
     *                 userRepository.deleteAllUsers().onSuccess {
     *                     showMessage("Todos los usuarios han sido eliminados")
     *                     navigateToLogin()
     *                 }
     *             }
     *         }
     *     )
     * }
     * ```
     *
     * @return [Result]<[Unit]> indicando éxito o error.
     *
     * @see UserDao.deleteAllUsers
     * @see logout
     */
    suspend fun deleteAllUsers(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.deleteAllUsers()
            // También limpiar la sesión actual
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un hash SHA-256 de la contraseña del usuario.
     *
     * **⚠️ ADVERTENCIA DE SEGURIDAD:**
     * SHA-256 simple es inadecuado para hashing de contraseñas en producción.
     * Es vulnerable a ataques de rainbow tables y fuerza bruta por su velocidad.
     *
     * **Recomendaciones para producción:**
     * 1. Migrar a **BCrypt** (recomendado):
     *    ```kotlin
     *    import org.mindrot.jbcrypt.BCrypt
     *    fun hashPassword(password: String) = BCrypt.hashpw(password, BCrypt.gensalt(12))
     *    ```
     * 2. O usar **Argon2** (más moderno):
     *    ```kotlin
     *    import de.mkammerer.argon2.Argon2Factory
     *    val argon2 = Argon2Factory.create()
     *    fun hashPassword(password: String) = argon2.hash(10, 65536, 1, password)
     *    ```
     * 3. O **PBKDF2** con salt (nativo de Android):
     *    ```kotlin
     *    import javax.crypto.SecretKeyFactory
     *    import javax.crypto.spec.PBEKeySpec
     *    ```
     *
     * **¿Por qué SHA-256 es inseguro para contraseñas?**
     * - Es demasiado rápido (permite billones de intentos/segundo en GPUs)
     * - No incluye salt (dos usuarios con misma contraseña tienen mismo hash)
     * - No tiene "cost factor" configurable
     *
     * @param password Contraseña en texto plano a hashear.
     * @return Hash hexadecimal de 64 caracteres (256 bits).
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
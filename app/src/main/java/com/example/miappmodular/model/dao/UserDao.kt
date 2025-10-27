package com.example.miappmodular.model.dao

import androidx.room.*
import com.example.miappmodular.model.entity.User
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object (DAO) para operaciones CRUD en la entidad [User].
 *
 * Esta interfaz define el contrato de acceso a datos para la tabla "users"
 * en la base de datos Room. Room genera automáticamente la implementación
 * de todos los métodos anotados en tiempo de compilación.
 *
 * Todos los métodos marcados con `suspend` deben ejecutarse desde una corrutina
 * o un scope de corrutina (como `viewModelScope`). Los métodos que retornan
 * [Flow] emiten actualizaciones automáticas cuando los datos cambian en la BD.
 *
 * Ejemplo de uso básico:
 * ```kotlin
 * class UserRepository(private val userDao: UserDao) {
 *     suspend fun registerUser(user: User) {
 *         userDao.insertUser(user)
 *     }
 *
 *     suspend fun authenticateUser(email: String): User? {
 *         return userDao.getUserByEmail(email)
 *     }
 *
 *     val allUsers: Flow<List<User>> = userDao.getAllUsers()
 * }
 * ```
 *
 * @see com.example.miappmodular.model.entity.User
 * @see com.example.miappmodular.model.database.AppDatabase
 * @see com.example.miappmodular.repository.UserRepository
 */
@Dao
interface UserDao {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * Útil para la autenticación y para verificar si un email ya está registrado.
     * La consulta es case-sensitive.
     *
     * @param email Dirección de correo electrónico a buscar (case-sensitive).
     * @return El [User] encontrado o `null` si no existe un usuario con ese email.
     *
     * @see getUserById
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id UUID del usuario a buscar.
     * @return El [User] encontrado o `null` si no existe un usuario con ese ID.
     *
     * @see getUserByEmail
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * Si ya existe un usuario con el mismo ID (Primary Key), este será
     * reemplazado por completo debido a [OnConflictStrategy.REPLACE].
     * En la mayoría de casos, esto funciona como un "upsert" (insert or update).
     *
     * @param user Objeto [User] a insertar/reemplazar en la base de datos.
     *
     * @see updateUser
     * @see deleteUser
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * Room usa la Primary Key (id) del objeto [User] para identificar
     * qué fila actualizar. Si no existe un usuario con ese ID, no se
     * realiza ninguna operación.
     *
     * @param user Objeto [User] con los datos actualizados. Debe contener el mismo ID.
     *
     * @see insertUser
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Elimina un usuario específico de la base de datos.
     *
     * Room usa la Primary Key (id) del objeto [User] para identificar
     * qué fila eliminar.
     *
     * @param user Objeto [User] a eliminar. Solo se requiere el ID.
     *
     * @see deleteAllUsers
     */
    @Delete
    suspend fun deleteUser(user: User)

    /**
     * Obtiene todos los usuarios ordenados por fecha de creación descendente.
     *
     * Retorna un [Flow] que emite automáticamente una nueva lista cada vez
     * que los datos en la tabla "users" cambian (insert, update, delete).
     * Esto permite observar cambios en tiempo real desde la UI.
     *
     * Los usuarios más recientes aparecen primero en la lista.
     *
     * Ejemplo de observación en ViewModel:
     * ```kotlin
     * val users: StateFlow<List<User>> = userDao.getAllUsers()
     *     .stateIn(
     *         scope = viewModelScope,
     *         started = SharingStarted.WhileSubscribed(5000),
     *         initialValue = emptyList()
     *     )
     * ```
     *
     * @return [Flow] que emite la lista actualizada de todos los usuarios.
     *
     * @see getUserCount
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Cuenta el número total de usuarios registrados en la base de datos.
     *
     * Útil para estadísticas, validaciones o para verificar si es el primer
     * usuario que se registra en el dispositivo.
     *
     * @return Número total de usuarios en la tabla "users".
     *
     * @see getAllUsers
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    /**
     * Actualiza la fecha del último inicio de sesión de un usuario específico.
     *
     * Este método es más eficiente que cargar el usuario completo, modificar
     * el campo `lastLogin` y llamar a [updateUser], ya que solo actualiza
     * una columna específica.
     *
     * Se invoca típicamente después de una autenticación exitosa.
     *
     * @param userId ID (UUID) del usuario cuyo último login se actualizará.
     * @param lastLogin Nueva fecha y hora del último inicio de sesión.
     *
     * @see User.lastLogin
     */
    @Query("UPDATE users SET lastLogin = :lastLogin WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, lastLogin: Date)

    /**
     * Elimina TODOS los usuarios de la base de datos.
     *
     * ⚠️ **OPERACIÓN DESTRUCTIVA**: Esta acción no se puede deshacer.
     * Úsala solo para funcionalidades de desarrollo, testing o
     * "cerrar sesión en todos los dispositivos".
     *
     * Ejemplo de uso seguro:
     * ```kotlin
     * // Mostrar confirmación al usuario antes de ejecutar
     * if (userConfirmed) {
     *     userDao.deleteAllUsers()
     *     sessionManager.clearSession()
     * }
     * ```
     *
     * @see deleteUser
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
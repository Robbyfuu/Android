package com.example.miappmodular.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Entidad de base de datos Room que representa un usuario del sistema.
 *
 * Esta clase define el esquema de la tabla "users" en la base de datos local SQLite.
 * Almacena información completa del usuario incluyendo credenciales hasheadas,
 * metadatos de sesión y preferencias de perfil.
 *
 * La entidad se sincroniza con la API de Xano mediante el [UserRepository], que
 * coordina operaciones entre el almacenamiento local (Room) y remoto (API REST).
 *
 * Ejemplo de creación de usuario:
 * ```kotlin
 * val newUser = User(
 *     name = "María González",
 *     email = "maria.gonzalez@example.com",
 *     passwordHash = hashPassword("SecurePass123!"),
 *     createdAt = Date(),
 *     lastLogin = Date(),
 *     profileImagePath = null
 * )
 * userDao.insertUser(newUser)
 * ```
 *
 * Ejemplo de consulta:
 * ```kotlin
 * val user = userDao.getUserByEmail("maria.gonzalez@example.com")
 * user?.let {
 *     println("Usuario encontrado: ${it.name}")
 *     println("Última sesión: ${it.lastLogin}")
 * }
 * ```
 *
 * @property id Identificador único UUID del usuario. Se genera automáticamente
 *              mediante [UUID.randomUUID] si no se especifica. Primary Key.
 * @property name Nombre completo del usuario. Requerido, no puede estar vacío.
 *                Debe tener al menos 3 caracteres según [ValidationUtils].
 * @property email Dirección de correo electrónico única del usuario. Requerido.
 *                 Debe ser un email válido según [ValidationUtils.validateEmail].
 *                 Se recomienda crear un índice UNIQUE en esta columna.
 * @property passwordHash Hash SHA-256 de la contraseña del usuario. Nunca se almacena
 *                        la contraseña en texto plano. El hash se genera mediante
 *                        [UserRepository.hashPassword]. En producción, considerar
 *                        usar BCrypt o Argon2 en lugar de SHA-256.
 * @property createdAt Fecha y hora de creación de la cuenta. Se establece una sola vez
 *                     al registrar el usuario. Usado para mostrar "Miembro desde..."
 *                     en la UI de perfil.
 * @property lastLogin Fecha y hora del último inicio de sesión exitoso. Nullable,
 *                     se actualiza mediante [UserDao.updateLastLogin] cada vez que
 *                     el usuario se autentica correctamente. Null para usuarios nuevos
 *                     que aún no han iniciado sesión.
 * @property profileImagePath Ruta local del archivo de imagen de perfil del usuario.
 *                            Nullable, ya que es opcional. Apunta a un archivo en el
 *                            almacenamiento interno de la app. Null si el usuario no
 *                            ha configurado una foto de perfil.
 *
 * @see com.example.miappmodular.model.dao.UserDao
 * @see com.example.miappmodular.repository.UserRepository
 * @see com.example.miappmodular.model.database.AppDatabase
 * @see com.example.miappmodular.utils.ValidationUtils
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Date,
    val lastLogin : Date? = null,
    val profileImagePath: String? = null
)

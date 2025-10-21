package com.example.miappmodular.repository

import android.content.Context
import com.example.miappmodular.model.SessionManager
import com.example.miappmodular.model.database.AppDatabase
import com.example.miappmodular.model.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Date

/**
 * Repository para manejo de usuarios con patrón MVVM
 */
class UserRepository(context: Context) {

    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val sessionManager = SessionManager(context)

    /**
     * Registra un nuevo usuario
     */
    suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Verificar si el usuario ya existe
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return@withContext Result.failure(
                    Exception("El email ya está registrado")
                )
            }

            // Crear nuevo usuario con password hasheado
            val user = User(
                name = name,
                email = email,
                passwordHash = hashPassword(password),
                createdAt = Date()
            )

            // Guardar en base de datos
            userDao.insertUser(user)

            // Guardar sesión
            sessionManager.saveUserSession(
                userId = user.id,
                email = user.email,
                name = user.name
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Realiza login del usuario
     */
    suspend fun loginUser(
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Buscar usuario
            val user = userDao.getUserByEmail(email)
                ?: return@withContext Result.failure(
                    Exception("Usuario no encontrado")
                )

            // Verificar contraseña
            if (user.passwordHash != hashPassword(password)) {
                return@withContext Result.failure(
                    Exception("Contraseña incorrecta")
                )
            }

            // Actualizar último login
            userDao.updateLastLogin(user.id, Date())

            // Guardar sesión
            sessionManager.saveUserSession(
                userId = user.id,
                email = user.email,
                name = user.name,
                rememberMe = rememberMe
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra sesión
     */
    suspend fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Obtiene el usuario actual
     */
    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val session = sessionManager.userSession.firstOrNull()
        session?.let {
            userDao.getUserById(it.userId)
        }
    }

    /**
     * Observable de todos los usuarios
     */
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    /**
     * Actualiza perfil del usuario
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
     * Hash de contraseña simple (en producción usar BCrypt)
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
package com.example.miappmodular.data.local.database

import android.content.Context
import androidx.room.*
import com.example.miappmodular.data.local.dao.UserDao
import com.example.miappmodular.data.local.entity.User
import java.util.Date

/**
 * Convertidores de tipos para almacenar objetos complejos en Room.
 *
 * Room solo puede almacenar tipos primitivos directamente en SQLite.
 * Esta clase proporciona conversiones bidireccionales para tipos complejos
 * como [Date], permitiendo su uso en entidades de Room.
 *
 * Los métodos anotados con [@TypeConverter] se aplican automáticamente
 * cuando Room necesita persistir o recuperar estos tipos.
 *
 * Ejemplo de uso en entidad:
 * ```kotlin
 * @Entity
 * data class User(
 *     val createdAt: Date  // Room usa Converters automáticamente
 * )
 * ```
 *
 * @see AppDatabase
 */
class Converters {
    /**
     * Convierte un timestamp Unix (Long) a un objeto [Date].
     *
     * Se invoca automáticamente cuando Room lee una columna de tipo Date
     * desde la base de datos SQLite.
     *
     * @param value Timestamp en milisegundos desde epoch Unix, o null.
     * @return Objeto [Date] correspondiente, o null si el valor es null.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convierte un objeto [Date] a timestamp Unix (Long).
     *
     * Se invoca automáticamente cuando Room escribe una columna de tipo Date
     * a la base de datos SQLite.
     *
     * @param date Objeto [Date] a convertir, o null.
     * @return Timestamp en milisegundos desde epoch Unix, o null si date es null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Clase abstracta que representa la base de datos principal de la aplicación.
 *
 * Esta clase configura Room Database con patrón Singleton thread-safe para
 * garantizar una única instancia en toda la aplicación. Gestiona todas las
 * entidades, DAOs y configuraciones de migración de la base de datos local.
 *
 * **Configuración actual:**
 * - **Nombre de BD:** "app_database"
 * - **Versión:** 1
 * - **Entidades:** [User]
 * - **Migración:** Destructiva (`.fallbackToDestructiveMigration()`)
 * - **Convertidores:** [Converters] para tipos [Date]
 *
 * **Patrón Singleton:**
 * Usa `@Volatile` y `synchronized` para garantizar thread-safety en contextos
 * multi-hilo. La instancia se crea lazy (solo cuando se necesita).
 *
 * Ejemplo de uso en Repository:
 * ```kotlin
 * class UserRepository(context: Context) {
 *     private val database = AppDatabase.getDatabase(context)
 *     private val userDao = database.userDao()
 *
 *     suspend fun getAllUsers(): List<User> {
 *         return userDao.getAllUsers().first()
 *     }
 * }
 * ```
 *
 * Ejemplo de uso con Dependency Injection (Hilt):
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object DatabaseModule {
 *     @Provides
 *     @Singleton
 *     fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
 *         return AppDatabase.getDatabase(context)
 *     }
 * }
 * ```
 *
 * ⚠️ **Nota sobre migraciones:**
 * Actualmente usa `.fallbackToDestructiveMigration()`, que ELIMINA todos los
 * datos al cambiar la versión del esquema. Para producción, implementa
 * migraciones explícitas con `Migration` para preservar datos del usuario.
 *
 * @see UserDao
 * @see User
 * @see Converters
 */
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO de usuarios.
     *
     * Room genera automáticamente la implementación de esta función abstracta.
     *
     * @return Instancia singleton del [UserDao] para operaciones CRUD en usuarios.
     */
    abstract fun userDao(): UserDao

    companion object {
        /**
         * Instancia singleton de la base de datos.
         *
         * `@Volatile` garantiza que los cambios a INSTANCE sean visibles
         * inmediatamente en todos los hilos.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia singleton de la base de datos de forma thread-safe.
         *
         * Si la instancia no existe, la crea dentro de un bloque `synchronized`
         * para evitar condiciones de carrera en entornos multi-hilo.
         *
         * Usa `applicationContext` en lugar de `context` para evitar memory leaks
         * asociados con contextos de Activity o Fragment.
         *
         * @param context Contexto de Android, preferiblemente [Application] context.
         * @return Instancia singleton de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
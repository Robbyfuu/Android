package com.example.miappmodular

import android.app.Application
import com.example.miappmodular.data.local.SessionManager
import com.example.miappmodular.data.local.dao.UserDao
import com.example.miappmodular.data.local.database.AppDatabase
import com.example.miappmodular.data.remote.AuthApiService
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.repository.AvatarRepository
import com.example.miappmodular.repository.UserRepository

/**
 * Contenedor simple de dependencias para la aplicación.
 *
 * Este contenedor centraliza la creación y gestión de dependencias compartidas,
 * proporcionando una alternativa más simple a frameworks como Hilt o Dagger.
 *
 * **¿Por qué usar un contenedor de dependencias?**
 *
 * **Problema sin contenedor:**
 * ```kotlin
 * // En LoginViewModel
 * val userRepository = UserRepository(application)  // Instancia #1
 *
 * // En RegisterViewModel
 * val userRepository = UserRepository(application)  // Instancia #2
 *
 * // En ProfileViewModel
 * val userRepository = UserRepository(application)  // Instancia #3
 * ```
 * ❌ Resultado: 3 instancias diferentes, caché no compartida, más memoria usada
 *
 * **Solución con contenedor:**
 * ```kotlin
 * // En LoginViewModel, RegisterViewModel, ProfileViewModel
 * val userRepository = AppDependencies.getInstance(application).userRepository
 * ```
 * ✅ Resultado: 1 instancia compartida, caché compartida, eficiente
 *
 * **Ventajas sobre creación manual:**
 * - ✅ Una sola instancia de cada dependencia (Singleton pattern)
 * - ✅ Fácil de testear: puedes reemplazar el contenedor con mocks
 * - ✅ Centralizado: si UserRepository necesita nuevas dependencias, cambias 1 lugar
 * - ✅ Simple de entender: no requiere anotaciones ni compilación especial
 *
 * **Ventajas sobre Hilt/Dagger:**
 * - ✅ No requiere configuración gradle compleja
 * - ✅ No requiere anotaciones (@Inject, @Module, etc.)
 * - ✅ Código 100% explícito y visible
 * - ✅ Perfecto para aprender fundamentos antes de usar frameworks
 *
 * **Ejemplo de uso en ViewModels:**
 * ```kotlin
 * class ProfileViewModel(application: Application) : AndroidViewModel(application) {
 *     // Obtener dependencias del contenedor
 *     private val dependencies = AppDependencies.getInstance(application)
 *     private val userRepository = dependencies.userRepository
 *
 *     // O en una sola línea:
 *     private val userRepository = AppDependencies.getInstance(application).userRepository
 * }
 * ```
 *
 * **Ejemplo de testing:**
 * ```kotlin
 * @Test
 * fun testProfileViewModel() {
 *     // Crear mock repository
 *     val mockRepo = mockk<UserRepository>()
 *     every { mockRepo.getCurrentUser() } returns fakeUser
 *
 *     // Reemplazar el contenedor real con uno de test
 *     AppDependencies.setTestInstance(
 *         AppDependencies(
 *             userRepository = mockRepo,
 *             sessionManager = mockSessionManager,
 *             database = mockDatabase,
 *             apiService = mockApiService
 *         )
 *     )
 *
 *     // Crear ViewModel (usará el mock)
 *     val viewModel = ProfileViewModel(testApplication)
 *     // Verificar comportamiento...
 * }
 * ```
 *
 * **Patrón de diseño:**
 * Este es el "Service Locator Pattern", un patrón intermedio entre:
 * - Creación manual (muy simple pero repetitivo)
 * - Dependency Injection frameworks (muy potente pero complejo)
 *
 * @property userRepository Repositorio único compartido por todos los ViewModels
 * @property sessionManager Gestor de sesión único para toda la app
 * @property database Instancia de Room Database (singleton)
 * @property apiService Cliente de API Retrofit
 * @property avatarRepository Repositorio para persistencia del avatar del usuario
 *
 * @see UserRepository
 * @see SessionManager
 * @see AppDatabase
 * @see AuthApiService
 * @see AvatarRepository
 */
class AppDependencies(
    val userRepository: UserRepository,
    val sessionManager: SessionManager,
    val database: AppDatabase,
    val apiService: AuthApiService,
    val userDao: UserDao,
    val avatarRepository: AvatarRepository
) {
    companion object {
        /**
         * Instancia única del contenedor (Singleton).
         * Se inicializa lazy: solo se crea cuando se usa por primera vez.
         */
        @Volatile
        private var INSTANCE: AppDependencies? = null

        /**
         * Instancia de prueba para testing.
         * Si está configurada, getInstance() retorna esta en lugar de la real.
         */
        @Volatile
        private var TEST_INSTANCE: AppDependencies? = null

        /**
         * Obtiene la instancia única del contenedor de dependencias.
         *
         * Usa el patrón Singleton con Double-Check Locking para asegurar
         * que solo se crea una instancia incluso en entornos multi-thread.
         *
         * **Flujo:**
         * 1. Primera llamada: Crea todas las dependencias y las cachea
         * 2. Llamadas subsiguientes: Retorna la misma instancia cacheada
         *
         * **Thread-safety:**
         * - @Volatile: Asegura que todos los threads vean el mismo valor
         * - synchronized: Previene que múltiples threads creen instancias duplicadas
         *
         * @param application Contexto de la aplicación para inicializar dependencias
         * @return Instancia única de AppDependencies con todas las dependencias listas
         */
        fun getInstance(application: Application): AppDependencies {
            // Si hay una instancia de test configurada, retornarla
            TEST_INSTANCE?.let { return it }

            // Si ya existe una instancia real, retornarla (fast path)
            return INSTANCE ?: synchronized(this) {
                // Double-check: verificar de nuevo dentro del bloque sincronizado
                INSTANCE ?: buildDependencies(application).also { INSTANCE = it }
            }
        }

        /**
         * Construye todas las dependencias de la aplicación.
         *
         * Este método crea las instancias reales de:
         * - Room Database
         * - SessionManager
         * - Retrofit ApiService
         * - UserRepository (que usa las anteriores)
         *
         * Solo se llama una vez en la primera llamada a getInstance().
         *
         * @param application Contexto de la aplicación
         * @return Contenedor con todas las dependencias inicializadas
         */
        private fun buildDependencies(application: Application): AppDependencies {
            // 1. Crear Database (singleton de Room)
            val database = AppDatabase.getDatabase(application)

            // 2. Crear DAOs
            val userDao = database.userDao()

            // 3. Crear SessionManager
            val sessionManager = SessionManager(application)

            // 4. Obtener ApiService de RetrofitClient
            // (RetrofitClient ya se inicializa en MainActivity.onCreate)
            val apiService = RetrofitClient.authApiService

            // 5. Crear UserRepository con todas sus dependencias
            val userRepository = UserRepository(application)

            // 6. Crear AvatarRepository para persistencia del avatar
            val avatarRepository = AvatarRepository(application)

            return AppDependencies(
                userRepository = userRepository,
                sessionManager = sessionManager,
                database = database,
                apiService = apiService,
                userDao = userDao,
                avatarRepository = avatarRepository
            )
        }

        /**
         * Configura una instancia de prueba para testing.
         *
         * Usado en tests unitarios para reemplazar las dependencias reales
         * con mocks o fakes.
         *
         * **Ejemplo:**
         * ```kotlin
         * @Before
         * fun setup() {
         *     val mockRepo = mockk<UserRepository>()
         *     AppDependencies.setTestInstance(
         *         AppDependencies(mockRepo, mockSession, mockDb, mockApi, mockDao)
         *     )
         * }
         *
         * @After
         * fun tearDown() {
         *     AppDependencies.clearTestInstance()
         * }
         * ```
         *
         * @param testInstance Instancia mock/fake para usar en tests
         */
        fun setTestInstance(testInstance: AppDependencies) {
            TEST_INSTANCE = testInstance
        }

        /**
         * Limpia la instancia de prueba.
         *
         * Debe llamarse en @After de cada test para evitar que un test
         * afecte a otro.
         */
        fun clearTestInstance() {
            TEST_INSTANCE = null
        }

        /**
         * Resetea la instancia real del contenedor.
         *
         * Útil para forzar la recreación de dependencias, por ejemplo:
         * - Después de logout (limpiar caché)
         * - En tests de integración
         * - Cambio de configuración de la app
         *
         * ⚠️ Usar con cuidado: esto invalida todas las referencias existentes
         */
        fun reset() {
            synchronized(this) {
                INSTANCE = null
            }
        }
    }
}

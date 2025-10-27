package com.example.miappmodular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.miappmodular.network.RetrofitClient
import com.example.miappmodular.ui.navigation.AppNavigation
import com.example.miappmodular.ui.theme.MiAppModularTheme

/**
 * Actividad principal y punto de entrada de la aplicación.
 *
 * Esta actividad es el contenedor raíz que aloja toda la UI de Jetpack Compose.
 * No contiene lógica de negocio ni UI específica, solo inicializa:
 * 1. El tema Material3 de la app ([MiAppModularTheme])
 * 2. El grafo de navegación ([AppNavigation])
 *
 * **Arquitectura:**
 * ```
 * MainActivity (Activity)
 *   └── MiAppModularTheme (Material3 Theme)
 *       └── Surface (Background container)
 *           └── AppNavigation (NavHost)
 *               ├── LoginScreen
 *               ├── RegisterScreen
 *               ├── HomeScreen
 *               └── ProfileScreen
 * ```
 *
 * **Configuración en AndroidManifest.xml:**
 * Esta actividad debe estar configurada como `MAIN` y `LAUNCHER`:
 * ```xml
 * <activity
 *     android:name=".MainActivity"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.MAIN" />
 *         <category android:name="android.intent.category.LAUNCHER" />
 *     </intent-filter>
 * </activity>
 * ```
 *
 * **Ciclo de vida:**
 * - `onCreate()` se llama una vez al crear la actividad
 * - `setContent {}` establece la UI Compose (reemplaza setContentView en XML)
 * - La navegación y estado se manejan mediante Compose Navigation y ViewModels
 *
 * @see MiAppModularTheme
 * @see AppNavigation
 */
class MainActivity : ComponentActivity() {

    /**
     * Punto de entrada del ciclo de vida de la actividad.
     *
     * Inicializa la UI Compose con:
     * - RetrofitClient para configuración de red
     * - Tema Material3 personalizado
     * - Surface con color de fondo del tema
     * - Sistema de navegación de la app
     *
     * @param savedInstanceState Estado guardado de la actividad (null en primera ejecución).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar RetrofitClient con el contexto de la aplicación
        // IMPORTANTE: Esto debe hacerse ANTES de setContent {} para que
        // SessionManager esté disponible cuando se creen los ViewModels
        RetrofitClient.initialize(this)

        setContent {
            MiAppModularTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

# Tutorial: Integración de Cámara y Galería para Avatar de Perfil

## 📚 Objetivo
Aprender a integrar la funcionalidad de captura de fotos y selección de imágenes desde la galería para el avatar del perfil de usuario en una aplicación Android con Jetpack Compose.

## 🎯 ¿Qué vamos a construir?
- Un avatar clickeable que permite al usuario seleccionar una imagen
- Diálogo de opciones: Cámara o Galería
- Manejo de permisos runtime (CAMERA, READ_MEDIA_IMAGES)
- Captura de fotos con la cámara del dispositivo
- Selección de imágenes desde la galería
- Visualización de la imagen seleccionada usando Coil

---

## 📋 Prerrequisitos
- Conocimientos básicos de Kotlin y Jetpack Compose
- Entender el patrón MVVM
- Familiaridad con StateFlow y estado en Compose
- Android Studio instalado

---

## 🚀 Paso 1: Configurar Permisos en AndroidManifest.xml

### ¿Qué son los permisos en Android?
Los permisos protegen la privacidad del usuario. Necesitamos solicitar permisos para acceder a funciones sensibles como la cámara y el almacenamiento.

### Agregar permisos al AndroidManifest.xml

Ubicación: `app/src/main/AndroidManifest.xml`

```xml
<!-- Permisos para cámara y almacenamiento -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<!-- Para Android 12 y anteriores -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

**💡 Explicación:**
- `CAMERA`: Permite acceder a la cámara del dispositivo
- `READ_MEDIA_IMAGES`: Permiso moderno (Android 13+) para leer imágenes
- `READ_EXTERNAL_STORAGE`: Permiso antiguo (Android 12 y anteriores), limitado por `maxSdkVersion`

---

## 🚀 Paso 2: Configurar FileProvider

### ¿Qué es un FileProvider?
Un FileProvider permite compartir archivos de forma segura entre tu app y otras aplicaciones (como la app de cámara). Es obligatorio desde Android 7.0 (API 24).

### 2.1: Crear archivo file_paths.xml

Ubicación: `app/src/main/res/xml/file_paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Ruta para imágenes capturadas con la cámara -->
    <external-files-path
        name="camera_images"
        path="Pictures/" />
</paths>
```

**💡 Explicación:**
- `external-files-path`: Usa el almacenamiento externo privado de la app
- `name="camera_images"`: Nombre lógico de la ruta
- `path="Pictures/"`: Carpeta donde se guardarán las fotos

### 2.2: Registrar FileProvider en AndroidManifest.xml

Agrega dentro del tag `<application>`:

```xml
<!-- FileProvider para captura de imágenes con cámara -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**💡 Explicación:**
- `authorities`: Identificador único usando el applicationId
- `exported="false"`: El provider no es accesible desde otras apps directamente
- `grantUriPermissions="true"`: Permite otorgar permisos temporales de URI

---

## 🚀 Paso 3: Agregar Dependencias

### ¿Qué es Coil?
Coil es una biblioteca moderna para cargar imágenes en Android. Es ligera, rápida y está optimizada para Jetpack Compose.

### Agregar Coil en build.gradle.kts

Ubicación: `app/build.gradle.kts`

```kotlin
dependencies {
    // ... otras dependencias

    // Coil - Para cargar imágenes desde URI
    implementation("io.coil-kt:coil-compose:2.5.0")
}
```

**🔄 Sincronizar proyecto:** Haz clic en "Sync Now" en Android Studio.

---

## 🚀 Paso 4: Crear el Diálogo de Selección de Imagen

### ¿Por qué un componente separado?
Separar el diálogo en su propio archivo lo hace reutilizable y más fácil de mantener.

### Crear ImagePickerDialog.kt

Ubicación: `app/src/main/java/com/example/miappmodular/ui/components/ImagePickerDialog.kt`

```kotlin
package com.example.miappmodular.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.theme.*

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Text(
                text = "Seleccionar imagen",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Foreground
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Elige cómo deseas seleccionar tu imagen:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ForegroundMuted
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opción de Cámara
                ImagePickerOption(
                    icon = Icons.Filled.CameraAlt,
                    title = "Tomar foto",
                    description = "Abre la cámara para capturar una nueva foto",
                    onClick = onCameraClick
                )

                ShadcnDivider()

                // Opción de Galería
                ImagePickerOption(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "Elegir de galería",
                    description = "Selecciona una imagen de tu dispositivo",
                    onClick = onGalleryClick
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            ShadcnButton(
                onClick = onDismiss,
                text = "Cancelar",
                variant = ButtonVariant.Outline,
                size = ButtonSize.Default
            )
        }
    )
}

@Composable
private fun ImagePickerOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    ShadcnCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ForegroundMuted
                    )
                )
            }
        }
    }
}
```

**💡 Conceptos clave:**
- **Patrón presentacional**: El componente no tiene lógica de negocio
- **Callbacks**: `onCameraClick` y `onGalleryClick` delegan las acciones al padre
- **Reutilizable**: Puede usarse en cualquier pantalla que necesite seleccionar imágenes

---

## 🚀 Paso 5: Actualizar ProfileViewModel

### ¿Por qué agregar avatarUri al estado?
El estado del ViewModel es la fuente única de verdad. Guardar el URI aquí asegura que la UI se actualice reactivamente.

### Modificar ProfileViewModel.kt

Ubicación: `app/src/main/java/com/example/miappmodular/viewmodel/ProfileViewModel.kt`

#### 5.1: Agregar import
```kotlin
import android.net.Uri
```

#### 5.2: Actualizar ProfileUiState
```kotlin
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val formattedCreatedAt: String = "",
    val avatarUri: Uri? = null  // ✨ Nuevo campo
)
```

#### 5.3: Agregar función updateAvatar
```kotlin
/**
 * Actualiza la URI del avatar del usuario.
 */
fun updateAvatar(uri: Uri?) {
    _uiState.update { it.copy(avatarUri = uri) }
}
```

**💡 Explicación:**
- `avatarUri: Uri?`: Almacena la ubicación de la imagen seleccionada
- `updateAvatar()`: Método para actualizar el avatar desde la UI
- `_uiState.update { it.copy() }`: Patrón inmutable de actualización de estado

---

## 🚀 Paso 6: Actualizar ProfileScreen

Este es el paso más complejo. Vamos a agregar toda la lógica de permisos, launchers y UI.

### Conceptos importantes antes de empezar:

#### Activity Result API
La forma moderna de obtener resultados de otras apps (cámara, galería).

```kotlin
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    // Manejar resultado
}
```

#### Accompanist Permissions
Biblioteca para manejar permisos de forma declarativa en Compose.

```kotlin
val permissionsState = rememberMultiplePermissionsState(
    listOf(Manifest.permission.CAMERA)
)
```

### 6.1: Agregar imports necesarios

Ubicación: `app/src/main/java/com/example/miappmodular/ui/screens/ProfileScreen.kt`

```kotlin
import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
```

### 6.2: Modificar ProfileContent

Reemplaza la función `ProfileContent` completa con esta versión:

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ProfileContent(
    uiState: com.example.miappmodular.viewmodel.ProfileUiState,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()
    var showImagePicker by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Definir los permisos según la versión de Android
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    val permissionsState = rememberMultiplePermissionsState(permissions)

    // Launcher para capturar foto con cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.updateAvatar(tempCameraUri)
        }
    }

    // Launcher para seleccionar imagen de galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateAvatar(it)
        }
    }

    // Mostrar el diálogo de selección de imagen
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onCameraClick = {
                showImagePicker = false
                if (permissionsState.permissions.any {
                    it.permission == Manifest.permission.CAMERA && it.hasPermission
                }) {
                    // Crear archivo temporal para la foto
                    tempCameraUri = createImageUri(context)
                    tempCameraUri?.let { takePictureLauncher.launch(it) }
                } else {
                    // Solicitar permiso de cámara
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            onGalleryClick = {
                showImagePicker = false
                val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                if (permissionsState.permissions.any {
                    it.permission == imagePermission && it.hasPermission
                }) {
                    // Lanzar selector de galería
                    pickImageLauncher.launch("image/*")
                } else {
                    // Solicitar permiso de almacenamiento
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Card
        ShadcnCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Circle con imagen o icono
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    // Avatar principal
                    if (uiState.avatarUri != null) {
                        // Mostrar imagen seleccionada con Coil
                        AsyncImage(
                            model = uiState.avatarUri,
                            contentDescription = "Avatar del usuario",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .clickable { showImagePicker = true }
                                .background(Primary),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Mostrar icono por defecto
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showImagePicker = true },
                            shape = CircleShape,
                            color = Primary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Seleccionar avatar",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(28.dp)
                            )
                        }
                    }

                    // Icono de cámara en esquina
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { showImagePicker = true },
                        shape = CircleShape,
                        color = Surface,
                        shadowElevation = 2.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = Primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Name
                Text(
                    text = uiState.user?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // User Email
                Text(
                    text = uiState.user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ForegroundMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ... resto del código (Information Card) permanece igual
    }
}
```

### 6.3: Agregar función helper para crear URI

Agregar al final del archivo `ProfileScreen.kt`:

```kotlin
/**
 * Crea un URI temporal para guardar la foto capturada por la cámara.
 */
private fun createImageUri(context: Context): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "profile_avatar_$timeStamp.jpg"
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)

    return try {
        val imageFile = File(storageDir, imageFileName)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        null
    }
}
```

**💡 Explicación:**
- Crea un nombre único usando timestamp
- Usa `FileProvider.getUriForFile()` para crear URI seguro
- Maneja errores con try-catch devolviendo null

---

## 🚀 Paso 7: Agregar Persistencia del Avatar (Nivel Básico)

### ¿Por qué persistencia?
Actualmente, cuando cierras la app, el avatar seleccionado se pierde porque solo se guarda en memoria (en el estado del ViewModel). Para una mejor experiencia de usuario, el avatar debe persistir entre sesiones.

### ¿Qué es DataStore?
DataStore es la solución moderna de Android para almacenamiento de datos key-value. Reemplaza a SharedPreferences con ventajas importantes:
- **Asíncrono**: No bloquea el hilo principal
- **Type-safe**: Usa Kotlin coroutines y Flow
- **Reactivo**: Observa cambios automáticamente
- **Robusto**: Mejor manejo de errores

### 7.1: Crear AvatarRepository

Este repositorio encapsula toda la lógica de persistencia del avatar.

Ubicación: `app/src/main/java/com/example/miappmodular/repository/AvatarRepository.kt`

```kotlin
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

// Extensión para crear el DataStore de avatar
private val Context.avatarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "avatar_preferences"
)

class AvatarRepository(private val context: Context) {

    companion object {
        // Key para almacenar el URI del avatar en DataStore
        private val AVATAR_URI_KEY = stringPreferencesKey("avatar_uri_key")
    }

    /**
     * Obtiene el URI del avatar como Flow reactivo.
     * Emite cada vez que cambia el avatar guardado.
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
     * El cambio se persiste inmediatamente.
     */
    suspend fun saveAvatarUri(uri: Uri?) {
        if (uri != null) {
            context.avatarDataStore.edit { preferences ->
                preferences[AVATAR_URI_KEY] = uri.toString()
            }
        } else {
            clearAvatarUri()
        }
    }

    /**
     * Elimina el URI del avatar de DataStore.
     */
    suspend fun clearAvatarUri() {
        context.avatarDataStore.edit { preferences ->
            preferences.remove(AVATAR_URI_KEY)
        }
    }
}
```

**💡 Conceptos clave:**
- **DataStore extension**: La extensión `by preferencesDataStore` crea un singleton
- **Flow<Uri?>**: El Flow emite el valor actual y cada cambio posterior
- **suspend functions**: Las operaciones de escritura son asíncronas

### 7.2: Registrar AvatarRepository en Dependencies.kt

Tu proyecto usa un contenedor de dependencias centralizado. Vamos a agregar el `AvatarRepository` ahí.

Ubicación: `app/src/main/java/com/example/miappmodular/Dependencies.kt`

#### Agregar import:
```kotlin
import com.example.miappmodular.repository.AvatarRepository
```

#### Agregar propiedad al constructor:
```kotlin
class AppDependencies(
    val userRepository: UserRepository,
    val sessionManager: SessionManager,
    val database: AppDatabase,
    val apiService: AuthApiService,
    val userDao: UserDao,
    val avatarRepository: AvatarRepository  // ✨ Nueva dependencia
) {
```

#### Inicializar en buildDependencies:
```kotlin
private fun buildDependencies(application: Application): AppDependencies {
    // ... código existente ...

    // 6. Crear AvatarRepository para persistencia del avatar
    val avatarRepository = AvatarRepository(application)

    return AppDependencies(
        userRepository = userRepository,
        sessionManager = sessionManager,
        database = database,
        apiService = apiService,
        userDao = userDao,
        avatarRepository = avatarRepository  // ✨ Agregar aquí
    )
}
```

**💡 Explicación:**
- **Service Locator Pattern**: El contenedor crea y gestiona una única instancia
- **Singleton compartido**: Todos los ViewModels usan la misma instancia
- **Fácil de testear**: Puedes reemplazar con mocks en tests

### 7.3: Actualizar ProfileViewModel

Ahora vamos a integrar la persistencia en el ViewModel.

Ubicación: `app/src/main/java/com/example/miappmodular/viewmodel/ProfileViewModel.kt`

#### Obtener el repositorio del contenedor:
```kotlin
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val dependencies = AppDependencies.getInstance(application)
    private val userRepository = dependencies.userRepository

    // ✨ Obtener AvatarRepository del contenedor
    private val avatarRepository = dependencies.avatarRepository

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadSavedAvatar()  // ✨ Cargar avatar guardado al iniciar
    }

    // ✨ Nueva función: Cargar avatar desde DataStore
    private fun loadSavedAvatar() {
        viewModelScope.launch {
            avatarRepository.getAvatarUri().collect { savedUri ->
                _uiState.update { it.copy(avatarUri = savedUri) }
            }
        }
    }

    // ... resto del código ...
}
```

#### Actualizar función updateAvatar:
```kotlin
fun updateAvatar(uri: Uri?) {
    viewModelScope.launch {
        avatarRepository.saveAvatarUri(uri)
        // El estado se actualiza automáticamente vía Flow en loadSavedAvatar()
    }
}
```

**💡 Flujo de actualización:**
1. Usuario selecciona imagen → `updateAvatar()` se ejecuta
2. Se guarda en DataStore → `saveAvatarUri()`
3. DataStore emite nuevo valor → Flow en `getAvatarUri()`
4. Flow se recolecta en `loadSavedAvatar()`
5. Estado se actualiza → UI se refresca automáticamente

**🎯 Ventajas de este patrón:**
- **Reactivo**: El estado siempre refleja lo guardado en DataStore
- **Automático**: No hay que actualizar el estado manualmente
- **Consistente**: Una única fuente de verdad (DataStore)

### 7.4: Probar la persistencia

**Test manual:**
1. Ejecuta la app
2. Ve a la pantalla de perfil
3. Selecciona un avatar (cámara o galería)
4. **Cierra completamente la app** (no solo minimizar)
5. Vuelve a abrir la app
6. Ve a la pantalla de perfil
7. ✅ **El avatar debe seguir ahí!**

**Flujo de datos:**
```
[Usuario selecciona imagen]
         ↓
[updateAvatar() en ViewModel]
         ↓
[saveAvatarUri() en Repository]
         ↓
[DataStore persiste en disco]
         ↓
[Flow emite nuevo valor]
         ↓
[loadSavedAvatar() recolecta]
         ↓
[Estado se actualiza]
         ↓
[UI muestra avatar]
```

---

## 🧪 Paso 8: Probar la Implementación

### Checklist de pruebas:

#### ✅ 1. Permisos
- [ ] La app solicita permiso de cámara al intentar tomar foto
- [ ] La app solicita permiso de almacenamiento al intentar abrir galería
- [ ] Los permisos persisten después de concederlos

#### ✅ 2. Cámara
- [ ] Se abre la app de cámara del dispositivo
- [ ] La foto capturada se muestra en el avatar
- [ ] El avatar es circular y se ve correctamente

#### ✅ 3. Galería
- [ ] Se abre el selector de imágenes del sistema
- [ ] La imagen seleccionada se muestra en el avatar
- [ ] Se mantiene la relación de aspecto (ContentScale.Crop)

#### ✅ 4. UI/UX
- [ ] El diálogo se cierra después de seleccionar una opción
- [ ] El icono de cámara en la esquina es visible
- [ ] Todo el avatar es clickeable
- [ ] El diseño es consistente con el tema de la app

---

## 🎓 Conceptos Clave Aprendidos

### 1. **Permisos Runtime**
En Android 6.0+, algunos permisos deben solicitarse en tiempo de ejecución. Accompanist Permissions facilita esto en Compose.

### 2. **FileProvider**
Mecanismo seguro para compartir archivos entre apps. Reemplaza el uso directo de file:// URIs que causaban excepciones de seguridad.

### 3. **Activity Result API**
API moderna para obtener resultados de otras activities. Reemplaza a `startActivityForResult()`.

### 4. **Coil para Compose**
Biblioteca de carga de imágenes optimizada para Jetpack Compose. Características:
- Carga asíncrona
- Caché automático
- Soporte para URIs, URLs, Resources, Files
- Integración nativa con coroutines

### 5. **Estado Reactivo**
El ViewModel mantiene el estado. La UI reacciona automáticamente a los cambios mediante `collectAsState()`.

### 6. **Composición**
Los launchers y estados de permisos se crean con `remember*` para sobrevivir recomposiciones pero recrearse en nueva instancia del composable.

---

## 🚀 Mejoras Opcionales (Retos)

### Reto 1: Compresión de imagen
Las fotos de cámara pueden ser muy grandes. Implementa:
- Redimensionar imagen antes de mostrarla
- Comprimir imagen para ahorrar memoria

### Reto 2: Subir a servidor
Implementa la funcionalidad para:
- Convertir imagen a Base64 o Multipart
- Subir al backend mediante Retrofit
- Actualizar URL en la base de datos

### Reto 3: Cropping
Permite al usuario recortar la imagen:
- Integrar una biblioteca de crop (ej: UCrop)
- Mostrar preview antes de confirmar

---

## 📚 Recursos Adicionales

### Documentación oficial:
- [Permisos en Android](https://developer.android.com/training/permissions/requesting)
- [FileProvider](https://developer.android.com/reference/androidx/core/content/FileProvider)
- [Activity Result API](https://developer.android.com/training/basics/intents/result)
- [Coil](https://coil-kt.github.io/coil/compose/)

### Bibliotecas usadas:
- [Accompanist Permissions](https://google.github.io/accompanist/permissions/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

## ❓ Preguntas Frecuentes

### ¿Por qué usar FileProvider en lugar de URI directo?
Por seguridad. Android 7.0+ lanza `FileUriExposedException` si compartes file:// URIs directamente.

### ¿Por qué diferentes permisos según versión de Android?
Android 13+ introdujo permisos granulares. `READ_MEDIA_IMAGES` es más específico que `READ_EXTERNAL_STORAGE`.

### ¿El avatar se guarda automáticamente?
¡Sí! Gracias a DataStore, el avatar persiste automáticamente entre sesiones de la app. Se guarda cuando lo seleccionas y se carga al abrir la app.

### ¿Puedo usar esto en otras pantallas?
¡Sí! El `ImagePickerDialog` es completamente reutilizable. Solo pásale los callbacks apropiados.

---

## 🎉 ¡Felicidades!

Has implementado con éxito:
- ✅ Manejo de permisos runtime
- ✅ Captura de fotos con cámara
- ✅ Selección de imágenes desde galería
- ✅ FileProvider para compartir archivos seguros
- ✅ Visualización de imágenes con Coil
- ✅ Patrón MVVM con estado reactivo
- ✅ **Persistencia con DataStore** (el avatar sobrevive al cierre de la app!)
- ✅ Patrón Repository para encapsular persistencia
- ✅ Service Locator Pattern con contenedor de dependencias

Ahora puedes aplicar estos conceptos en otras funcionalidades de tu app.

---

## 📝 Notas para el Profesor

### Tiempo estimado: 3-4 horas

### Dificultad: Intermedia-Avanzada

### Conceptos cubiertos:
- Permisos Android
- FileProvider
- Activity Result API
- Jetpack Compose avanzado
- MVVM pattern
- Manejo de estado reactivo
- Carga de imágenes con Coil
- **DataStore para persistencia**
- **Repository Pattern**
- **Service Locator Pattern**

### Evaluación sugerida:
1. Implementación funcional (30%)
2. Manejo correcto de permisos (15%)
3. Persistencia con DataStore (20%)
4. UI/UX (15%)
5. Código limpio y documentado (20%)

---

**¿Preguntas? Consulta con tu profesor o revisa la documentación oficial de Android.**
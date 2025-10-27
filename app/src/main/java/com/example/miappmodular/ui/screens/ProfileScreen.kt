package com.example.miappmodular.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.viewmodel.ProfileViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de perfil del usuario autenticado (Smart Component).
 *
 * **Patrón Smart/Dumb Components:**
 * - **Smart (este)**: Maneja ProfileViewModel y estado reactivo
 * - **Dumb ([ProfileScreenContent])**: Solo UI pura con manejo de estados
 *
 * **Funcionalidad:**
 * - Muestra información completa del usuario autenticado
 * - Carga automática de datos en el init del ViewModel
 * - Botón de refresh para recargar perfil
 * - Navegación de regreso a pantalla anterior (HomeScreen)
 * - Manejo de 3 estados: Loading, Error, Success
 * - Diseño shadcn.io con avatar circular y cards
 *
 * **Datos mostrados:**
 * - Avatar: Icono Person en círculo Primary
 * - Nombre completo del usuario
 * - Email del usuario
 * - Fecha de registro (formato español: "enero 2024")
 * - Último acceso (formato: "dd/MM/yyyy HH:mm")
 *
 * **Estados manejados:**
 * - **Loading**: Muestra CircularProgressIndicator centrado
 * - **Error**: Card con mensaje de error y botón "Reintentar"
 * - **Success**: Muestra ProfileContent con datos del usuario
 *
 * **Inicialización automática:**
 * El ProfileViewModel carga los datos automáticamente en su bloque init,
 * por lo que no es necesario llamar manualmente a ningún método de carga.
 *
 * **Navegación:**
 * - Botón Atrás (TopBar) → Regresa a HomeScreen
 * - Botón Refresh (TopBar) → Recarga datos del perfil desde Room
 *
 * @param viewModel ViewModel que maneja la carga y estado del perfil (inyectado automáticamente).
 * @param onNavigateBack Callback para regresar a la pantalla anterior (típicamente HomeScreen).
 *
 * @see ProfileViewModel
 * @see ProfileScreenContent
 * @see com.example.miappmodular.repository.UserRepository.getCurrentUser
 * @see com.example.miappmodular.ui.navigation.AppNavigation
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh
    )
}

/**
 * ProfileScreenContent (Presentational/Dumb Component).
 *
 * Componente de UI pura que renderiza la pantalla de perfil con manejo de estados.
 * No tiene dependencias del ViewModel, haciéndolo seguro para @Preview.
 *
 * **Arquitectura de UI:**
 * ```
 * Scaffold
 * ├── TopAppBar
 * │   ├── NavigationIcon: ArrowBack
 * │   ├── Title: "Perfil"
 * │   └── Actions: Refresh icon
 * └── Box (centered)
 *     ├── when (uiState.isLoading) → CircularProgressIndicator
 *     ├── when (uiState.error != null) → ErrorState card
 *     └── when (uiState.user != null) → ProfileContent
 * ```
 *
 * **Manejo de estados:**
 * - **isLoading = true**: Muestra CircularProgressIndicator centrado
 * - **error != null**: Muestra ErrorState con mensaje y botón "Reintentar"
 * - **user != null**: Muestra ProfileContent con avatar e información completa
 *
 * **Diseño shadcn.io:**
 * - TopAppBar: Material3 con Surface background
 * - Background: BackgroundSecondary para contraste
 * - Componentes: ErrorState y ProfileContent como subcomponentes
 *
 * **TopAppBar actions:**
 * - ArrowBack (left): Ejecuta onNavigateBack
 * - Refresh (right): Ejecuta onRefresh para recargar datos
 *
 * @param uiState Estado inmutable con datos del perfil, loading y errores.
 * @param onNavigateBack Callback para navegar hacia atrás.
 * @param onRefresh Callback para recargar los datos del perfil.
 *
 * @see ProfileUiState
 * @see ErrorState
 * @see ProfileContent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    uiState: com.example.miappmodular.viewmodel.ProfileUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Foreground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Foreground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                            tint = Foreground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = Primary
                    )
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error,
                        onRetry = onRefresh
                    )
                }
                uiState.user != null -> {
                    ProfileContent(
                        uiState = uiState,
                        onRefresh = onRefresh
                    )
                }
            }
        }
    }
}

/**
 * Componente de estado de error reutilizable con diseño shadcn.io.
 *
 * Muestra un mensaje de error amigable al usuario con opción de reintentar
 * la operación fallida. Diseñado para ser usado cuando la carga de datos
 * del perfil falla (ej: usuario no encontrado, error de base de datos).
 *
 * **Diseño:**
 * - Card centrado con padding 24.dp
 * - Icono Error grande (64.dp) en color Destructive (rojo)
 * - Título "Error" en headlineSmall Bold
 * - Mensaje de error descriptivo en bodyMedium Muted
 * - Botón "Reintentar" en ButtonVariant.Default (Primary)
 *
 * **Casos de uso:**
 * - Error al cargar perfil desde Room (usuario no encontrado)
 * - Error de base de datos (SQLite exception)
 * - Timeout en operaciones de lectura
 * - Cualquier error recuperable mediante retry
 *
 * **UX:**
 * El mensaje de error debe ser descriptivo y en español para que el usuario
 * entienda qué salió mal (ej: "No se encontró el usuario", "Error al cargar el perfil").
 *
 * @param error Mensaje de error descriptivo a mostrar al usuario.
 * @param onRetry Callback ejecutado al presionar el botón "Reintentar".
 *
 * @see ShadcnCard
 * @see ShadcnButton
 * @see ProfileScreenContent
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        ShadcnCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = Destructive,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ForegroundMuted
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                ShadcnButton(
                    onClick = onRetry,
                    text = "Reintentar",
                    variant = ButtonVariant.Default,
                    size = ButtonSize.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Componente de contenido principal del perfil con diseño shadcn.io.
 *
 * Renderiza la información completa del usuario cuando la carga es exitosa.
 * Incluye funcionalidad de selección de avatar desde cámara o galería.
 *
 * **Funcionalidad de avatar:**
 * - Avatar clickeable que abre diálogo de selección
 * - Opción de tomar foto con cámara (requiere permiso CAMERA)
 * - Opción de seleccionar de galería (requiere permiso READ_MEDIA_IMAGES)
 * - Muestra imagen seleccionada usando Coil o icono por defecto
 * - Icono de cámara en esquina inferior derecha del avatar
 *
 * **Manejo de permisos:**
 * - Solicita permisos antes de abrir cámara o galería
 * - Muestra Snackbar si los permisos son denegados
 * - Compatible con permisos de Android 13+ y versiones anteriores
 *
 * **Activity Result Contracts:**
 * - TakePicture: Captura foto usando la cámara
 * - GetContent: Selecciona imagen de galería
 * - FileProvider: Crea URIs seguros para archivos de cámara
 *
 * @param uiState Estado con User completo, formattedCreatedAt y avatarUri.
 * @param onRefresh Callback para recargar datos (no usado actualmente).
 *
 * @see ProfileItem
 * @see ImagePickerDialog
 * @see com.example.miappmodular.viewmodel.ProfileViewModel.updateAvatar
 */
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
    val snackbarHostState = remember { SnackbarHostState() }

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
                if (permissionsState.permissions.any { it.permission == Manifest.permission.CAMERA && it.status.isGranted }) {
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

                if (permissionsState.permissions.any { it.permission == imagePermission && it.status.isGranted }) {
                    // Lanzar selector de galería
                    pickImageLauncher.launch("image/*")
                } else {
                    // Solicitar permiso de almacenamiento
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

            // User Information Card
            ShadcnCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Información",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Foreground
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileItem(
                        icon = Icons.Filled.Person,
                        label = "Nombre completo",
                        value = uiState.user?.name ?: ""
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    ShadcnDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileItem(
                        icon = Icons.Filled.Email,
                        label = "Correo electrónico",
                        value = uiState.user?.email ?: ""
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    ShadcnDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileItem(
                        icon = Icons.Filled.CalendarToday,
                        label = "Miembro desde",
                        value = uiState.formattedCreatedAt
                    )

                    uiState.user?.lastLogin?.let { lastLogin ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ShadcnDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        ProfileItem(
                            icon = Icons.Filled.AccessTime,
                            label = "Último acceso",
                            value = SimpleDateFormat(
                                "dd/MM/yyyy HH:mm",
                                Locale("es", "ES")
                            ).format(lastLogin)
                        )
                    }
                }
            }
        }

        // Snackbar para mensajes de permisos
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Crea un URI temporal para guardar la foto capturada por la cámara.
 *
 * Utiliza FileProvider para crear un URI seguro que puede ser compartido
 * con la aplicación de cámara. El archivo se crea en el directorio Pictures
 * del almacenamiento externo de la aplicación.
 *
 * **Patrón de nombres:**
 * - Formato: "profile_avatar_YYYYMMDD_HHmmss.jpg"
 * - Ejemplo: "profile_avatar_20240115_143025.jpg"
 *
 * **Ubicación:**
 * - context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
 * - No requiere permisos de almacenamiento en Android 10+
 *
 * @param context Contexto de la aplicación
 * @return Uri del archivo temporal, o null si hay error
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

/**
 * Item de información de perfil reutilizable con diseño shadcn.io.
 *
 * Componente presentacional que muestra un campo de información del usuario
 * con un icono, etiqueta y valor. Usado para crear listas de información
 * estructuradas y consistentes.
 *
 * **Diseño:**
 * - Layout horizontal: Icono (left) + Column (label + value)
 * - Icono: 20.dp, color Primary
 * - Label: labelMedium, ForegroundMuted, Medium weight (etiqueta descriptiva)
 * - Value: bodyMedium, Foreground, Normal weight (valor destacado)
 * - Spacing: 12.dp horizontal, 4.dp vertical entre label y value
 *
 * **Patrón de diseño:**
 * Este componente implementa el patrón "Label-Value Pair" común en perfiles
 * y formularios de solo lectura. El icono proporciona contexto visual rápido.
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * ProfileItem(
 *     icon = Icons.Filled.Email,
 *     label = "Correo electrónico",
 *     value = "user@example.com"
 * )
 * ```
 *
 * **Casos de uso comunes:**
 * - Información de perfil (nombre, email, teléfono)
 * - Fechas (registro, último acceso, cumpleaños)
 * - Configuraciones (idioma, zona horaria, preferencias)
 * - Cualquier par label-value que necesite representación consistente
 *
 * **Accesibilidad:**
 * El icono tiene contentDescription = null porque es decorativo. La información
 * semántica está en el label y value que sí son leídos por lectores de pantalla.
 *
 * @param icon Icono Material que representa el tipo de información.
 * @param label Etiqueta descriptiva del campo (ej: "Nombre completo").
 * @param value Valor del campo a mostrar (ej: "Juan Pérez").
 *
 * @see ProfileContent
 */
@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ForegroundMuted,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Foreground,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

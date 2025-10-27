package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.viewmodel.*

/**
 * Pantalla de registro de nuevos usuarios (Smart Component).
 *
 * **Patrón Smart/Dumb Components:**
 * - **Smart (este)**: Maneja ViewModel, estado y navegación
 * - **Dumb ([RegisterScreenContent])**: Solo UI pura, apta para @Preview
 *
 * **Funcionalidad:**
 * - Registro con nombre completo, email y contraseña
 * - Confirmación de contraseña con validación de coincidencia
 * - Validación en tiempo real mientras se escribe
 * - Navegación automática al Home tras registro exitoso
 * - Navegación de regreso a pantalla de login
 * - Diseño shadcn.io con componentes reutilizables y scrollable
 *
 * **Validaciones implementadas:**
 * - Nombre: Mínimo 3 caracteres
 * - Email: Formato RFC 5322 válido
 * - Contraseña: 8 caracteres mínimo, mayúscula, minúscula y número
 * - Confirmación: Debe coincidir exactamente con la contraseña
 *
 * **Navegación:**
 * Usa [LaunchedEffect] para observar [RegisterUiState.isSuccess] y
 * navegar automáticamente cuando el registro es exitoso. El backstack
 * se limpia para prevenir volver a pantallas de autenticación.
 *
 * **Flujo de registro:**
 * ```
 * 1. Usuario completa formulario (name, email, password, confirmPassword)
 * 2. Validación en tiempo real en cada campo
 * 3. Usuario presiona "Registrarse"
 * 4. Validación completa + verificación de passwords coincidentes
 * 5. API POST /auth/signup
 * 6. Guarda User en Room + Session en DataStore
 * 7. Navega a Home (backstack limpiado)
 * ```
 *
 * @param viewModel ViewModel con lógica de registro (inyectado automáticamente).
 * @param onNavigateBack Callback para regresar a pantalla de login.
 * @param onRegisterSuccess Callback ejecutado tras registro exitoso (navega a Home).
 *
 * @see RegisterViewModel
 * @see RegisterScreenContent
 * @see com.example.miappmodular.ui.navigation.AppNavigation
 * @see com.example.miappmodular.repository.UserRepository.registerUser
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navegar cuando el registro sea exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = { viewModel.register() },
        onNavigateBack = onNavigateBack,
        onCleanError = viewModel::clearErrors
    )
}


/**
 * RegisterScreenContent (Presentational/Dumb Component).
 *
 * Componente de UI pura sin dependencias del ViewModel. Renderiza la interfaz
 * de registro con 4 campos de formulario y manejo de errores mediante Snackbar.
 *
 * **Características de UI:**
 * - Card principal con icono PersonAdd y título
 * - 4 campos ShadcnInput: nombre, email, password, confirmPassword
 * - Validación visual con mensajes de error bajo cada campo
 * - Botón de registro con estado loading
 * - Botón secundario "Ya tengo cuenta" para volver al login
 * - Snackbar para errores generales (ej: error de API)
 * - Scrollable verticalmente para pantallas pequeñas
 *
 * **Diseño shadcn.io:**
 * - Paleta: Primary, ForegroundMuted, BackgroundSecondary
 * - Componentes: ShadcnInput, ShadcnButton, ShadcnCard, ShadcnSnackbar
 * - Variantes: ButtonVariant.Default y ButtonVariant.Outline
 * - Elevación: 1.dp en card principal
 *
 * **Estados manejados:**
 * - Normal: Todos los campos vacíos y habilitados
 * - Con errores: Bordes rojos y mensajes de error visibles
 * - Loading: Botón deshabilitado con CircularProgressIndicator
 * - Error general: Snackbar rojo con duración corta
 *
 * **IME Actions:**
 * - Nombre → Next (salta a email)
 * - Email → Next (salta a password)
 * - Password → Next (salta a confirmPassword)
 * - ConfirmPassword → Done (ejecuta onRegisterClick)
 *
 * @param uiState Estado inmutable con datos y errores del formulario.
 * @param onNameChange Callback invocado al cambiar el nombre.
 * @param onEmailChange Callback invocado al cambiar el email.
 * @param onPasswordChange Callback invocado al cambiar la contraseña.
 * @param onConfirmPasswordChange Callback invocado al cambiar la confirmación.
 * @param onRegisterClick Callback para iniciar el proceso de registro.
 * @param onNavigateBack Callback para volver a la pantalla de login.
 * @param onCleanError Callback para limpiar el error general tras mostrarlo.
 *
 * @see RegisterUiState
 * @see com.example.miappmodular.ui.components.ShadcnInput
 * @see com.example.miappmodular.ui.components.ShadcnButton
 */
@Composable
fun RegisterScreenContent(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onCleanError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de error
    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            onCleanError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                ShadcnSnackbar(
                    message = data.visuals.message,
                    variant = SnackbarVariant.Destructive
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/Header Card
                ShadcnCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icono de la app
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = Primary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Título
                        Text(
                            text = "Crear Cuenta",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Foreground
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtítulo
                        Text(
                            text = "Completa el formulario para registrarte",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ForegroundMuted
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Campo de Nombre
                        ShadcnInput(
                            value = uiState.name,
                            onValueChange = onNameChange,
                            label = "Nombre completo",
                            placeholder = "Juan Pérez",
                            error = uiState.nameError,
                            leadingIcon = Icons.Filled.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Email
                        ShadcnInput(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            label = "Email",
                            placeholder = "tu@email.com",
                            error = uiState.emailError,
                            leadingIcon = Icons.Filled.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Contraseña
                        ShadcnInput(
                            value = uiState.password,
                            onValueChange = onPasswordChange,
                            label = "Contraseña",
                            placeholder = "••••••••",
                            error = uiState.passwordError,
                            leadingIcon = Icons.Filled.Lock,
                            isPassword = true,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Confirmar Contraseña
                        ShadcnInput(
                            value = uiState.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = "Confirmar contraseña",
                            placeholder = "••••••••",
                            error = uiState.confirmPasswordError,
                            leadingIcon = Icons.Filled.Lock,
                            isPassword = true,
                            imeAction = ImeAction.Done,
                            onImeAction = onRegisterClick,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de Registro
                        ShadcnButton(
                            onClick = onRegisterClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            loading = uiState.isLoading,
                            variant = ButtonVariant.Default,
                            size = ButtonSize.Default,
                            text = "Registrarse"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider con texto
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShadcnDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "O",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ForegroundMuted
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            ShadcnDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para volver al Login
                        ShadcnButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Default,
                            text = "Ya tengo cuenta"
                        )
                    }
                }
            }
        }
    }
}

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
 * RegisterScreen (Smart Component)
 *
 * Componente inteligente que maneja el ViewModel y la lógica de negocio.
 * Para previews, pasa viewModel = null.
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
 * RegisterScreenContent (Presentational/Dumb Component)
 *
 * Componente de UI pura para la pantalla de registro.
 * No tiene dependencias del ViewModel, por lo que es seguro para usar en @Preview.
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

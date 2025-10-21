package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.viewmodel.LoginViewModel
import com.example.miappmodular.viewmodel.LoginUiState

/**
 * Pantalla de Login con diseño shadcn.io (Smart Component)
 *
 * Este es el componente "smart" que maneja el ViewModel y la lógica de negocio.
 * Para previews, pasa directamente a LoginScreenContent.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navegar al home cuando el login sea exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = { viewModel.login(uiState.email, uiState.password, false) },
        onRegisterClick = onNavigateToRegister,
        onClearErrors = viewModel::clearErrors
    )
}

/**
 * LoginScreenContent (Presentational/Dumb Component)
 *
 * Este es el componente "dumb" que solo maneja la UI.
 * No tiene dependencias del ViewModel, por lo que es seguro para usar en @Preview.
 */
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onClearErrors: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de error
    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            onClearErrors()
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/Header
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
                                imageVector = Icons.Filled.Lock,
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
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Foreground
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtítulo
                        Text(
                            text = "Ingresa tus credenciales para continuar",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ForegroundMuted
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

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
                            imeAction = ImeAction.Done,
                            onImeAction = onLoginClick,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de Login
                        ShadcnButton(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            loading = uiState.isLoading,
                            variant = ButtonVariant.Default,
                            size = ButtonSize.Default,
                            text = "Iniciar Sesión"
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

                        // Botón de Registro
                        ShadcnButton(
                            onClick = onRegisterClick,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Default,
                            text = "Crear Cuenta"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer - Credenciales de prueba
                ShadcnCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Info,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Credenciales de prueba",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Foreground
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Muted, MaterialTheme.shapes.small)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row {
                                Text(
                                    text = "Email: ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = ForegroundMuted
                                    )
                                )
                                Text(
                                    text = "test@test.com",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Normal,
                                        color = Foreground
                                    )
                                )
                            }
                            Row {
                                Text(
                                    text = "Password: ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = ForegroundMuted
                                    )
                                )
                                Text(
                                    text = "Test1234",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Normal,
                                        color = Foreground
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login Screen"
)
@Composable
fun LoginScreenPreview() {
    MiAppModularTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onClearErrors = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login Screen - With Data"
)
@Composable
fun LoginScreenPreviewWithData() {
    MiAppModularTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "test@test.com",
                password = "Test1234",
                emailError = null,
                passwordError = null,
                generalError = null,
                isLoading = false,
                isSuccess = false
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onClearErrors = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login Screen - Loading State"
)
@Composable
fun LoginScreenPreviewLoading() {
    MiAppModularTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "test@test.com",
                password = "Test1234",
                emailError = null,
                passwordError = null,
                generalError = null,
                isLoading = true,
                isSuccess = false
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onClearErrors = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login Screen - With Errors"
)
@Composable
fun LoginScreenPreviewWithErrors() {
    MiAppModularTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "invalid-email",
                password = "123",
                emailError = "Email inválido",
                passwordError = "La contraseña debe tener al menos 8 caracteres",
                generalError = null,
                isLoading = false,
                isSuccess = false
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onClearErrors = {}
        )
    }
}

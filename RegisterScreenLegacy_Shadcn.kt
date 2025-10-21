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
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.viewmodel.RegisterViewModel

/**
 * RegisterScreen - Versión Legacy con estilo shadcn.io
 *
 * Esta versión usa estado local (remember) en lugar de ViewModel
 * Diseñada para propósitos educativos/demostración
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenLegacy(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    // Estado local (sin ViewModel)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Validaciones locales
    fun validateName(): Boolean {
        return when {
            name.isBlank() -> {
                nameError = "El nombre es obligatorio"
                false
            }
            name.length < 3 -> {
                nameError = "Mínimo 3 caracteres"
                false
            }
            else -> {
                nameError = null
                true
            }
        }
    }

    fun validateEmail(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return when {
            email.isBlank() -> {
                emailError = "El email es obligatorio"
                false
            }
            !email.matches(emailRegex.toRegex()) -> {
                emailError = "Formato de email inválido"
                false
            }
            else -> {
                emailError = null
                true
            }
        }
    }

    fun validatePasswords(): Boolean {
        val isPasswordValid = when {
            password.isBlank() -> {
                passwordError = "La contraseña es obligatoria"
                false
            }
            password.length < 8 -> {
                passwordError = "Mínimo 8 caracteres"
                false
            }
            else -> {
                passwordError = null
                true
            }
        }

        val isConfirmValid = when {
            confirmPassword != password -> {
                confirmPasswordError = "Las contraseñas no coinciden"
                false
            }
            else -> {
                confirmPasswordError = null
                true
            }
        }

        return isPasswordValid && isConfirmValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Foreground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card principal con formulario
                ShadcnCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ícono de la app
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

                        // Campo de Nombre con ShadcnInput
                        ShadcnInput(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) validateName()
                            },
                            label = "Nombre completo",
                            placeholder = "Juan Pérez",
                            error = nameError,
                            leadingIcon = Icons.Filled.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Email con ShadcnInput
                        ShadcnInput(
                            value = email,
                            onValueChange = {
                                email = it
                                if (emailError != null) validateEmail()
                            },
                            label = "Email",
                            placeholder = "tu@email.com",
                            error = emailError,
                            leadingIcon = Icons.Filled.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Contraseña con ShadcnInput
                        ShadcnInput(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError != null) validatePasswords()
                            },
                            label = "Contraseña",
                            placeholder = "••••••••",
                            error = passwordError,
                            leadingIcon = Icons.Filled.Lock,
                            isPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Confirmar Contraseña con ShadcnInput
                        ShadcnInput(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                if (confirmPasswordError != null) validatePasswords()
                            },
                            label = "Confirmar contraseña",
                            placeholder = "••••••••",
                            error = confirmPasswordError,
                            leadingIcon = Icons.Filled.Lock,
                            isPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de Registro con ShadcnButton
                        ShadcnButton(
                            onClick = {
                                if (validateName() && validateEmail() && validatePasswords()) {
                                    isLoading = true
                                    showSuccessDialog = true
                                }
                            },
                            enabled = !isLoading,
                            loading = isLoading,
                            text = "Registrarse",
                            variant = ButtonVariant.Default,
                            size = ButtonSize.Default,
                            modifier = Modifier.fillMaxWidth()
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

        // Dialog de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = com.example.miappmodular.ui.theme.Success,
                        modifier = Modifier.size(64.dp)
                    )
                },
                title = {
                    Text(
                        text = "¡Registro exitoso!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Foreground
                        )
                    )
                },
                text = {
                    Text(
                        text = "Tu cuenta ha sido creada correctamente",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ForegroundMuted
                        )
                    )
                },
                confirmButton = {
                    ShadcnButton(
                        onClick = {
                            showSuccessDialog = false
                            onRegisterSuccess()
                        },
                        text = "Continuar",
                        variant = ButtonVariant.Default,
                        size = ButtonSize.Default
                    )
                }
            )
        }
    }
}

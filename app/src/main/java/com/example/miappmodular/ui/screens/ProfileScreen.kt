package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.viewmodel.ProfileViewModel

/**
 * ProfileScreen (Smart Component)
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
 * ProfileScreenContent (Presentational Component)
 * Diseño basado en shadcn.io design system
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
 * Error State Component
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
 * Profile Content Component
 */
@Composable
private fun ProfileContent(
    uiState: com.example.miappmodular.viewmodel.ProfileUiState,
    onRefresh: () -> Unit
) {
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
                // Avatar Circle
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp)
                    )
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
                        value = java.text.SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            java.util.Locale("es", "ES")
                        ).format(lastLogin)
                    )
                }
            }
        }
    }
}

/**
 * Profile Item Component (shadcn.io styled)
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

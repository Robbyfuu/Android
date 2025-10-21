package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*

/**
 * Pantalla Home con diseño shadcn.io
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            // TopBar estilo shadcn.io
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Surface,
                shadowElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo/Título
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = MaterialTheme.shapes.small,
                                color = Primary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apps,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mi App Modular",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Foreground
                                    )
                                )
                                Text(
                                    text = "Dashboard",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ForegroundMuted
                                    )
                                )
                            }
                        }

                        // Acciones
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Perfil",
                                    tint = ForegroundMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Logout,
                                    contentDescription = "Cerrar sesión",
                                    tint = ForegroundMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    ShadcnDivider()
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header con estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Usuarios",
                        value = "1,234",
                        icon = Icons.Filled.People,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Activos Hoy",
                        value = "89",
                        icon = Icons.Filled.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título de sección
                Text(
                    text = "Módulos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )

                Text(
                    text = "Accede a las diferentes funcionalidades",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ForegroundMuted
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid de módulos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.Map,
                            title = "Mapa GPS",
                            description = "Ubicación y navegación",
                            badge = "IL 2.4",
                            onClick = onNavigateToMap
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.CameraAlt,
                            title = "Cámara",
                            description = "Captura de fotos",
                            badge = "IL 2.4",
                            onClick = onNavigateToCamera
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.Storage,
                            title = "Base de Datos",
                            description = "Persistencia local",
                            badge = "IL 2.3",
                            onClick = { }
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.Settings,
                            title = "Configuración",
                            description = "Preferencias de la app",
                            badge = "IL 2.3",
                            onClick = { }
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.Palette,
                            title = "Temas",
                            description = "Personalización visual",
                            badge = "IL 2.1",
                            onClick = { }
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.Notifications,
                            title = "Notificaciones",
                            description = "Alertas y mensajes",
                            badge = "IL 2.2",
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card de estadística estilo shadcn.io
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ShadcnCard(
        modifier = modifier,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ForegroundMuted,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = Muted
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ForegroundMuted,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
        }
    }
}

/**
 * Card de módulo/feature estilo shadcn.io
 */
@Composable
fun FeatureModuleCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String,
    onClick: () -> Unit
) {
    ShadcnCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onClick,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Muted
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    ShadcnBadge(
                        text = badge,
                        variant = BadgeVariant.Default
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
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

            // Footer del card
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Abrir",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
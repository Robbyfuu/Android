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

/**
 * Diálogo de selección de imagen con opciones de Cámara y Galería.
 *
 * **Funcionalidad:**
 * - Muestra un AlertDialog con dos opciones: Cámara y Galería
 * - Cada opción incluye un icono representativo y texto descriptivo
 * - Botón de cancelar para cerrar el diálogo
 * - Diseño consistente con shadcn.io theme
 *
 * **Casos de uso:**
 * - Selección de avatar en perfil de usuario
 * - Subida de imágenes en formularios
 * - Cualquier flujo que requiera captura o selección de imágenes
 *
 * **Patrón de diseño:**
 * Usa callbacks para delegar las acciones al componente padre,
 * manteniendo este componente sin lógica de negocio (presentational).
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * var showDialog by remember { mutableStateOf(false) }
 *
 * if (showDialog) {
 *     ImagePickerDialog(
 *         onDismiss = { showDialog = false },
 *         onCameraClick = {
 *             showDialog = false
 *             launchCamera()
 *         },
 *         onGalleryClick = {
 *             showDialog = false
 *             launchGallery()
 *         }
 *     )
 * }
 * ```
 *
 * @param onDismiss Callback ejecutado al cancelar o cerrar el diálogo
 * @param onCameraClick Callback ejecutado al seleccionar la opción de Cámara
 * @param onGalleryClick Callback ejecutado al seleccionar la opción de Galería
 *
 * @see ProfileScreen
 */
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

/**
 * Opción individual del selector de imagen.
 *
 * Componente interno reutilizable que representa una opción seleccionable
 * dentro del ImagePickerDialog. Muestra un icono, título y descripción.
 *
 * **Diseño:**
 * - Card clickeable con efecto de elevación al hover
 * - Icono grande (48.dp) en color Primary
 * - Título en titleMedium Bold
 * - Descripción en bodySmall Muted
 * - Layout horizontal con espaciado consistente
 *
 * **Accesibilidad:**
 * El Card es clickeable y responde a gestos táctiles,
 * proporcionando feedback visual al usuario.
 *
 * @param icon Icono Material que representa la opción
 * @param title Título de la opción (ej: "Tomar foto")
 * @param description Descripción breve de la acción
 * @param onClick Callback ejecutado al hacer clic en la opción
 */
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
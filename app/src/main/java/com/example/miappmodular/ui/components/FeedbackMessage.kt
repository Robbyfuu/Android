package com.example.miappmodular.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tipos de feedback visual para el usuario
 */
enum class FeedbackType {
    SUCCESS, ERROR, WARNING, INFO
}

/**
 * Diálogo de feedback con iconografía y colores semánticos
 */
@Composable
fun FeedbackDialog(
    type: FeedbackType,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmButton: (@Composable () -> Unit)? = null
) {
    val (icon, color) = when (type) {
        FeedbackType.SUCCESS -> Icons.Filled.CheckCircle to Color(0xFF4CAF50)
        FeedbackType.ERROR -> Icons.Filled.Error to MaterialTheme.colorScheme.error
        FeedbackType.WARNING -> Icons.Filled.Warning to Color(0xFFFF9800)
        FeedbackType.INFO -> Icons.Filled.Info to MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = confirmButton ?: {
            TextButton(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}

/**
 * Snackbar con colores semánticos para feedback rápido
 */
@Composable
fun FeedbackSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = when {
                    data.visuals.message.startsWith("✓") -> Color(0xFF4CAF50)
                    data.visuals.message.startsWith("✗") -> MaterialTheme.colorScheme.error
                    data.visuals.message.startsWith("⚠") -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.inverseSurface
                },
                contentColor = Color.White
            )
        }
    )
}
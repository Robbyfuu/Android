package com.example.miappmodular.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning

sealed class SnackbarVariant(
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color,
    val iconColor: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Default : SnackbarVariant(
        backgroundColor = Surface,
        textColor = Foreground,
        iconColor = ForegroundMuted,
        icon = Icons.Filled.Info
    )

    object Success : SnackbarVariant(
        backgroundColor = SuccessLight,
        textColor = androidx.compose.ui.graphics.Color(0xFF166534), // green-800
        iconColor = com.example.miappmodular.ui.theme.Success,
        icon = Icons.Filled.CheckCircle
    )

    object Destructive : SnackbarVariant(
        backgroundColor = DestructiveLight,
        textColor = androidx.compose.ui.graphics.Color(0xFF991B1B), // red-800
        iconColor = com.example.miappmodular.ui.theme.Destructive,
        icon = Icons.Filled.Error
    )

    object Warning : SnackbarVariant(
        backgroundColor = WarningLight,
        textColor = androidx.compose.ui.graphics.Color(0xFF92400E), // amber-800
        iconColor = com.example.miappmodular.ui.theme.Warning,
        icon = Icons.Filled.Warning
    )
}
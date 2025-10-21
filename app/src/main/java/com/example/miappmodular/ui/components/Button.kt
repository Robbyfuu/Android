package com.example.miappmodular.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.theme.*

/**
 * Botón estilo shadcn.io con texto simple
 * Variantes: default, outline, ghost, destructive
 */
@Composable
fun ShadcnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Default,
    size: ButtonSize = ButtonSize.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> Muted
            loading -> variant.backgroundColor
            isPressed -> variant.pressedColor
            else -> variant.backgroundColor
        },
        animationSpec = tween(150)
    )

    val contentColor = when {
        !enabled -> MutedForeground
        else -> variant.contentColor
    }

    val borderColor = if (variant == ButtonVariant.Outline) {
        if (enabled) Border else Muted
    } else Color.Transparent

    Box(
        modifier = modifier
            .height(size.height)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .then(
                if (variant == ButtonVariant.Outline) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(6.dp))
                } else Modifier
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cargando...",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}
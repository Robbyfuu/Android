package com.example.miappmodular.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miappmodular.ui.theme.*

/**
 * Textarea estilo shadcn.io
 */
@Composable
fun ShadcnTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 6
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> Destructive
            isFocused -> Ring
            else -> Border
        },
        animationSpec = tween(150)
    )

    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (error != null) Destructive else Foreground
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Textarea Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (enabled) Surface else Muted)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                textStyle = TextStyle(
                    color = if (enabled) Foreground else MutedForeground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                ),
                minLines = minLines,
                maxLines = maxLines,
                cursorBrush = SolidColor(Ring),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                color = ForegroundSubtle,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Helper Text o Error
        AnimatedVisibility(
            visible = error != null || helperText != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: helperText ?: "",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (error != null) Destructive else ForegroundMuted
                ),
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
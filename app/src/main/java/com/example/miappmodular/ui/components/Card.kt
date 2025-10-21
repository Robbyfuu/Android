package com.example.miappmodular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.theme.Border
import com.example.miappmodular.ui.theme.Surface

/**
 * Card estilo shadcn.io
 */
@Composable
fun ShadcnCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = RoundedCornerShape(8.dp),
            clip = false
        )
        .clip(RoundedCornerShape(8.dp))
        .background(Surface)
        .border(
            width = 1.dp,
            color = Border,
            shape = RoundedCornerShape(8.dp)
        )

    if (onClick != null) {
        Column(
            modifier = cardModifier.clickable(onClick = onClick),
            content = content
        )
    } else {
        Column(
            modifier = cardModifier,
            content = content
        )
    }
}
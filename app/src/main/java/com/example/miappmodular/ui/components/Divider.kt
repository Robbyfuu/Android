package com.example.miappmodular.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.theme.Border

/**
 * Divider estilo shadcn.io
 */
@Composable
fun ShadcnDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = Border
    )
}
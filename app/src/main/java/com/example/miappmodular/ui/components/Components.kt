/**
 * Módulo de Componentes UI - Estilo shadcn.io
 *
 * Este archivo sirve como punto de entrada centralizado para todos los componentes UI.
 * Similar a un módulo de Angular, permite importar todos los componentes desde un solo lugar.
 *
 * ## Uso:
 * ```kotlin
 * import com.example.miappmodular.ui.components.*
 * import com.example.miappmodular.ui.theme.*
 *
 * @Composable
 * fun MyScreen() {
 *     ShadcnButton(onClick = {}) { Text("Click me") }
 *     ShadcnInput(value = "", onValueChange = {})
 *     ShadcnCard { /* content */ }
 * }
 * ```
 *
 * ## Componentes disponibles:
 *
 * ### Formularios:
 * - `ShadcnButton` - Botón con variantes (Default, Outline, Ghost, Destructive, Secondary)
 * - `ShadcnInput` - Campo de texto con validación y estilos
 * - `ShadcnTextarea` - Área de texto multilínea
 *
 * ### Layout:
 * - `ShadcnCard` - Tarjeta con borde y sombra
 * - `ShadcnDivider` - Separador horizontal
 *
 * ### Feedback:
 * - `ShadcnBadge` - Badge/etiqueta con variantes (Default, Success, Destructive, Warning)
 * - `ShadcnSnackbar` - Notificación toast con variantes
 *
 * ### Variantes (desde ui.theme):
 * - `ButtonVariant` - Default, Outline, Ghost, Destructive, Secondary
 * - `ButtonSize` - Small, Default, Large
 * - `BadgeVariant` - Default, Success, Destructive, Warning
 * - `SnackbarVariant` - Default, Success, Destructive, Warning
 *
 * ## Componentes Legacy (a migrar):
 * - `FormTextField` - ⚠️ Migrar a ShadcnInput
 * - `FeedbackMessage` - ⚠️ Migrar a ShadcnSnackbar
 * - `AnimatedComponents` - ⚠️ Revisar y documentar
 */
@file:JvmName("ShadcnComponents")

package com.example.miappmodular.ui.components

// Este archivo actúa como módulo de re-exportación.
// Todos los componentes individuales están en sus propios archivos:
// - Button.kt → ShadcnButton
// - Input.kt → ShadcnInput
// - Textarea.kt → ShadcnTextarea
// - Card.kt → ShadcnCard
// - Badge.kt → ShadcnBadge
// - Divider.kt → ShadcnDivider
// - Snackbar.kt → ShadcnSnackbar

// Las variantes están en: ui/theme/ComponentVariants.kt y ui/theme/SnackbarVariant.kt
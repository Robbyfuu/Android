//package com.example.miappmodular.ui.screens
//
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.runtime.Composable
//import com.example.miappmodular.ui.theme.MiAppModularTheme
//import com.example.miappmodular.viewmodel.RegisterUiState
//
//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    name = "Register Screen - Empty"
//)
//@Composable
//fun RegisterScreenPreview() {
//    MiAppModularTheme {
//        RegisterScreenContent(
//            uiState = RegisterUiState(),
//            onNameChange = {},
////            onEmailChange = {},
//            onPasswordChange = {},
//            onConfirmPasswordChange = {},
//            onRegisterClick = {},
//            onNavigateBack = {},
//            onCleanError = {}
//        )
//    }
//}
//
//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    name = "Register Screen - With Data"
//)
//@Composable
//fun RegisterScreenPreviewWithData() {
//    MiAppModularTheme {
//        RegisterScreenContent(
//            uiState = RegisterUiState(
//                name = "Juan Pérez",
//                email = "juan@example.com",
//                password = "Password123",
//                confirmPassword = "Password123"
//            ),
//            onNameChange = {},
//            onEmailChange = {},
//            onPasswordChange = {},
//            onConfirmPasswordChange = {},
//            onRegisterClick = {},
//            onNavigateBack = {},
//            onCleanError = {}
//        )
//    }
//}
//
//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    name = "Register Screen - Loading"
//)
//@Composable
//fun RegisterScreenPreviewLoading() {
//    MiAppModularTheme {
//        RegisterScreenContent(
//            uiState = RegisterUiState(
//                name = "Juan Pérez",
//                email = "juan@example.com",
//                password = "Password123",
//                confirmPassword = "Password123",
//                isLoading = true
//            ),
//            onNameChange = {},
//            onEmailChange = {},
//            onPasswordChange = {},
//            onConfirmPasswordChange = {},
//            onRegisterClick = {},
//            onNavigateBack = {},
//            onCleanError = {}
//        )
//    }
//}
//
//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    name = "Register Screen - With Errors"
//)
//@Composable
//fun RegisterScreenPreviewWithErrors() {
//    MiAppModularTheme {
//        RegisterScreenContent(
//            uiState = RegisterUiState(
//                name = "Jo",
//                email = "invalid-email",
//                password = "123",
//                confirmPassword = "456",
//                nameError = "El nombre debe tener mínimo 3 caracteres",
//                emailError = "Email inválido",
//                passwordError = "La contraseña debe tener al menos 8 caracteres",
//                confirmPasswordError = "Las contraseñas no coinciden"
//            ),
//            onNameChange = {},
//            onEmailChange = {},
//            onPasswordChange = {},
//            onConfirmPasswordChange = {},
//            onRegisterClick = {},
//            onNavigateBack = {},
//            onCleanError = {}
//        )
//    }
//}

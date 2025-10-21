package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.entity.User
import com.example.miappmodular.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Estados del perfil de usuario
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val formattedCreatedAt: String = ""
)

/**
 * ViewModel para la pantalla de perfil
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Carga el perfil del usuario actual
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val user = userRepository.getCurrentUser()

                if (user != null) {
                    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                    val formattedDate = dateFormat.format(user.createdAt)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            formattedCreatedAt = formattedDate,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontró el usuario"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar el perfil"
                    )
                }
            }
        }
    }

    /**
     * Recarga el perfil
     */
    fun refresh() {
        loadUserProfile()
    }
}

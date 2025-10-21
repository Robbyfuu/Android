package com.example.miappmodular.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension para crear DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_preferences"
)

/**
 * Gestor de sesión con DataStore
 */
class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    /**
     * Guarda la sesión del usuario
     */
    suspend fun saveUserSession(
        userId: String,
        email: String,
        name: String,
        rememberMe: Boolean = false
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_NAME] = name
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_REMEMBER_ME] = rememberMe
        }
    }

    /**
     * Observable del estado de login
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    /**
     * Observable de los datos del usuario
     */
    val userSession: Flow<UserSession?> = context.dataStore.data.map { preferences ->
        if (preferences[KEY_IS_LOGGED_IN] == true) {
            UserSession(
                userId = preferences[KEY_USER_ID] ?: "",
                email = preferences[KEY_USER_EMAIL] ?: "",
                name = preferences[KEY_USER_NAME] ?: "",
                rememberMe = preferences[KEY_REMEMBER_ME] ?: false
            )
        } else null
    }

    /**
     * Cierra la sesión
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_NAME)
            preferences[KEY_IS_LOGGED_IN] = false
            if (preferences[KEY_REMEMBER_ME] != true) {
                preferences.clear()
            }
        }
    }

    /**
     * Guarda preferencia de tema
     */
    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    /**
     * Observable del tema
     */
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "system"
    }
}

/**
 * Modelo de sesión de usuario
 */
data class UserSession(
    val userId: String,
    val email: String,
    val name: String,
    val rememberMe: Boolean
)
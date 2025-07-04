package com.vidora.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("user_token")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }

    // 🔐 Token
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { it[KEY_TOKEN] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(KEY_TOKEN) }
    }

    // 👤 User ID
    val userEmailFlow: Flow<String?> = context.dataStore.data
        .map { it[KEY_USER_EMAIL] }

    suspend fun saveUserEmail(userEmail: String) {
        context.dataStore.edit { it[KEY_USER_EMAIL] = userEmail }
    }

    suspend fun getUserEmail(): String? {
        return context.dataStore.data.map { it[KEY_USER_EMAIL] }.firstOrNull()
    }

    suspend fun clearUserEmail() {
        context.dataStore.edit { it.remove(KEY_USER_EMAIL) }
    }

    // 🚪 Clear Everything (Logout)
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

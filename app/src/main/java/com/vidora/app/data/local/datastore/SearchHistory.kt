package com.vidora.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject

private val Context.searchDataStore by preferencesDataStore("search_history")

class SearchHistory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authDataStore: UserPreferences
) {
    private fun historyKey(userEmail: String) = stringPreferencesKey("recent_searches_$userEmail")

    val historyFlow: Flow<List<String>> = authDataStore.userEmailFlow
        .map { userEmail ->
            if (userEmail.isNullOrBlank()) {
                emptyList()
            } else {
                context.searchDataStore.data.map { prefs ->
                    prefs[historyKey(userEmail)]?.let { json ->
                        JSONArray(json).let { arr ->
                            List(arr.length()) { i -> arr.getString(i) }
                        }
                    } ?: emptyList()
                }.first()
            }
        }

    suspend fun addQuery(query: String, maxSize: Int = 20) {
        val userId = authDataStore.getUserEmail() ?: return

        context.searchDataStore.edit { prefs ->
            val existing = prefs[historyKey(userId)]?.let { json ->
                JSONArray(json).let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                }
            } ?: emptyList()

            val updated = listOf(query) + existing.filter { it != query }
            val trimmed = updated.take(maxSize)

            prefs[historyKey(userId)] = JSONArray(trimmed).toString()
        }
    }

    suspend fun clearHistory() {
        val userId = authDataStore.getUserEmail() ?: return
        context.searchDataStore.edit { prefs ->
            prefs.remove(historyKey(userId))
        }
    }
}

package com.vidora.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.subscriptionStore by preferencesDataStore("user_subscription")

class SubscriptionDataStore @Inject constructor (
    @ApplicationContext private val context: Context
) {

    private val KEY_SUBSCRIPTIONS = stringSetPreferencesKey("subscriptions")

    val subscribedIds: Flow<Set<String>> = context.subscriptionStore.data
        .map { prefs ->
            prefs[KEY_SUBSCRIPTIONS] ?: emptySet()
        }

    suspend fun saveSubscription(channelId: String) {
        context.subscriptionStore.edit { prefs ->
            val current = prefs[KEY_SUBSCRIPTIONS] ?: emptySet()
            prefs[KEY_SUBSCRIPTIONS] = current + channelId
        }
    }

    suspend fun deleteSubscription(channelId: String) {
        context.subscriptionStore.edit { prefs ->
            val current = prefs[KEY_SUBSCRIPTIONS] ?: emptySet()
            prefs[KEY_SUBSCRIPTIONS] = current - channelId
        }
    }
}
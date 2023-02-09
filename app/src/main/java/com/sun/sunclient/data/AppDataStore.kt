package com.sun.sunclient.data

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.Constants.DATA_STORE_PREFERENCES
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_PREFERENCES)

class AppDataStore(@ApplicationContext private val context: Context) {
    private val ACCESS_KEY = stringPreferencesKey(Constants.PREFERENCES_ACCESS_KEY)
    private val COOKIE_SET_KEY = stringSetPreferencesKey(Constants.PREFERENCES_COOKIE_SET_KEY)

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_KEY] = token
        }
    }

    fun readAccessToken() : Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_KEY] ?: ""
        }
    }

    suspend fun saveCookieSet(cookieSet : Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[COOKIE_SET_KEY] = cookieSet
        }
    }

    fun readCookieSet() : Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[COOKIE_SET_KEY] ?: HashSet()
        }
    }
}
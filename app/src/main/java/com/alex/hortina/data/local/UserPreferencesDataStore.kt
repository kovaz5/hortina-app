package com.alex.hortina.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun saveUser(id: String, name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
        }
    }

    val user: Flow<UserData?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID]
        val name = prefs[USER_NAME]
        val email = prefs[USER_EMAIL]
        if (id != null && name != null && email != null) {
            UserData(id, name, email)
        } else null
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }

    private val SESSION_COOKIE = stringPreferencesKey("session_cookie")

    suspend fun saveSessionCookie(cookie: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_COOKIE] = cookie
        }
    }

    suspend fun getSessionCookie(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[SESSION_COOKIE]
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(SESSION_COOKIE)
        }
    }

}

data class UserData(
    val id: String, val name: String, val email: String
)

package com.example.helion.core.session

import android.content.Context
import android.content.SharedPreferences
import com.example.helion.core.model.ServerEnvironment

/**
 * Manages authentication credentials isolated strictly by environment namespace.
 * Invariant: Credentials from PRIVATE TEST must NEVER bleed into or be retrieved in PRODUCTION.
 */
class AuthCredentialStore(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private fun tokenKey(env: ServerEnvironment): String = "auth_token_${env.name.lowercase()}"

    fun getAuthToken(env: ServerEnvironment): String? {
        return prefs.getString(tokenKey(env), null)
    }

    fun setAuthToken(env: ServerEnvironment, token: String?) {
        if (token.isNullOrBlank()) {
            clearAuthToken(env)
        } else {
            prefs.edit().putString(tokenKey(env), token).apply()
        }
    }

    fun clearAuthToken(env: ServerEnvironment) {
        prefs.edit().remove(tokenKey(env)).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "helion_auth_credentials"
    }
}

package com.example.helion.core.session

import android.content.Context
import android.content.SharedPreferences
import com.example.helion.core.model.ServerEnvironment

/**
 * Persists the selected server environment across application restarts.
 * Invariant: Does not silently switch environments if a server becomes unavailable.
 */
class EnvironmentPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getSelectedEnvironment(): ServerEnvironment {
        val stored = prefs.getString(KEY_SELECTED_ENV, ServerEnvironment.DEMO.name)
        return try {
            ServerEnvironment.valueOf(stored ?: ServerEnvironment.DEMO.name)
        } catch (e: Exception) {
            ServerEnvironment.DEMO
        }
    }

    fun setSelectedEnvironment(env: ServerEnvironment) {
        prefs.edit().putString(KEY_SELECTED_ENV, env.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "helion_environment_prefs"
        private const val KEY_SELECTED_ENV = "key_selected_server_environment"
    }
}

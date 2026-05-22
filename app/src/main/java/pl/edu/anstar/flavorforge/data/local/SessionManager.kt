package pl.edu.anstar.flavorforge.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "FlavorForgePrefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveSession(token: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}

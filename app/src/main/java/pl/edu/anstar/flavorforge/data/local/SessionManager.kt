package pl.edu.anstar.flavorforge.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "FlavorForgePrefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_FAVORITES = "favorite_recipe_ids"
    }

    fun saveSession(token: String, userName: String?, email: String?) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun addFavorite(recipeId: Int) {
        val current = getFavoriteIds().toMutableSet()
        current.add(recipeId.toString())
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun removeFavorite(recipeId: Int) {
        val current = getFavoriteIds().toMutableSet()
        current.remove(recipeId.toString())
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun isFavorite(recipeId: Int): Boolean {
        return getFavoriteIds().contains(recipeId.toString())
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // ----------------------------------------
    // User Settings Helper Functions
    // ----------------------------------------

    fun getSettingsPrefs(): SharedPreferences {
        val email = getEmail()
        val prefsName = if (!email.isNullOrEmpty()) "settings_$email" else "settings"
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun getSettingsPrefs(ctx: Context): SharedPreferences {
        val email = getEmail()
        val prefsName = if (!email.isNullOrEmpty()) "settings_$email" else "settings"
        return ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun applySettings(ctx: Context) {
        val sp = getSettingsPrefs(ctx)

        // 1. Apply Theme
        val savedTheme = sp.getString("app_theme", "system")
        val nightMode = when (savedTheme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        // 2. Apply Language
        val savedLang = sp.getString("app_lang", null)
        if (savedLang != null) {
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}

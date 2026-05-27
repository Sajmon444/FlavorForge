package pl.edu.anstar.flavorforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import pl.edu.anstar.flavorforge.data.local.SessionManager

@HiltAndroidApp
class FlavorForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Automatically load and apply saved user settings (theme/language) on startup
        val sessionManager = SessionManager(this)
        sessionManager.applySettings(this)
    }
}
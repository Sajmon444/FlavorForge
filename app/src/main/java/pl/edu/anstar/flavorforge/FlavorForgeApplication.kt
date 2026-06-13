package pl.edu.anstar.flavorforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import pl.edu.anstar.flavorforge.data.local.SessionManager

@HiltAndroidApp
class FlavorForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val sessionManager = SessionManager(this)
        sessionManager.applySettings(this)
    }
}
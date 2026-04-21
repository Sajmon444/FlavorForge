package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
        
        findViewById<Button>(R.id.btnApplySettings)?.setOnClickListener {
            Toast.makeText(this, "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

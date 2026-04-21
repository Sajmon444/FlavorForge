package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RecipesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recepies)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
}

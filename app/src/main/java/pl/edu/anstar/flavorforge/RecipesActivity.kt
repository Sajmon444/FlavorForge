package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class RecipesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recepies)

        drawerLayout = findViewById(R.id.drawerLayout)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }


        findViewById<ImageView>(R.id.btnFilters)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }


        findViewById<Button>(R.id.btnApplyFilters)?.setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}

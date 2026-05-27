package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.data.local.SessionManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        val userHeader = findViewById<TextView>(R.id.userHeader)
        val userSubHeader = findViewById<TextView>(R.id.tvUserSubHeader)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        
        if (sessionManager.isLoggedIn()) {
            val userName = sessionManager.getUserName() ?: "Zalogowany"
            userHeader.text = userName
            userSubHeader.text = "Dzięki, że korzystasz z naszej aplikacji."
            btnLogout.visibility = android.view.View.VISIBLE
            btnRegister.visibility = android.view.View.GONE
        } else {
            userHeader.text = "Użytkownik niezalogowany"
            userSubHeader.text = getString(R.string.register_for_free)
            btnLogout.visibility = android.view.View.GONE
            btnRegister.visibility = android.view.View.VISIBLE
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnSearchRecipes).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


        findViewById<ImageView>(R.id.logo).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnRecipes).setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnLiked).setOnClickListener {
            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, LikedActivity::class.java))
            } else {
                android.widget.Toast.makeText(
                    this,
                    "Zaloguj się, aby zapisywać ulubione przepisy i mieć je zawsze pod ręką!",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnApplyFilters)?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, RecipesActivity::class.java))
        }

    }
}

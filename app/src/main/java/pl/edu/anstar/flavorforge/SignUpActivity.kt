package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.goToSignIn).setOnClickListener {
            finish()
        }
    }
}

package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        findViewById<Button>(R.id.btnContinueWithoutLogin).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.goToSignUp).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}

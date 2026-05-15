package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.ui.signin.SignInViewModel

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnContinueWithoutLogin = findViewById<Button>(R.id.btnContinueWithoutLogin)
        val goToSignUp = findViewById<TextView>(R.id.goToSignUp)

        btnSignIn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signIn(email, password)
        }

        btnContinueWithoutLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        goToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        viewModel.signInResult.observe(this) { result ->
            result.onSuccess {
                android.util.Log.d("SignInActivity", "Login success: $it")
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure { exception ->
                android.util.Log.e("SignInActivity", "Login failed", exception)
                Toast.makeText(this, exception.message ?: "Login failed", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            btnSignIn.isEnabled = !isLoading
            btnContinueWithoutLogin.isEnabled = !isLoading
        }
    }
}

package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.ui.signup.SignUpViewModel

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val termsCheckbox = findViewById<CheckBox>(R.id.termsCheckbox)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val goToSignIn = findViewById<TextView>(R.id.goToSignIn)

        btnBack?.setOnClickListener {
            finish()
        }

        btnContinue.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signUp(email, password, name)
        }

        goToSignIn.setOnClickListener {
            finish()
        }

        viewModel.signUpResult.observe(this) { result ->
            result.onSuccess {
                android.util.Log.d("SignUpActivity", "Registration success: $it")
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }.onFailure { exception ->
                android.util.Log.e("SignUpActivity", "Registration failed", exception)
                Toast.makeText(this, exception.message ?: "Registration failed", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            btnContinue.isEnabled = !isLoading
            // You might want to show a progress bar here
        }
    }
}

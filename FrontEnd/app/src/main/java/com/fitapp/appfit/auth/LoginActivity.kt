package com.fitapp.appfit.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitapp.appfit.MainActivity
import com.fitapp.appfit.databinding.ActivityLoginBinding
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.utils.SessionManager
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.initialize(applicationContext)

        // Si ya tiene sesión válida, ir directo a MainActivity
        if (SessionManager.isTokenValid()) {
            navigateToMain()
            return
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    // Pequeño delay para que SessionManager termine de guardar el token
                    binding.root.post { navigateToMain() }
                }
                is Resource.Error -> {
                    showLoading(false)
                    val message = when {
                        resource.message?.contains("401") == true ||
                                resource.message?.contains("Credenciales") == true -> "Email o contraseña incorrectos"
                        resource.message?.contains("timeout", ignoreCase = true) == true ||
                                resource.message?.contains("connect", ignoreCase = true) == true -> "No se puede conectar al servidor"
                        else -> resource.message ?: "Error al iniciar sesión"
                    }
                    showError(message)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            isValid = false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }
        return isValid
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
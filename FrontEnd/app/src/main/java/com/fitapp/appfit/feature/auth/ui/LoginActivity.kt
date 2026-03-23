package com.fitapp.appfit.feature.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitapp.appfit.MainActivity
import com.fitapp.appfit.core.session.SessionManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.ActivityAuthLoginBinding
import com.fitapp.appfit.feature.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.initialize(applicationContext)

        SessionManager.onSessionExpired = {
            runOnUiThread {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }

        if (SessionManager.isTokenValid()) {
            navigateToMain()
            return
        }

        if (SessionManager.hasSession()) {
            navigateToMain()
            return
        }

        handleDeepLinkIfPresent(intent)

        setupObservers()
        setupClickListeners()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIfPresent(intent)
    }

    private fun handleDeepLinkIfPresent(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "fitapp" && data.host == "auth") {
            viewModel.handleGoogleCallback(data)
        }
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

        binding.btnGoogleLogin.setOnClickListener {
            viewModel.openGoogleLogin(this)
        }
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
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

        viewModel.googleLoginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    navigateToMain()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "Error con Google Login")
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
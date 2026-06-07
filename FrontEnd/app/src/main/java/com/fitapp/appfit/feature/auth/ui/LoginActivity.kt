package com.fitapp.appfit.feature.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitapp.appfit.MainActivity
import com.fitapp.appfit.core.network.TokenRefreshCoordinator
import com.fitapp.appfit.core.session.SessionManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.core.util.applySystemBarInsets
import com.fitapp.appfit.databinding.ActivityAuthLoginBinding
import com.fitapp.appfit.feature.auth.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarInsets(applyTop = true, applyBottom = true)
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
            val refreshed = runBlocking { TokenRefreshCoordinator.refreshSession() }
            if (refreshed) {
                navigateToMain()
            }
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
        val data = intent?.data ?: return
        if (data.scheme != "fitapp" || data.host != "auth") return

        when (data.path) {
            "/callback" -> viewModel.handleGoogleCallback(data)
            "/verify-email" -> viewModel.handleEmailVerifiedDeepLink(data)
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

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            startActivity(Intent(this, ForgotPasswordActivity::class.java).apply {
                putExtra(ForgotPasswordActivity.EXTRA_EMAIL, email)
            })
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
                    when {
                        resource.message == "EMAIL_NOT_VERIFIED" -> showEmailNotVerifiedDialog()
                        resource.message?.contains("401") == true ||
                                resource.message?.contains("Credenciales") == true ->
                            showError("Email o contraseña incorrectos")
                        resource.message?.contains("timeout", ignoreCase = true) == true ||
                                resource.message?.contains("connect", ignoreCase = true) == true ->
                            showError("No se puede conectar al servidor")
                        else -> showError(resource.message ?: "Error al iniciar sesión")
                    }
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

        viewModel.emailVerifiedState.observe(this) { resource ->
            if (resource is Resource.Success) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Correo verificado")
                    .setMessage("Tu correo ha sido verificado. Ya puedes iniciar sesión.")
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }

        viewModel.resendVerificationState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> showError("Correo de verificación reenviado")
                is Resource.Error -> showError(resource.message ?: "No se pudo reenviar el correo")
                else -> {}
            }
        }
    }

    private fun showEmailNotVerifiedDialog() {
        val email = binding.etEmail.text.toString().trim()
        MaterialAlertDialogBuilder(this)
            .setTitle("Correo no verificado")
            .setMessage("Debes verificar tu correo antes de iniciar sesión. ¿Reenviar el correo de verificación?")
            .setPositiveButton("Reenviar") { _, _ ->
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.resendVerificationByEmail(email)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

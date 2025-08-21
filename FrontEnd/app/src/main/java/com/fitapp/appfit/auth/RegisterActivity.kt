package com.fitapp.appfit.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitapp.appfit.MainActivity
import com.fitapp.appfit.databinding.ActivityRegisterBinding
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.utils.SessionManager
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.initialize(applicationContext)

        setupObservers()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val fullName = binding.etFullName.text.toString()

            if (validateInput(email, password, fullName)) {
                viewModel.register(email, password, fullName)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.registerState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    showSuccessAndNavigate()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "Error en registro")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccessAndNavigate() {
        Snackbar.make(binding.root, "¡Registro exitoso!", Snackbar.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }

    private fun validateInput(email: String, password: String, fullName: String): Boolean {
        var isValid = true

        if (fullName.isBlank()) {
            binding.etFullName.error = "Ingresa tu nombre"
            isValid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            isValid = false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }

        return isValid
    }
}
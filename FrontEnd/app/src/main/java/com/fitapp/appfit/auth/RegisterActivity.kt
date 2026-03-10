package com.fitapp.appfit.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
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

        setupRealTimeValidation()
        setupObservers()

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            if (validateInput(fullName, email, password, confirmPassword)) {
                viewModel.register(email, password, fullName)
            }
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    // Limpia el error del campo en tiempo real mientras el usuario escribe
    private fun setupRealTimeValidation() {
        binding.etFullName.doAfterTextChanged { binding.tilFullName.error = null }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
            // Si ya escribió en confirmar, revalidar en tiempo real
            if (!binding.etConfirmPassword.text.isNullOrEmpty()) {
                if (binding.etPassword.text.toString() == binding.etConfirmPassword.text.toString()) {
                    binding.tilConfirmPassword.error = null
                }
            }
        }
        binding.etConfirmPassword.doAfterTextChanged {
            if (!it.isNullOrEmpty()) {
                if (it.toString() == binding.etPassword.text.toString()) {
                    binding.tilConfirmPassword.error = null
                }
            }
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
                    val message = when {
                        resource.message?.contains("409") == true ||
                                resource.message?.contains("already", ignoreCase = true) == true ||
                                resource.message?.contains("existe", ignoreCase = true) == true ->
                            "Este correo ya está registrado"
                        resource.message?.contains("timeout", ignoreCase = true) == true ||
                                resource.message?.contains("connect", ignoreCase = true) == true ->
                            "No se puede conectar al servidor"
                        else -> resource.message ?: "Error al crear la cuenta"
                    }
                    showError(message)
                }
            }
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Nombre completo
        when {
            fullName.isBlank() -> {
                binding.tilFullName.error = "Ingresa tu nombre completo"
                isValid = false
            }
            fullName.length < 3 -> {
                binding.tilFullName.error = "El nombre debe tener al menos 3 caracteres"
                isValid = false
            }
            fullName.length > 50 -> {
                binding.tilFullName.error = "El nombre no puede superar 50 caracteres"
                isValid = false
            }
            !fullName.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$")) -> {
                binding.tilFullName.error = "Solo se permiten letras y espacios"
                isValid = false
            }
        }

        // Email
        when {
            email.isBlank() -> {
                binding.tilEmail.error = "Ingresa tu correo"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "El correo no es válido"
                isValid = false
            }
        }

        // Contraseña
        when {
            password.isBlank() -> {
                binding.tilPassword.error = "Ingresa una contraseña"
                isValid = false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Mínimo 6 caracteres"
                isValid = false
            }
            password.length > 64 -> {
                binding.tilPassword.error = "Máximo 64 caracteres"
                isValid = false
            }
            password.contains(" ") -> {
                binding.tilPassword.error = "La contraseña no puede tener espacios"
                isValid = false
            }
        }

        // Confirmar contraseña (solo si la contraseña es válida)
        if (isValid || binding.tilPassword.error == null) {
            when {
                confirmPassword.isBlank() -> {
                    binding.tilConfirmPassword.error = "Confirma tu contraseña"
                    isValid = false
                }
                confirmPassword != password -> {
                    binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                    isValid = false
                }
            }
        }

        // Foco al primer campo con error
        if (!isValid) {
            listOf(
                binding.tilFullName,
                binding.tilEmail,
                binding.tilPassword,
                binding.tilConfirmPassword
            ).firstOrNull { it.error != null }
                ?.editText?.requestFocus()
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.etFullName.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccessAndNavigate() {
        Snackbar.make(binding.root, "¡Cuenta creada con éxito!", Snackbar.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }, 1500)
    }
}
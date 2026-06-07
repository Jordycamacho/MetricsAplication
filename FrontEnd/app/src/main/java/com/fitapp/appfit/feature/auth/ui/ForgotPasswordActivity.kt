package com.fitapp.appfit.feature.auth.ui

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.core.util.applySystemBarInsets
import com.fitapp.appfit.databinding.ActivityAuthForgotPasswordBinding
import com.fitapp.appfit.feature.auth.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets(applyTop = true, applyBottom = true)

        val prefilledEmail = intent.getStringExtra(EXTRA_EMAIL).orEmpty()
        if (prefilledEmail.isNotBlank()) {
            binding.etEmail.setText(prefilledEmail)
        }

        binding.btnSend.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Correo inválido"
                return@setOnClickListener
            }
            binding.tilEmail.error = null
            viewModel.forgotPassword(email)
        }

        viewModel.forgotPasswordState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Correo enviado")
                        .setMessage("Si existe una cuenta con ese correo, recibirás un enlace para restablecer tu contraseña.")
                        .setPositiveButton("Aceptar") { _, _ -> finish() }
                        .show()
                }
                is Resource.Error -> {
                    showLoading(false)
                    Snackbar.make(binding.root, resource.message ?: "Error al enviar", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSend.isEnabled = !show
        binding.etEmail.isEnabled = !show
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}

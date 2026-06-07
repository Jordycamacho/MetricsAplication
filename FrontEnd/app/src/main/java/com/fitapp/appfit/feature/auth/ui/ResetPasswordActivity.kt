package com.fitapp.appfit.feature.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.core.util.applySystemBarInsets
import com.fitapp.appfit.databinding.ActivityAuthResetPasswordBinding
import com.fitapp.appfit.feature.auth.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthResetPasswordBinding
    private val viewModel: AuthViewModel by viewModels()
    private var resetToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets(applyTop = true, applyBottom = true)

        resetToken = intent.data?.getQueryParameter("token")
            ?: intent.getStringExtra(EXTRA_TOKEN)

        if (resetToken.isNullOrBlank()) {
            Snackbar.make(binding.root, "Enlace inválido", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnReset.setOnClickListener {
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            when {
                password.length < 6 -> binding.tilPassword.error = "Mínimo 6 caracteres"
                password != confirm -> binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                else -> {
                    binding.tilPassword.error = null
                    binding.tilConfirmPassword.error = null
                    viewModel.resetPassword(resetToken!!, password)
                }
            }
        }

        viewModel.resetPasswordState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Contraseña actualizada")
                        .setMessage("Ya puedes iniciar sesión con tu nueva contraseña.")
                        .setPositiveButton("Iniciar sesión") { _, _ ->
                            startActivity(Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            })
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
                is Resource.Error -> {
                    showLoading(false)
                    Snackbar.make(binding.root, resource.message ?: "Error al restablecer", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnReset.isEnabled = !show
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }
}

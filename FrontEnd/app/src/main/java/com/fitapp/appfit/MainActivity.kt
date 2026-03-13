package com.fitapp.appfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.fitapp.appfit.auth.LoginActivity
import com.fitapp.appfit.databinding.ActivityMainBinding
import com.fitapp.appfit.model.ProfileViewModel
import com.fitapp.appfit.utils.Resource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        showBetaWarningIfNeeded()

        profileViewModel.logoutState.observe(this) { resource ->
            if (resource is Resource.Success) {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }

    private fun showBetaWarningIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val alreadyShown = prefs.getBoolean("beta_warning_shown", false)
        if (alreadyShown) return

        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Versión Beta")
            .setMessage(
                "Estás usando una versión beta de AppFit.\n\n" +
                        "• Puede contener errores o comportamientos inesperados.\n" +
                        "• Algunos datos podrían no guardarse correctamente.\n" +
                        "• Tu feedback es muy valioso para mejorar la app.\n\n" +
                        "¡Gracias por ser parte de esta etapa!"
            )
            .setPositiveButton("Entendido") { dialog, _ ->
                prefs.edit().putBoolean("beta_warning_shown", true).apply()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
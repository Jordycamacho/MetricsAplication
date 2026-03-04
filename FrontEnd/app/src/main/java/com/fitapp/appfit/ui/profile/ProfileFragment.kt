package com.fitapp.appfit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fitapp.appfit.R
import com.fitapp.appfit.auth.LoginActivity
import com.fitapp.appfit.databinding.FragmentProfileBinding
import com.fitapp.appfit.databinding.ItemProfileRowBinding
import com.fitapp.appfit.model.ProfileViewModel
import com.fitapp.appfit.response.user.request.UserResponse
import com.fitapp.appfit.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRows()
        setupObservers()
        viewModel.loadProfile()
    }

    // ── Configuración de filas ───────────────────────────────────────────────
    // Los include con id generan bindings anidados: binding.rowEditProfile es ItemProfileRowBinding

    private fun setupRows() {
        with(binding.rowEditProfile) {
            ivRowIcon.setImageResource(R.drawable.ic_profile)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Editar perfil"
            tvRowSubtitle.visibility = View.GONE
            root.setOnClickListener { showEditProfileDialog() }
        }

        with(binding.rowChangePassword) {
            ivRowIcon.setImageResource(R.drawable.ic_lock)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Cambiar contraseña"
            tvRowSubtitle.visibility = View.GONE
            root.setOnClickListener { showChangePasswordDialog() }
        }

        with(binding.rowVerifyEmail) {
            ivRowIcon.setImageResource(R.drawable.ic_email)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Verificar correo"
            tvRowSubtitle.visibility = View.GONE
            root.setOnClickListener { confirmResendVerification() }
        }

        with(binding.rowSubscription) {
            ivRowIcon.setImageResource(R.drawable.ic_crown)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Mi suscripción"
            tvRowSubtitle.text = "Plan FREE activo"
            tvRowSubtitle.visibility = View.VISIBLE
            // TODO: navegar a suscripción cuando esté disponible
        }

        with(binding.rowLogout) {
            ivRowIcon.setImageResource(R.drawable.ic_logout)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Cerrar sesión"
            tvRowSubtitle.visibility = View.GONE
            ivRowChevron.visibility = View.GONE
            root.setOnClickListener { confirmLogout() }
        }

        with(binding.rowDeleteAccount) {
            ivRowIcon.setImageResource(R.drawable.ic_delete)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_light)
            tvRowTitle.text = "Eliminar cuenta"
            tvRowTitle.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            tvRowSubtitle.text = "Esta acción no se puede deshacer"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener { confirmDeleteAccount() }
        }
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.profileState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* opcional: shimmer o skeleton */ }
                is Resource.Success -> populateProfile(resource.data!!)
                is Resource.Error   -> showSnackbar(resource.message ?: "Error al cargar perfil")
            }
        }

        viewModel.updateProfileState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> { showSnackbar("Perfil actualizado"); viewModel.loadProfile() }
                is Resource.Error   -> showSnackbar(resource.message ?: "Error al actualizar")
                else -> {}
            }
        }

        viewModel.changePasswordState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> showSnackbar("Contraseña actualizada correctamente")
                is Resource.Error   -> showSnackbar(resource.message ?: "Error al cambiar contraseña")
                else -> {}
            }
        }

        viewModel.logoutState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) navigateToLogin()
        }

        viewModel.deleteAccountState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> { showSnackbar("Cuenta eliminada"); navigateToLogin() }
                is Resource.Error   -> showSnackbar(resource.message ?: "Error al eliminar cuenta")
                else -> {}
            }
        }

        viewModel.resendVerificationState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> showSnackbar("Correo de verificación enviado")
                is Resource.Error   -> showSnackbar(resource.message ?: "Error al enviar correo")
                else -> {}
            }
        }
    }

    // ── Poblar UI ────────────────────────────────────────────────────────────

    private fun populateProfile(user: UserResponse) {
        binding.tvFullName.text = user.fullName?.takeIf { it.isNotBlank() } ?: "Sin nombre"
        binding.tvEmail.text = user.email
        binding.tvStatPlan.text = user.subscription?.type ?: "FREE"
        binding.tvStatMaxRoutines.text = user.maxRoutines.toString()
        binding.tvPlanBadge.text = user.subscription?.type ?: "FREE"

        // Estado de verificación de correo
        with(binding.rowVerifyEmail) {
            if (user.emailVerified) {
                ivRowIcon.setImageResource(R.drawable.ic_check)
                tvRowTitle.text = "Correo verificado"
                tvRowBadge.visibility = View.GONE
                root.isClickable = false
                root.alpha = 0.5f
            } else {
                tvRowBadge.text = "No verificado"
                tvRowBadge.visibility = View.VISIBLE
            }
        }

        // Subtítulo de suscripción
        user.subscription?.endDate?.let { endDate ->
            binding.rowSubscription.tvRowSubtitle.text = "Expira: $endDate"
            binding.rowSubscription.tvRowSubtitle.visibility = View.VISIBLE
        }
    }

    // ── Diálogos ─────────────────────────────────────────────────────────────

    private fun showEditProfileDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(dialogView)

        val currentName = binding.tvFullName.text.toString()
        dialogView.findViewById<TextInputEditText>(R.id.et_full_name)
            .setText(if (currentName == "Sin nombre") "" else currentName)

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btn_save).setOnClickListener {
            val newName = dialogView.findViewById<TextInputEditText>(R.id.et_full_name)
                .text.toString().trim()
            if (newName.isBlank()) {
                dialogView.findViewById<TextInputEditText>(R.id.et_full_name).error = "Ingresa tu nombre"
                return@setOnClickListener
            }
            viewModel.updateProfile(newName)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        dialog.setContentView(dialogView)

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btn_save).setOnClickListener {
            val current = dialogView.findViewById<TextInputEditText>(R.id.et_current_password)
                .text.toString()
            val newPwd = dialogView.findViewById<TextInputEditText>(R.id.et_new_password)
                .text.toString()

            if (current.isBlank()) {
                dialogView.findViewById<TextInputEditText>(R.id.et_current_password)
                    .error = "Ingresa tu contraseña actual"
                return@setOnClickListener
            }
            if (newPwd.length < 8) {
                dialogView.findViewById<TextInputEditText>(R.id.et_new_password)
                    .error = "Mínimo 8 caracteres"
                return@setOnClickListener
            }
            viewModel.changePassword(current, newPwd)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Cerrar sesión")
            .setMessage("¿Deseas cerrar sesión en este dispositivo?")
            .setPositiveButton("Cerrar sesión") { _, _ -> viewModel.logout() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmResendVerification() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Verificar correo")
            .setMessage("Se enviará un enlace a ${binding.tvEmail.text}")
            .setPositiveButton("Enviar") { _, _ -> viewModel.resendVerification() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDeleteAccount() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Eliminar cuenta")
            .setMessage(
                "Tu cuenta será desactivada inmediatamente y eliminada permanentemente en 30 días.\n\n¿Estás seguro?"
            )
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteAccount() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Navegación ───────────────────────────────────────────────────────────

    private fun navigateToLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.fitapp.appfit.feature.profile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.BuildConfig
import com.fitapp.appfit.R
import com.fitapp.appfit.core.preferences.AppPreferences
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentProfileBinding
import com.fitapp.appfit.feature.auth.ui.LoginActivity
import com.fitapp.appfit.feature.profile.ProfileViewModel
import com.fitapp.appfit.feature.profile.model.response.UserResponse
import com.fitapp.appfit.feature.profile.util.LocalCacheCleaner
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

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
        setupStaticUi()
        setupRows()
        setupObservers()
        viewModel.loadProfile()
    }

    override fun onResume() {
        super.onResume()
        refreshAppRowSubtitles()
        binding.rowWorkoutSettings.tvRowSubtitle.text =
            WorkoutPreferences.getProfileSummary(requireContext())
    }

    private fun setupStaticUi() {
        binding.tvVersion.text = "JNOBFIT v${BuildConfig.VERSION_NAME}"
    }

    // ── Filas ────────────────────────────────────────────────────────────────

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
            tvRowSubtitle.text = "Cargando..."
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener {
                findNavController().navigate(R.id.navigation_subscription)
            }
        }

        with(binding.rowWorkoutSettings) {
            ivRowIcon.setImageResource(R.drawable.ic_settings_24)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Ajustes de entrenamiento"
            tvRowSubtitle.text = WorkoutPreferences.getProfileSummary(requireContext())
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener {
                findNavController().navigate(R.id.navigation_workout_preferences)
            }
        }

        with(binding.rowUnits) {
            ivRowIcon.setImageResource(R.drawable.ic_settings_24)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Unidades de medida"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener { showUnitsSheet() }
        }

        with(binding.rowPrefill) {
            ivRowIcon.setImageResource(R.drawable.ic_settings_24)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Valores pre-rellenados"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener { showPrefillSheet() }
        }

        with(binding.rowClearCache) {
            ivRowIcon.setImageResource(R.drawable.ic_delete)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Limpiar caché local"
            tvRowSubtitle.text = "Rutinas y datos offline en caché"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener { confirmClearCache() }
        }

        with(binding.rowFeedback) {
            ivRowIcon.setImageResource(R.drawable.ic_email)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = getString(R.string.feedback_row_title)
            tvRowSubtitle.text = getString(R.string.feedback_row_subtitle)
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener {
                findNavController().navigate(R.id.action_profile_to_feedback)
            }
        }

        with(binding.rowAbout) {
            ivRowIcon.setImageResource(R.drawable.ic_profile)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Acerca de JNOBFIT"
            tvRowSubtitle.text = "JNOBFIT v${BuildConfig.VERSION_NAME}"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener { showAboutSheet() }
        }

        with(binding.rowImportExport) {
            ivRowIcon.setImageResource(R.drawable.ic_add)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Importar / Exportar"
            tvRowSubtitle.text = "Beta — copia de rutinas"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener {
                findNavController().navigate(R.id.action_profile_to_import_export)
            }
        }

        with(binding.rowLegal) {
            ivRowIcon.setImageResource(R.drawable.ic_lock)
            ivRowIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            tvRowTitle.text = "Privacidad y términos"
            tvRowSubtitle.text = "Próximamente"
            tvRowSubtitle.visibility = View.VISIBLE
            root.setOnClickListener { showLegalSheet() }
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

        refreshAppRowSubtitles()
    }

    private fun refreshAppRowSubtitles() {
        val ctx = requireContext()
        binding.rowUnits.tvRowSubtitle.text =
            "Peso: ${AppPreferences.getWeightUnitLabel(ctx)} · Dist: ${AppPreferences.getDistanceUnitLabel(ctx)}"
        binding.rowPrefill.tvRowSubtitle.text = AppPreferences.getPrefillStrategyLabel(ctx)
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.profileState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> populateProfile(resource.data!!)
                is Resource.Error   -> showSnackbar(resource.message ?: "Error al cargar perfil")
            }
        }

        viewModel.routineCount.observe(viewLifecycleOwner) { count ->
            binding.tvStatRoutines.text = count.toString()
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
        val displayName = user.fullName?.takeIf { it.isNotBlank() } ?: "Sin nombre"
        binding.tvFullName.text = displayName
        binding.tvEmail.text = user.email
        binding.tvStatPlan.text = user.subscription?.type ?: "FREE"
        binding.tvPlanBadge.text = user.subscription?.type ?: "FREE"
        binding.tvStatMaxRoutines.text = user.subscription?.maxRoutines?.toString() ?: "—"
        updateAvatarInitials(displayName)

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

        user.subscription?.endDate?.let { endDate ->
            binding.rowSubscription.tvRowSubtitle.text = "Expira: $endDate"
            binding.rowSubscription.tvRowSubtitle.visibility = View.VISIBLE
        }
    }

    private fun updateAvatarInitials(fullName: String) {
        val initials = deriveInitials(fullName)
        if (initials.isNullOrBlank() || fullName == "Sin nombre") {
            binding.tvAvatarInitials.visibility = View.GONE
            binding.ivAvatar.visibility = View.VISIBLE
            return
        }
        binding.tvAvatarInitials.text = initials
        binding.tvAvatarInitials.visibility = View.VISIBLE
        binding.ivAvatar.visibility = View.INVISIBLE
    }

    private fun deriveInitials(fullName: String): String? {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return null
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            else -> parts[0].take(2).uppercase()
        }
    }

    // ── Bottom sheets ────────────────────────────────────────────────────────

    private fun showUnitsSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val sheetView = layoutInflater.inflate(R.layout.sheet_units, null)
        dialog.setContentView(sheetView)

        val ctx = requireContext()
        when (AppPreferences.getWeightUnit(ctx)) {
            AppPreferences.WeightUnit.KG -> sheetView.findViewById<RadioButton>(R.id.rb_weight_kg).isChecked = true
            AppPreferences.WeightUnit.LBS -> sheetView.findViewById<RadioButton>(R.id.rb_weight_lbs).isChecked = true
        }
        when (AppPreferences.getDistanceUnit(ctx)) {
            AppPreferences.DistanceUnit.M -> sheetView.findViewById<RadioButton>(R.id.rb_distance_m).isChecked = true
            AppPreferences.DistanceUnit.FT -> sheetView.findViewById<RadioButton>(R.id.rb_distance_ft).isChecked = true
        }

        sheetView.findViewById<RadioGroup>(R.id.rg_weight_unit)
            .setOnCheckedChangeListener { _, checkedId ->
                val unit = if (checkedId == R.id.rb_weight_lbs) {
                    AppPreferences.WeightUnit.LBS
                } else {
                    AppPreferences.WeightUnit.KG
                }
                AppPreferences.setWeightUnit(ctx, unit)
                refreshAppRowSubtitles()
            }

        sheetView.findViewById<RadioGroup>(R.id.rg_distance_unit)
            .setOnCheckedChangeListener { _, checkedId ->
                val unit = if (checkedId == R.id.rb_distance_ft) {
                    AppPreferences.DistanceUnit.FT
                } else {
                    AppPreferences.DistanceUnit.M
                }
                AppPreferences.setDistanceUnit(ctx, unit)
                refreshAppRowSubtitles()
            }

        dialog.show()
    }

    private fun showPrefillSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val sheetView = layoutInflater.inflate(R.layout.sheet_prefill, null)
        dialog.setContentView(sheetView)

        val ctx = requireContext()
        when (AppPreferences.getPrefillStrategy(ctx)) {
            AppPreferences.PrefillStrategy.LAST_SAME_ROUTINE ->
                sheetView.findViewById<RadioButton>(R.id.rb_prefill_same_routine).isChecked = true
            AppPreferences.PrefillStrategy.LAST_EXERCISE ->
                sheetView.findViewById<RadioButton>(R.id.rb_prefill_last_exercise).isChecked = true
        }

        sheetView.findViewById<RadioGroup>(R.id.rg_prefill_strategy)
            .setOnCheckedChangeListener { _, checkedId ->
                val strategy = if (checkedId == R.id.rb_prefill_last_exercise) {
                    AppPreferences.PrefillStrategy.LAST_EXERCISE
                } else {
                    AppPreferences.PrefillStrategy.LAST_SAME_ROUTINE
                }
                AppPreferences.setPrefillStrategy(ctx, strategy)
                refreshAppRowSubtitles()
            }

        dialog.show()
    }

    private fun showAboutSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val sheetView = layoutInflater.inflate(R.layout.sheet_about, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<TextView>(R.id.tv_about_version).text =
            "JNOBFIT v${BuildConfig.VERSION_NAME}"
        sheetView.findViewById<TextView>(R.id.tv_about_website).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://jnobfit.online")))
        }

        dialog.show()
    }

    private fun showLegalSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        dialog.setContentView(layoutInflater.inflate(R.layout.sheet_legal, null))
        dialog.show()
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

    private fun confirmClearCache() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Limpiar caché local")
            .setMessage(
                "Se eliminarán las rutinas guardadas offline y los datos en caché. " +
                    "Tu sesión no se cerrará.\n\n¿Continuar?"
            )
            .setPositiveButton("Limpiar") { _, _ ->
                lifecycleScope.launch {
                    when (val result = LocalCacheCleaner.clearRoutineCache(requireContext())) {
                        LocalCacheCleaner.Result.Success ->
                            showSnackbar("Caché local limpiada")
                        LocalCacheCleaner.Result.ActiveWorkoutInProgress ->
                            showSnackbar("Hay un entrenamiento en curso. Termínalo antes de limpiar.")
                        is LocalCacheCleaner.Result.Error ->
                            showSnackbar(result.message)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

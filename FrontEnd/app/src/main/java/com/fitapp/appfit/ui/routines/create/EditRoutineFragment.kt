// com.fitapp.appfit.ui.routines.create/EditRoutineFragment.kt
package com.fitapp.appfit.ui.routines.create

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.constants.NavigationKeys
import com.fitapp.appfit.databinding.FragmentEditRoutineBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.routine.request.UpdateRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource

class EditRoutineFragment : Fragment() {
    private var _binding: FragmentEditRoutineBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val args: EditRoutineFragmentArgs by navArgs()

    private var sportsMap = mutableMapOf<String, Long>()
    private var currentRoutineId: Long = 0
    private var currentRoutine: RoutineResponse? = null
    private var isSportsLoaded = false
    private var isRoutineLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ID de la rutina de los argumentos
        currentRoutineId = args.routineId

        Log.d("EditRoutine", "Cargando rutina ID: $currentRoutineId")

        setupSportsSpinner()
        setupClickListeners()
        setupObservers()
        setupNavigationResultListener()

        // Cargar datos EN ORDEN: primero deportes, luego rutina
        sportViewModel.getAllSports()
    }

    private fun setupNavigationResultListener() {
        // Escuchar resultados de navegación
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            NavigationKeys.NEED_REFRESH
        )?.observe(viewLifecycleOwner) { needRefresh ->
            if (needRefresh == true) {
                Log.d("EditRoutine", "🔄 Recibida señal de refresco")
                Handler(Looper.getMainLooper()).postDelayed({
                    loadRoutineData()
                }, 500)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>(
                    NavigationKeys.NEED_REFRESH
                )
            }
        }
    }

    private fun setupSportsSpinner() {
        val placeholder = arrayOf("Seleccionar deporte (opcional)")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            placeholder
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSports.adapter = adapter
    }

    private fun setupObservers() {
        // Observar todos los deportes
        sportViewModel.allSportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                        isSportsLoaded = true

                        // Una vez cargados deportes, cargar la rutina
                        if (!isRoutineLoaded) {
                            loadRoutineData()
                        } else {
                            // Si ya se cargó la rutina, actualizar el deporte
                            currentRoutine?.let { routine ->
                                selectSportInSpinner(routine)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    showToast("Error cargando deportes: ${resource.message}")
                    loadRoutineData()
                }
                is Resource.Loading -> {
                    // Mostrar loading si quieres
                }
            }
        })

        // Observar los detalles de la rutina - ¡ESTE ES EL QUE FALTABA!
        routineViewModel.routineDetailState.observe(viewLifecycleOwner, Observer { resource ->
            binding.progressBar.isVisible = false

            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { routine ->
                        Log.d("EditRoutine", "✅ Rutina cargada: ${routine.id}")
                        Log.d("EditRoutine", "  - SportName: ${routine.sportName}")
                        Log.d("EditRoutine", "  - SportId: ${routine.sportId}")
                        Log.d("EditRoutine", "  - TrainingDays: ${routine.trainingDays}")
                        Log.d("EditRoutine", "  - SessionsPerWeek: ${routine.sessionsPerWeek}")

                        currentRoutine = routine
                        isRoutineLoaded = true

                        // Poblar el formulario con los datos de la rutina
                        populateFormWithRoutineData(routine)

                        // Si los deportes ya se cargaron, seleccionar el deporte en el spinner
                        if (isSportsLoaded) {
                            selectSportInSpinner(routine)
                        }
                    } ?: run {
                        showError("La rutina no contiene datos")
                    }
                }
                is Resource.Error -> {
                    showError("Error cargando rutina: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        })

        // Observar el estado de actualización
        routineViewModel.updateRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            binding.btnSaveRoutine.isEnabled = true
            binding.progressBar.isVisible = false

            when (resource) {
                is Resource.Success -> {
                    showToast("✅ Rutina actualizada exitosamente")

                    // Notificar que se actualizó
                    routineViewModel.notifyAnyUpdate()

                    // Enviar señal por SavedStateHandle
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        NavigationKeys.ROUTINE_UPDATED, true
                    )

                    // Pequeño delay antes de navegar
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigateUp()
                    }, 500)
                }
                is Resource.Error -> {
                    showError("Error: ${resource.message ?: "Error desconocido"}")
                }
                is Resource.Loading -> {
                    binding.btnSaveRoutine.isEnabled = false
                    binding.progressBar.isVisible = true
                }
            }
        })

        // Observar el estado de eliminación
        routineViewModel.deleteRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    showToast("✅ Rutina eliminada exitosamente")

                    // Notificar que se eliminó
                    routineViewModel.notifyAnyUpdate()

                    // Enviar señal por SavedStateHandle
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        NavigationKeys.ROUTINE_DELETED, true
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigateUp()
                    }, 500)
                }
                is Resource.Error -> {
                    showError("Error eliminando rutina: ${resource.message}")
                }
                else -> {}
            }
        })
    }

    private fun loadRoutineData() {
        // Mostrar loading
        binding.progressBar.isVisible = true
        routineViewModel.getRoutine(currentRoutineId)
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf("Seleccionar deporte")

        sports.forEach { sport ->
            val displayName = if (sport.isPredefined) {
                " ${sport.name} (Predefinido)"
            } else {
                " ${sport.name} (Personalizado)"
            }
            sportNames.add(displayName)
            sportsMap[displayName] = sport.id
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sportNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSports.adapter = adapter

        Log.d("EditRoutine", "Deportes cargados: ${sports.size} deportes")
    }

    private fun selectSportInSpinner(routine: RoutineResponse) {
        val sportName = routine.sportName
        val sportId = routine.sportId

        Log.d("EditRoutine", "Intentando seleccionar deporte: sportName='$sportName', sportId='$sportId'")

        if (sportName != null && sportId != null) {
            // Buscar el deporte en la lista por ID
            val displayName = sportsMap.entries.find { entry ->
                entry.value == sportId
            }?.key

            if (displayName != null) {
                val adapter = binding.spinnerSports.adapter as? ArrayAdapter<String>
                if (adapter != null) {
                    val position = adapter.getPosition(displayName)
                    if (position >= 0) {
                        binding.spinnerSports.setSelection(position)
                        Log.d("EditRoutine", "✅ Deporte seleccionado: $displayName (posición $position)")
                        return
                    }
                }
            }

            // Si no lo encuentra por ID, buscar por nombre
            val alternativeDisplayName = sportsMap.entries.find { entry ->
                entry.key.contains(sportName, ignoreCase = true)
            }?.key

            if (alternativeDisplayName != null) {
                val adapter = binding.spinnerSports.adapter as? ArrayAdapter<String>
                adapter?.let {
                    val position = it.getPosition(alternativeDisplayName)
                    if (position >= 0) {
                        binding.spinnerSports.setSelection(position)
                        Log.d("EditRoutine", "✅ Deporte encontrado por nombre: $alternativeDisplayName")
                        return
                    }
                }
            }
        }

        // Si no encuentra el deporte, seleccionar la opción por defecto
        Log.d("EditRoutine", "⚠️ Deporte no encontrado, seleccionando opcional")
        binding.spinnerSports.setSelection(0)
    }

    private fun populateFormWithRoutineData(routine: RoutineResponse) {
        Log.d("EditRoutine", "=== POBLANDO FORMULARIO ===")
        Log.d("EditRoutine", "Nombre: ${routine.name}")
        Log.d("EditRoutine", "Deporte: ${routine.sportName}")
        Log.d("EditRoutine", "Días: ${routine.trainingDays}")
        Log.d("EditRoutine", "Sesiones/semana: ${routine.sessionsPerWeek}")
        Log.d("EditRoutine", "Objetivo: ${routine.goal}")
        Log.d("EditRoutine", "Activa: ${routine.isActive}")

        // 1. Nombre
        binding.etRoutineName.setText(routine.name)

        // 2. Descripción
        binding.etRoutineDescription.setText(routine.description ?: "")

        // 3. Objetivo
        binding.etGoal.setText(routine.goal ?: "")

        // 4. Sesiones por semana - MANEJAR CASO NULL o 0
        val sessions = when {
            routine.sessionsPerWeek == null -> "3"
            routine.sessionsPerWeek <= 0 -> "3"
            else -> routine.sessionsPerWeek.toString()
        }
        binding.etSessionsPerWeek.setText(sessions)
        Log.d("EditRoutine", "Sesiones establecidas: $sessions")

        // 5. DÍAS DE ENTRENAMIENTO
        val days = routine.trainingDays ?: emptySet()
        setTrainingDaysCheckboxes(days)
        Log.d("EditRoutine", "Checkboxes configurados para: $days")

        // 6. Estado activo
        updateToggleButton(routine.isActive)

        Log.d("EditRoutine", "✅ Formulario poblado correctamente")
    }

    private fun setTrainingDaysCheckboxes(days: Set<String>) {
        // Limpiar todos primero
        clearAllDayCheckboxes()

        Log.d("EditRoutine", "Configurando checkboxes para días: $days")

        // Marcar solo los días que están en el conjunto
        days.forEach { day ->
            val upperDay = day.uppercase()
            Log.d("EditRoutine", "Procesando día: $day -> $upperDay")

            when (upperDay) {
                "MONDAY" -> {
                    binding.cbMonday.isChecked = true
                    Log.d("EditRoutine", "✓ Lunes marcado")
                }
                "TUESDAY" -> {
                    binding.cbTuesday.isChecked = true
                    Log.d("EditRoutine", "✓ Martes marcado")
                }
                "WEDNESDAY" -> {
                    binding.cbWednesday.isChecked = true
                    Log.d("EditRoutine", "✓ Miércoles marcado")
                }
                "THURSDAY" -> {
                    binding.cbThursday.isChecked = true
                    Log.d("EditRoutine", "✓ Jueves marcado")
                }
                "FRIDAY" -> {
                    binding.cbFriday.isChecked = true
                    Log.d("EditRoutine", "✓ Viernes marcado")
                }
                "SATURDAY" -> {
                    binding.cbSaturday.isChecked = true
                    Log.d("EditRoutine", "✓ Sábado marcado")
                }
                "SUNDAY" -> {
                    binding.cbSunday.isChecked = true
                    Log.d("EditRoutine", "✓ Domingo marcado")
                }
                else -> Log.w("EditRoutine", "⚠️ Día no reconocido: $day")
            }
        }

        // Verificar qué checkboxes están marcados
        val marcados = mutableListOf<String>()
        if (binding.cbMonday.isChecked) marcados.add("Lunes")
        if (binding.cbTuesday.isChecked) marcados.add("Martes")
        if (binding.cbWednesday.isChecked) marcados.add("Miércoles")
        if (binding.cbThursday.isChecked) marcados.add("Jueves")
        if (binding.cbFriday.isChecked) marcados.add("Viernes")
        if (binding.cbSaturday.isChecked) marcados.add("Sábado")
        if (binding.cbSunday.isChecked) marcados.add("Domingo")

        Log.d("EditRoutine", "Checkboxes marcados después: $marcados")
    }

    private fun clearAllDayCheckboxes() {
        binding.cbMonday.isChecked = false
        binding.cbTuesday.isChecked = false
        binding.cbWednesday.isChecked = false
        binding.cbThursday.isChecked = false
        binding.cbFriday.isChecked = false
        binding.cbSaturday.isChecked = false
        binding.cbSunday.isChecked = false
    }

    private fun updateToggleButton(isActive: Boolean) {
        val text = if (isActive) "Desactivar Rutina" else "Activar Rutina"
        binding.btnToggleActive.text = text

        // Cambiar color basado en estado
        if (isActive) {
            // Rojo para desactivar
            binding.btnToggleActive.setBackgroundColor(android.graphics.Color.parseColor("#FF5252"))
            binding.btnToggleActive.setTextColor(android.graphics.Color.WHITE)
        } else {
            // Verde para activar
            binding.btnToggleActive.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            binding.btnToggleActive.setTextColor(android.graphics.Color.WHITE)
        }

        Log.d("EditRoutine", "Botón configurado: $text (activa: $isActive)")
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveRoutine.setOnClickListener {
            if (validateAllFields()) {
                updateRoutine()
            }
        }

        binding.btnDeleteRoutine.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnToggleActive.setOnClickListener {
            currentRoutine?.let { routine ->
                val newActiveState = !routine.isActive
                Log.d("EditRoutine", "Cambiando estado de rutina a: $newActiveState")
                routineViewModel.updateRoutine(currentRoutineId,
                    UpdateRoutineRequest(isActive = newActiveState))
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Rutina")
            .setMessage("¿Estás seguro de que quieres eliminar esta rutina? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                routineViewModel.deleteRoutine(currentRoutineId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun validateAllFields(): Boolean {
        var isValid = true

        // Validar nombre
        val name = binding.etRoutineName.text.toString()
        if (name.isEmpty()) {
            binding.etRoutineName.error = "El nombre es obligatorio"
            isValid = false
        }

        // Validar que al menos un día esté seleccionado
        val hasDaysSelected = binding.cbMonday.isChecked ||
                binding.cbTuesday.isChecked ||
                binding.cbWednesday.isChecked ||
                binding.cbThursday.isChecked ||
                binding.cbFriday.isChecked ||
                binding.cbSaturday.isChecked ||
                binding.cbSunday.isChecked

        if (!hasDaysSelected) {
            showToast("Selecciona al menos un día de entrenamiento")
            isValid = false
        }

        // Validar sesiones por semana
        val sessions = binding.etSessionsPerWeek.text.toString()
        if (sessions.isEmpty()) {
            binding.etSessionsPerWeek.error = "Las sesiones por semana son obligatorias"
            isValid = false
        } else {
            val sessionsInt = sessions.toIntOrNull()
            if (sessionsInt == null || sessionsInt < 1 || sessionsInt > 7) {
                binding.etSessionsPerWeek.error = "Debe ser un número entre 1 y 7"
                isValid = false
            }
        }

        return isValid
    }

    private fun updateRoutine() {
        val name = binding.etRoutineName.text.toString()
        val selectedSport = binding.spinnerSports.selectedItem.toString()
        val description = binding.etRoutineDescription.text.toString()
        val trainingDays = getSelectedTrainingDaysAsStrings()
        val goal = binding.etGoal.text.toString()
        val sessionsPerWeek = binding.etSessionsPerWeek.text.toString().toIntOrNull() ?: 3

        // Obtener sportId del mapa
        val sportId = if (selectedSport != "Seleccionar deporte (opcional)") {
            sportsMap[selectedSport]
        } else {
            null
        }

        Log.d("EditRoutine", "=== ACTUALIZANDO RUTINA ===")
        Log.d("EditRoutine", "  - Nombre: $name")
        Log.d("EditRoutine", "  - Deporte seleccionado: $selectedSport")
        Log.d("EditRoutine", "  - Deporte ID: $sportId")
        Log.d("EditRoutine", "  - Días: $trainingDays")
        Log.d("EditRoutine", "  - Sesiones/semana: $sessionsPerWeek")
        Log.d("EditRoutine", "  - Objetivo: $goal")

        // Crear el request de actualización
        val request = UpdateRoutineRequest(
            name = name,
            description = if (description.isNotBlank()) description else null,
            sportId = sportId,
            trainingDays = trainingDays,
            goal = if (goal.isNotBlank()) goal else null,
            sessionsPerWeek = sessionsPerWeek
        )

        routineViewModel.updateRoutine(currentRoutineId, request)
    }

    private fun getSelectedTrainingDaysAsStrings(): List<String> {
        val days = mutableListOf<String>()
        if (binding.cbMonday.isChecked) days.add("MONDAY")
        if (binding.cbTuesday.isChecked) days.add("TUESDAY")
        if (binding.cbWednesday.isChecked) days.add("WEDNESDAY")
        if (binding.cbThursday.isChecked) days.add("THURSDAY")
        if (binding.cbFriday.isChecked) days.add("FRIDAY")
        if (binding.cbSaturday.isChecked) days.add("SATURDAY")
        if (binding.cbSunday.isChecked) days.add("SUNDAY")
        return days
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
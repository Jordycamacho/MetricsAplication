package com.fitapp.appfit.ui.routines.sets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentConfigureSetsBinding
import com.fitapp.appfit.model.RoutineSetTemplateViewModel
import com.fitapp.appfit.response.routine.request.SetParameterRequest
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.sets.request.CreateSetTemplateRequest
import com.fitapp.appfit.utils.Resource
import kotlin.properties.Delegates

class ConfigureSetsFragment : Fragment() {

    private var _binding: FragmentConfigureSetsBinding? = null
    private val binding get() = _binding!!

    private val args: ConfigureSetsFragmentArgs by navArgs()
    private val setTemplateViewModel: RoutineSetTemplateViewModel by viewModels()

    private var routineExerciseId by Delegates.notNull<Long>()
    private var currentSets = mutableListOf<RoutineSetTemplateResponse>()

    companion object {
        private const val TAG = "ConfigureSetsFragment"
        private val SET_TYPES = listOf(
            "NORMAL", "WARM_UP", "DROP_SET", "SUPER_SET", "GIANT_SET",
            "PYRAMID", "REVERSE_PYRAMID", "CLUSTER", "REST_PAUSE",
            "ECCENTRIC", "ISOMETRIC"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigureSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineExerciseId = args.routineExerciseId
        Log.d(TAG, "Configurando sets para ejercicio: $routineExerciseId")

        setupToolbar()
        setupSpinners()
        setupListeners()
        setupObservers()
        loadExistingSets()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Configurar Sets"
    }

    private fun setupSpinners() {
        // Configurar spinner de tipos de set
        val setTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SET_TYPES
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerSetType.adapter = setTypeAdapter
        binding.spinnerSetType.setSelection(0) // NORMAL por defecto
    }

    private fun setupListeners() {
        // Botón para agregar set
        binding.btnAddSet.setOnClickListener {
            addSet()
        }

        // Botón para guardar todos los sets
        binding.btnSaveAll.setOnClickListener {
            saveAllSets()
        }

        // Botón para limpiar todos los sets
        binding.btnClearAll.setOnClickListener {
            clearAllSets()
        }

        // Botón para configurar parámetros avanzados
        binding.btnAdvancedParams.setOnClickListener {
            showAdvancedParamsDialog()
        }
    }

    private fun setupObservers() {
        // Observar creación de set
        setTemplateViewModel.createSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { setResponse ->
                        currentSets.add(setResponse)
                        updateSetsList()
                        Toast.makeText(requireContext(), "Set agregado exitosamente", Toast.LENGTH_SHORT).show()
                        clearForm()
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })

        // Observar carga de sets existentes
        setTemplateViewModel.getSetTemplatesByExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { sets ->
                        currentSets.clear()
                        currentSets.addAll(sets)
                        updateSetsList()
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, "Error cargando sets: ${resource.message}")
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })

        // NUEVO: Observar eliminación de sets por ejercicio
        setTemplateViewModel.deleteSetTemplatesByExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    currentSets.clear()
                    updateSetsList()
                    Toast.makeText(requireContext(), "Todos los sets eliminados", Toast.LENGTH_SHORT).show()
                    setTemplateViewModel.clearDeleteByExerciseState()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error al eliminar sets: ${resource.message}", Toast.LENGTH_SHORT).show()
                    setTemplateViewModel.clearDeleteByExerciseState()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })
    }

    private fun loadExistingSets() {
        setTemplateViewModel.getSetTemplatesByRoutineExercise(routineExerciseId)
    }

    private fun addSet() {
        val position = binding.etSetPosition.text.toString().toIntOrNull() ?: 1
        val setType = binding.spinnerSetType.selectedItem as String
        val restAfterSet = binding.etRestAfterSet.text.toString().toIntOrNull()
        val subSetNumber = binding.etSubSetNumber.text.toString().toIntOrNull()
        val groupId = binding.etGroupId.text.toString().takeIf { it.isNotEmpty() }

        // Crear parámetros básicos (repeticiones y peso por ahora)
        val parameters = mutableListOf<SetParameterRequest>()

        val reps = binding.etReps.text.toString().toIntOrNull()
        if (reps != null) {
            parameters.add(SetParameterRequest(
                parameterId = 1, // ID de repeticiones (ajustar según tu DB)
                integerValue = reps
            ))
        }

        val weight = binding.etWeight.text.toString().toDoubleOrNull()
        if (weight != null) {
            parameters.add(SetParameterRequest(
                parameterId = 2, // ID de peso (ajustar según tu DB)
                numericValue = weight
            ))
        }

        val request = CreateSetTemplateRequest(
            routineExerciseId = routineExerciseId,
            position = position,
            setType = setType,
            restAfterSet = restAfterSet,
            subSetNumber = subSetNumber,
            groupId = groupId,
            parameters = if (parameters.isNotEmpty()) parameters else null
        )

        Log.d(TAG, "Creando set: posición=$position, tipo=$setType, reps=$reps, weight=$weight")
        setTemplateViewModel.createSetTemplate(request)
    }

    private fun saveAllSets() {
        if (currentSets.isEmpty()) {
            Toast.makeText(requireContext(), "No hay sets para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "${currentSets.size} sets guardados", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun clearAllSets() {
        setTemplateViewModel.deleteSetTemplatesByRoutineExercise(routineExerciseId)
        // No limpiamos currentSets aquí, lo hacemos en el observer cuando sea exitoso
    }

    private fun showAdvancedParamsDialog() {
        // Implementar diálogo para parámetros avanzados
        Toast.makeText(requireContext(), "Parámetros avanzados - Próximamente", Toast.LENGTH_SHORT).show()
    }

    private fun updateSetsList() {
        val setsText = currentSets.joinToString("\n") { set ->
            "Set ${set.position}: ${set.setType} (Reps: ${getRepsFromSet(set)}, Peso: ${getWeightFromSet(set)})"
        }

        binding.tvSetsList.text = if (currentSets.isNotEmpty()) {
            setsText
        } else {
            "No hay sets configurados"
        }

        binding.tvSetCount.text = "Sets: ${currentSets.size}"
    }

    private fun getRepsFromSet(set: RoutineSetTemplateResponse): String {
        return set.parameters?.find { it.parameterName?.contains("rep", ignoreCase = true) == true }
            ?.integerValue?.toString() ?: "-"
    }

    private fun getWeightFromSet(set: RoutineSetTemplateResponse): String {
        return set.parameters?.find { it.parameterName?.contains("peso", ignoreCase = true) == true }
            ?.numericValue?.toString() ?: "-"
    }

    private fun clearForm() {
        binding.etSetPosition.text?.clear()
        binding.etReps.text?.clear()
        binding.etWeight.text?.clear()
        binding.etRestAfterSet.text?.clear()
        binding.etSubSetNumber.text?.clear()
        binding.etGroupId.text?.clear()
        binding.spinnerSetType.setSelection(0)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddSet.isEnabled = false
        binding.btnClearAll.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnAddSet.isEnabled = true
        binding.btnClearAll.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
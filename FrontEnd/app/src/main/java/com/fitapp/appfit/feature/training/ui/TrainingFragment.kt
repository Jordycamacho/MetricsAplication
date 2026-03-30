package com.fitapp.appfit.feature.training.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentTrainingBinding

class TrainingFragment : Fragment() {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Navegar a Rutinas
        binding.cardRoutines.setOnClickListener {
            findNavController().navigate(R.id.navigation_routines)
        }

        // Navegar a Deportes
        binding.cardSports.setOnClickListener {
            findNavController().navigate(R.id.navigation_sports)
        }

        // Navegar a Ejercicios
        binding.cardExercises.setOnClickListener {
            findNavController().navigate(R.id.navigation_exercises)
        }

        // Navegar a Parámetros
        binding.cardParameters.setOnClickListener {
            findNavController().navigate(R.id.navigation_exercise_params)
        }

        // Navegar a Marketplace (Paquetes)
        binding.cardMarketplace.setOnClickListener {
            findNavController().navigate(R.id.navigation_my_packages)
        }

        // Navegar a Importar/Exportar
        binding.cardImportExport.setOnClickListener {
            findNavController().navigate(R.id.navigation_import_export)
        }

        // Navegar a Categorías
        binding.cardCategories.setOnClickListener {
            findNavController().navigate(R.id.navigation_exercise_categories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.fitapp.appfit.ui.exercises.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.ItemExerciseBinding
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.utils.DateUtils  // Importar DateUtils

class ExerciseAdapter(
    private val onItemClick: (ExerciseResponse) -> Unit,
    private val onEditClick: (ExerciseResponse) -> Unit,
    private val onDeleteClick: (ExerciseResponse) -> Unit,
    private val onToggleStatusClick: (ExerciseResponse) -> Unit,
    private val isAdminMode: Boolean = false
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    companion object {
        private const val TAG = "ExerciseAdapter"
    }

    private var exercises = mutableListOf<ExerciseResponse>()

    fun setExercises(exercises: List<ExerciseResponse>) {
        Log.d(TAG, "setExercises: Actualizando lista con ${exercises.size} ejercicios")
        this.exercises.clear()
        this.exercises.addAll(exercises)
        notifyDataSetChanged()
    }

    fun addExercises(newExercises: List<ExerciseResponse>) {
        Log.d(TAG, "addExercises: Agregando ${newExercises.size} ejercicios")
        val startPosition = exercises.size
        exercises.addAll(newExercises)
        notifyItemRangeInserted(startPosition, newExercises.size)
    }

    fun getExercises(): List<ExerciseResponse> {
        return exercises.toList()
    }

    fun clearExercises() {
        Log.d(TAG, "clearExercises: Limpiando lista")
        exercises.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        Log.d(TAG, "onCreateViewHolder: Creando view holder")
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        Log.v(TAG, "onBindViewHolder: Binding ejercicio posición $position")
        holder.bind(exercises[position])
    }

    override fun getItemCount(): Int = exercises.size

    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ExerciseResponse) {
            try {
                Log.d(TAG, "bind: Mostrando ejercicio ${exercise.id} - ${exercise.name}")

                // Configurar datos básicos
                binding.tvExerciseName.text = exercise.name
                binding.tvExerciseType.text = exercise.exerciseType?.name ?: "SIN TIPO"
                binding.tvExerciseDescription.text = exercise.description ?: "Sin descripción"
                binding.tvExerciseSport.text = exercise.sportName ?: "Sin deporte"
                binding.tvExerciseCreatedBy.text = exercise.createdByEmail ?: "Usuario desconocido"

                // Configurar categorías
                val categoriesText = if (exercise.categoryNames.isNullOrEmpty()) {
                    "Sin categorías"
                } else {
                    exercise.categoryNames.joinToString(", ")
                }
                binding.tvExerciseCategories.text = "Categorías: $categoriesText"

                // Configurar parámetros
                val paramsText = if (exercise.supportedParameterNames.isNullOrEmpty()) {
                    "Sin parámetros"
                } else {
                    "Parámetros: ${exercise.supportedParameterNames.size}"
                }
                binding.tvExerciseParameters.text = paramsText

                // Configurar visibilidad
                if (exercise.isPublic == true) {
                    binding.tvExerciseVisibility.text = "👥 PÚBLICO"
                    binding.tvExerciseVisibility.setBackgroundResource(R.drawable.badge_custom)
                } else {
                    binding.tvExerciseVisibility.text = "👤 PERSONAL"
                    binding.tvExerciseVisibility.setBackgroundResource(R.drawable.badge_predefined)
                }

                // Configurar estado activo
                if (exercise.isActive == true) {
                    binding.tvExerciseStatus.text = "✅ ACTIVO"
                    binding.btnToggleStatus.text = "DESACTIVAR"
                    binding.tvExerciseStatus.setBackgroundResource(R.drawable.badge_active)
                } else {
                    binding.tvExerciseStatus.text = "❌ INACTIVO"
                    binding.btnToggleStatus.text = "ACTIVAR"
                    binding.tvExerciseStatus.setBackgroundResource(R.drawable.badge_inactive)
                }

                // Configurar estadísticas
                binding.tvExerciseUsageCount.text = "Usado ${exercise.usageCount ?: 0} veces"
                binding.tvExerciseRating.text = "⭐ ${exercise.rating ?: 0.0}"

                // CORRECCIÓN PRINCIPAL: Usar DateUtils para formatear la fecha
                if (!exercise.lastUsedAt.isNullOrEmpty()) {
                    val formattedDate = DateUtils.formatForDisplay(exercise.lastUsedAt)
                    binding.tvExerciseLastUsed.text = "Último uso: $formattedDate"
                    binding.tvExerciseLastUsed.visibility = View.VISIBLE
                } else {
                    binding.tvExerciseLastUsed.text = "Último uso: Nunca"
                    binding.tvExerciseLastUsed.visibility = View.VISIBLE
                }

                // Configurar botones según permisos
                val isPersonalExercise = exercise.isPublic == false
                val canEdit = isPersonalExercise || isAdminMode
                val canDelete = isPersonalExercise || isAdminMode

                if (canEdit) {
                    binding.btnEditExercise.visibility = View.VISIBLE
                } else {
                    binding.btnEditExercise.visibility = View.GONE
                }

                if (canDelete) {
                    binding.btnDeleteExercise.visibility = View.VISIBLE
                } else {
                    binding.btnDeleteExercise.visibility = View.GONE
                }

                // Listeners
                binding.root.setOnClickListener {
                    Log.d(TAG, "bind: Click en ejercicio ${exercise.id}")
                    onItemClick(exercise)
                }

                binding.btnEditExercise.setOnClickListener {
                    Log.d(TAG, "bind: Editar ejercicio ${exercise.id}")
                    onEditClick(exercise)
                }

                binding.btnDeleteExercise.setOnClickListener {
                    Log.d(TAG, "bind: Eliminar ejercicio ${exercise.id}")
                    onDeleteClick(exercise)
                }

                binding.btnToggleStatus.setOnClickListener {
                    Log.d(TAG, "bind: Cambiar estado ejercicio ${exercise.id}")
                    onToggleStatusClick(exercise)
                }

                // Solo admin puede hacer ejercicios públicos
                if (isAdminMode && exercise.isPublic == false) {
                    binding.btnMakePublic.visibility = View.VISIBLE
                    binding.btnMakePublic.setOnClickListener {
                        Log.d(TAG, "bind: Hacer público ejercicio ${exercise.id}")
                        // Aquí puedes agregar lógica adicional si necesitas
                    }
                } else {
                    binding.btnMakePublic.visibility = View.GONE
                }

            } catch (e: Exception) {
                Log.e(TAG, "bind error: ${e.message}", e)
                binding.tvExerciseName.text = "Error al cargar ejercicio"
            }
        }
    }
}
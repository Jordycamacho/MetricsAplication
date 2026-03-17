package com.fitapp.appfit.ui.routines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemRoutineExerciseBinding
import com.fitapp.appfit.databinding.ItemRoutineGroupHeaderBinding
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse

class RoutineExerciseAdapter(
    private val onEditClick: (RoutineExerciseResponse) -> Unit,
    private val onDeleteClick: (RoutineExerciseResponse) -> Unit,
    private val onAddSetClick: (RoutineExerciseResponse) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EXERCISE = 1

        private val DAY_ORDER = mapOf(
            "MONDAY" to 1, "TUESDAY" to 2, "WEDNESDAY" to 3,
            "THURSDAY" to 4, "FRIDAY" to 5, "SATURDAY" to 6, "SUNDAY" to 7
        )
        private val DAY_NAMES = mapOf(
            "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
            "THURSDAY" to "Jueves", "FRIDAY" to "Viernes",
            "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
        )
    }

    private val items = mutableListOf<ListItem>()

    sealed class ListItem {
        data class Header(val title: String, val count: Int) : ListItem()
        data class Exercise(val data: RoutineExerciseResponse) : ListItem()
    }

    fun submitList(exercises: List<RoutineExerciseResponse>) {
        items.clear()

        val hasDays = exercises.any { it.dayOfWeek != null }

        if (hasDays) {
            val grouped = exercises
                .groupBy { it.dayOfWeek ?: "SIN_DÍA" }
                .entries
                .sortedBy { DAY_ORDER[it.key] ?: 99 }

            grouped.forEach { (day, list) ->
                val title = DAY_NAMES[day] ?: day
                items.add(ListItem.Header(title, list.size))
                val sorted = list.sortedBy { it.sessionOrder ?: it.position }
                sorted.forEach { items.add(ListItem.Exercise(it)) }
            }
        } else {
            val grouped = exercises
                .groupBy { it.sessionNumber ?: 0 }
                .entries
                .sortedBy { it.key }

            grouped.forEach { (session, list) ->
                val title = if (session == 0) "Sin sesión" else "Sesión $session"
                items.add(ListItem.Header(title, list.size))
                val sorted = list.sortedBy { it.sessionOrder ?: it.position }
                sorted.forEach { items.add(ListItem.Exercise(it)) }
            }
        }

        notifyDataSetChanged()
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ListItem.Header -> TYPE_HEADER
        is ListItem.Exercise -> TYPE_EXERCISE
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemRoutineGroupHeaderBinding.inflate(inflater, parent, false)
            )
            else -> ExerciseViewHolder(
                ItemRoutineExerciseBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.Exercise -> (holder as ExerciseViewHolder).bind(item.data)
        }
    }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    inner class HeaderViewHolder(
        private val binding: ItemRoutineGroupHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: ListItem.Header) {
            binding.tvGroupTitle.text = header.title
            binding.tvGroupCount.text = "${header.count} ejercicio${if (header.count != 1) "s" else ""}"
        }
    }

    inner class ExerciseViewHolder(
        private val binding: ItemRoutineExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: RoutineExerciseResponse) {
            binding.tvExerciseName.text = exercise.exerciseName ?: "Ejercicio"

            binding.tvExerciseDetails.text = buildString {
                exercise.sessionNumber?.let { append("Sesión $it") }
                if (exercise.sessionOrder != null) {
                    if (isNotEmpty()) append(" · ")
                    append("Orden ${exercise.sessionOrder}")
                }
                if (exercise.restAfterExercise != null) {
                    if (isNotEmpty()) append(" · ")
                    append("${exercise.restAfterExercise}s descanso")
                }
            }

            val setsCount = exercise.sets ?: 0
            binding.tvSetsInfo.text = if (setsCount == 0) {
                "Sin sets"
            } else {
                "$setsCount set${if (setsCount != 1) "s" else ""}"
            }

            binding.btnEdit.setOnClickListener { onEditClick(exercise) }
            binding.btnDelete.setOnClickListener { onDeleteClick(exercise) }
            binding.btnAddSet.setOnClickListener { onAddSetClick(exercise) }
        }
    }
}
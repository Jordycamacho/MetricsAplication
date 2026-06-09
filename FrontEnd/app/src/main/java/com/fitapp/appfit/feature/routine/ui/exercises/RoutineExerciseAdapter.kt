package com.fitapp.appfit.feature.routine.ui.exercises

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemRoutineExerciseBinding
import com.fitapp.appfit.databinding.ItemRoutineGroupHeaderBinding
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.ReorderSessionExercisesRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

    data class GroupKey(
        val dayOfWeek: String?,
        val sessionNumber: Int?,
        val groupByDay: Boolean
    )

    sealed class ListItem {
        data class Header(val title: String, val count: Int, val groupKey: GroupKey) : ListItem()
        data class Exercise(val data: RoutineExerciseResponse, val groupKey: GroupKey) : ListItem()
    }

    private val items = mutableListOf<ListItem>()
    private var snapshotItems = listOf<ListItem>()
    private var reorderMode = false
    private var groupByDay = true
    private var itemTouchHelper: ItemTouchHelper? = null

    fun submitList(exercises: List<RoutineExerciseResponse>) {
        items.clear()
        groupByDay = exercises.any { it.dayOfWeek != null }

        if (groupByDay) {
            exercises.groupBy { it.dayOfWeek ?: "SIN_DÍA" }
                .entries
                .sortedBy { DAY_ORDER[it.key] ?: 99 }
                .forEach { (day, list) ->
                    val key = GroupKey(dayOfWeek = day, sessionNumber = list.firstOrNull()?.sessionNumber, groupByDay = true)
                    val title = if (day == "SIN_DÍA") "Sin día" else (DAY_NAMES[day] ?: day)
                    items.add(ListItem.Header(title, list.size, key))
                    list.sortedBy { it.sessionOrder ?: it.position }
                        .forEach { items.add(ListItem.Exercise(it, key)) }
                }
        } else {
            exercises.groupBy { it.sessionNumber ?: 0 }
                .entries
                .sortedBy { it.key }
                .forEach { (session, list) ->
                    val key = GroupKey(dayOfWeek = null, sessionNumber = session, groupByDay = false)
                    val title = if (session == 0) "Sin sesión" else "Sesión $session"
                    items.add(ListItem.Header(title, list.size, key))
                    list.sortedBy { it.sessionOrder ?: it.position }
                        .forEach { items.add(ListItem.Exercise(it, key)) }
                }
        }
        notifyDataSetChanged()
    }

    fun setReorderMode(enabled: Boolean) {
        if (enabled) snapshotItems = items.toList()
        reorderMode = enabled
        notifyDataSetChanged()
    }

    fun isReorderMode() = reorderMode

    fun restoreSnapshot() {
        items.clear()
        items.addAll(snapshotItems)
        reorderMode = false
        notifyDataSetChanged()
    }

    fun buildSessionReorderRequests(): List<ReorderSessionExercisesRequest> {
        val requests = mutableListOf<ReorderSessionExercisesRequest>()
        var currentKey: GroupKey? = null
        val idsInGroup = mutableListOf<Long>()

        fun flush() {
            val key = currentKey ?: return
            if (idsInGroup.isEmpty()) return
            requests.add(key.toRequest(idsInGroup.toList()))
            idsInGroup.clear()
        }

        items.forEach { item ->
            when (item) {
                is ListItem.Header -> {
                    flush()
                    currentKey = item.groupKey
                }
                is ListItem.Exercise -> idsInGroup.add(item.data.id)
            }
        }
        flush()
        return requests
    }

    fun attachItemTouchHelper(recyclerView: RecyclerView) {
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun isLongPressDragEnabled() = false

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (!reorderMode) return false
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
                return moveItem(from, to)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
        })
        helper.attachToRecyclerView(recyclerView)
        itemTouchHelper = helper
    }

    fun moveItem(from: Int, to: Int): Boolean {
        if (from == to) return false
        val fromItem = items.getOrNull(from) as? ListItem.Exercise ?: return false
        val toItem = items.getOrNull(to) as? ListItem.Exercise ?: return false
        if (fromItem.groupKey != toItem.groupKey) return false

        val moved = items.removeAt(from)
        items.add(to, moved)
        notifyItemMoved(from, to)
        refreshOrderBadgesInGroup(fromItem.groupKey)
        return true
    }

    fun showMoveToPositionDialog(
        exercise: RoutineExerciseResponse,
        groupKey: GroupKey,
        anchor: View
    ) {
        val groupExercises = items.filterIsInstance<ListItem.Exercise>()
            .filter { it.groupKey == groupKey }
        val maxPos = groupExercises.size
        val currentPos = groupExercises.indexOfFirst { it.data.id == exercise.id } + 1
        if (maxPos <= 1) return

        val input = android.widget.EditText(anchor.context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(currentPos.toString())
            setSelection(text.length)
            setPadding(48, 32, 48, 16)
        }

        MaterialAlertDialogBuilder(anchor.context)
            .setTitle("Mover a posición")
            .setMessage("Posición actual: #$currentPos (1–$maxPos)")
            .setView(input)
            .setPositiveButton("Mover") { _, _ ->
                val target = input.text.toString().toIntOrNull() ?: return@setPositiveButton
                if (target !in 1..maxPos) return@setPositiveButton
                moveExerciseToPosition(exercise.id, groupKey, target)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun moveExerciseToPosition(exerciseId: Long, groupKey: GroupKey, targetPosition: Int) {
        val ordered = items.filterIsInstance<ListItem.Exercise>()
            .filter { it.groupKey == groupKey }
            .map { it.data }
            .toMutableList()
        val fromIdx = ordered.indexOfFirst { it.id == exerciseId }
        if (fromIdx < 0) return
        val toIdx = targetPosition - 1
        if (toIdx !in ordered.indices || fromIdx == toIdx) return

        val moved = ordered.removeAt(fromIdx)
        ordered.add(toIdx, moved)
        replaceGroupExercises(groupKey, ordered)
        notifyDataSetChanged()
    }

    private fun replaceGroupExercises(groupKey: GroupKey, ordered: List<RoutineExerciseResponse>) {
        val headerIndex = items.indexOfFirst {
            it is ListItem.Header && it.groupKey == groupKey
        }
        if (headerIndex < 0) return

        var end = headerIndex + 1
        while (end < items.size && items[end] is ListItem.Exercise) {
            end++
        }
        for (i in end - 1 downTo headerIndex + 1) {
            items.removeAt(i)
        }
        ordered.forEachIndexed { index, exercise ->
            items.add(headerIndex + 1 + index, ListItem.Exercise(exercise, groupKey))
        }
    }

    private fun refreshOrderBadgesInGroup(groupKey: GroupKey) {
        var order = 1
        items.forEachIndexed { index, item ->
            if (item is ListItem.Exercise && item.groupKey == groupKey) {
                notifyItemChanged(index)
                order++
            }
        }
    }

    private fun orderInGroup(exerciseId: Long, groupKey: GroupKey): Int {
        var order = 0
        items.forEach { item ->
            if (item is ListItem.Exercise && item.groupKey == groupKey) {
                order++
                if (item.data.id == exerciseId) return order
            }
        }
        return order
    }

    private fun GroupKey.toRequest(ids: List<Long>): ReorderSessionExercisesRequest {
        return if (groupByDay && dayOfWeek != null && dayOfWeek != "SIN_DÍA") {
            ReorderSessionExercisesRequest(dayOfWeek = dayOfWeek, exerciseIds = ids)
        } else {
            ReorderSessionExercisesRequest(
                sessionNumber = sessionNumber?.takeIf { it > 0 } ?: 1,
                exerciseIds = ids
            )
        }
    }

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
            is ListItem.Exercise -> (holder as ExerciseViewHolder).bind(item.data, item.groupKey)
        }
    }

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

        fun bind(exercise: RoutineExerciseResponse, groupKey: GroupKey) {
            val order = orderInGroup(exercise.id, groupKey)
            binding.tvOrderBadge.text = "#$order"
            binding.tvOrderBadge.visibility = View.VISIBLE
            binding.tvExerciseName.text = exercise.exerciseName ?: "Ejercicio"

            binding.tvExerciseDetails.text = buildString {
                if (!groupByDay) {
                    exercise.sessionNumber?.let { append("Sesión $it") }
                }
                if (exercise.restAfterExercise != null) {
                    if (isNotEmpty()) append(" · ")
                    append("${exercise.restAfterExercise}s descanso")
                }
            }

            val setsCount = exercise.sets ?: 0
            binding.tvSetsInfo.text = if (setsCount == 0) "Sin sets" else "$setsCount set${if (setsCount != 1) "s" else ""}"

            val reordering = reorderMode
            binding.ivDragHandle.visibility = if (reordering) View.VISIBLE else View.GONE
            binding.btnMovePosition.visibility = if (reordering) View.VISIBLE else View.GONE
            binding.btnEdit.visibility = if (reordering) View.GONE else View.VISIBLE
            binding.btnDelete.visibility = if (reordering) View.GONE else View.VISIBLE
            binding.btnAddSet.visibility = if (reordering) View.GONE else View.VISIBLE

            binding.btnEdit.setOnClickListener { onEditClick(exercise) }
            binding.btnDelete.setOnClickListener { onDeleteClick(exercise) }
            binding.btnAddSet.setOnClickListener { onAddSetClick(exercise) }
            binding.btnMovePosition.setOnClickListener {
                showMoveToPositionDialog(exercise, groupKey, binding.root)
            }
            binding.ivDragHandle.setOnTouchListener { _, event ->
                if (reordering && event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper?.startDrag(this@ExerciseViewHolder)
                }
                false
            }
        }
    }
}

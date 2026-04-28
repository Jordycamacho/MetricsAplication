package com.fitapp.appfit.feature.workout.presentation.execution

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.SetExecutionParameterResponse
import com.fitapp.appfit.feature.workout.model.response.SetExecutionResponse

class SessionExerciseAdapter :
    ListAdapter<SessionExerciseResponse, SessionExerciseAdapter.ExerciseViewHolder>(DiffCallback()) {

    companion object { private const val TAG = "SessionExerciseAdapter" }

    private var previousById: Map<Long, SessionExerciseResponse> = emptyMap()

    /**
     * Called when previous-session exercises arrive (async, after the current list is already shown).
     * Forces a full rebind of every visible item so the comparison columns populate.
     */
    fun setPreviousExercises(previous: List<SessionExerciseResponse>) {
        previousById = previous.associateBy { it.exerciseId ?: -1L }
        // notifyDataSetChanged forces every ViewHolder to rebind even if DiffCallback
        // thinks the items haven't changed (they haven't — but the previous data has).
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, previousById[item.exerciseId])
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val tvSetCountBadge: TextView = itemView.findViewById(R.id.tv_set_count_badge)
        private val rvSets: RecyclerView = itemView.findViewById(R.id.rv_sets)
        private val setsAdapter = SessionSetsAdapter()

        init {
            rvSets.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = setsAdapter
                isNestedScrollingEnabled = false
            }
        }

        fun bind(exercise: SessionExerciseResponse, previous: SessionExerciseResponse?) {
            Log.d(TAG, "BIND_EXERCISE | id=${exercise.id} | name=${exercise.exerciseName} | hasPrevious=${previous != null}")

            tvExerciseName.text = when {
                !exercise.exerciseName.isNullOrBlank() -> exercise.exerciseName
                exercise.exerciseId != null -> "Ejercicio #${exercise.exerciseId}"
                else -> "Ejercicio"
            }

            val setCount = exercise.sets?.size ?: 0
            tvSetCountBadge.text = "$setCount set${if (setCount != 1) "s" else ""}"

            val currentSets = exercise.sets?.sortedBy { it.position } ?: emptyList()
            val prevByPosition = previous?.sets?.associateBy { it.position } ?: emptyMap()
            val pairs = currentSets.map { curr -> curr to prevByPosition[curr.position] }

            Log.d(TAG, "SUBMITTING_SETS | count=${pairs.size} | prevSets=${prevByPosition.size}")
            setsAdapter.submitPairs(pairs)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SessionExerciseResponse>() {
        override fun areItemsTheSame(old: SessionExerciseResponse, new: SessionExerciseResponse) =
            old.id == new.id
        override fun areContentsTheSame(old: SessionExerciseResponse, new: SessionExerciseResponse) =
            old == new
    }
}

// ─────────────────────────────────────────────────────────────────────────────

class SessionSetsAdapter : RecyclerView.Adapter<SessionSetsAdapter.SetViewHolder>() {

    companion object { private const val TAG = "SessionSetsAdapter" }

    private var pairs: List<Pair<SetExecutionResponse, SetExecutionResponse?>> = emptyList()

    fun submitPairs(newPairs: List<Pair<SetExecutionResponse, SetExecutionResponse?>>) {
        pairs = newPairs
        notifyDataSetChanged()
    }

    override fun getItemCount() = pairs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val (current, previous) = pairs[position]
        holder.bind(current, previous, position + 1)
    }

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvSetNumber: TextView = itemView.findViewById(R.id.tv_set_number)
        private val tvDelta: TextView = itemView.findViewById(R.id.tv_delta)
        private val tvPrevMain: TextView = itemView.findViewById(R.id.tv_prev_main)
        private val tvPrevSub: TextView = itemView.findViewById(R.id.tv_prev_sub)
        private val tvCurrMain: TextView = itemView.findViewById(R.id.tv_curr_main)
        private val tvCurrSub: TextView = itemView.findViewById(R.id.tv_curr_sub)
        private val tvSetPr: TextView = itemView.findViewById(R.id.tv_set_pr)

        fun bind(current: SetExecutionResponse, previous: SetExecutionResponse?, setNumber: Int) {
            Log.d(TAG, "BIND_SET | setNumber=$setNumber | params=${current.parameters?.size} | hasPrev=${previous != null}")

            tvSetNumber.text = "S$setNumber"

            val currValues = extractValues(current.parameters)
            val prevValues = previous?.let { extractValues(it.parameters) }

            // Current — right side
            tvCurrMain.text = currValues.mainLabel
            tvCurrSub.text = currValues.subLabel ?: ""
            tvCurrSub.isVisible = currValues.subLabel != null
            tvSetPr.isVisible = current.parameters?.any { it.isPersonalRecord == true } == true

            // Previous — left side
            if (prevValues != null) {
                tvPrevMain.text = prevValues.mainLabel
                tvPrevSub.text = prevValues.subLabel ?: ""
                tvPrevSub.isVisible = prevValues.subLabel != null

                val delta = computeDelta(currValues, prevValues)
                tvDelta.isVisible = true
                when {
                    delta > 0 -> {
                        tvDelta.text = "\u2191"
                        tvDelta.setTextColor(itemView.context.getColor(R.color.set_completed_green))
                    }
                    delta < 0 -> {
                        tvDelta.text = "\u2193"
                        tvDelta.setTextColor(itemView.context.getColor(R.color.red))
                    }
                    else -> {
                        tvDelta.text = "="
                        tvDelta.setTextColor(itemView.context.getColor(R.color.text_secondary_dark))
                    }
                }
            } else {
                tvPrevMain.text = "—"
                tvPrevSub.isVisible = false
                tvDelta.isVisible = false
            }
        }

        private fun computeDelta(curr: SetValues, prev: SetValues): Double {
            val cw = curr.weight; val pw = prev.weight
            val cr = curr.reps;  val pr = prev.reps
            return when {
                cw != null && pw != null -> {
                    val wd = cw - pw
                    if (wd != 0.0) wd else ((cr ?: 0) - (pr ?: 0)).toDouble()
                }
                cr != null && pr != null -> (cr - pr).toDouble()
                else -> 0.0
            }
        }
    }

    private fun extractValues(params: List<SetExecutionParameterResponse>?): SetValues {
        if (params.isNullOrEmpty()) return SetValues(null, null, null, "—", null)

        val weightParam = params.find { it.parameterType?.uppercase() in listOf("NUMBER", "WEIGHT") }
        val durationParam = params.find { it.parameterType?.uppercase() == "DURATION" }
        val intParam = params.find { it.parameterType?.uppercase() == "INTEGER" }

        val weight = weightParam?.numericValue
        val reps = weightParam?.integerValue ?: intParam?.integerValue
        val duration = durationParam?.durationValue

        return when {
            weight != null && reps != null -> SetValues(weight, reps, null,
                "${fmtWeight(weight)} kg", "$reps reps")
            weight != null -> SetValues(weight, null, null, "${fmtWeight(weight)} kg", null)
            reps != null   -> SetValues(null, reps, null, "$reps reps", null)
            duration != null -> SetValues(null, null, duration, fmtDuration(duration), null)
            else -> SetValues(null, null, null, "—", null)
        }
    }

    private fun fmtWeight(w: Double) =
        if (w % 1.0 == 0.0) w.toInt().toString() else "%.1f".format(w)

    private fun fmtDuration(s: Long): String {
        val m = s / 60; val sec = s % 60
        return if (m > 0) "${m}m ${sec}s" else "${sec}s"
    }

    data class SetValues(
        val weight: Double?, val reps: Int?, val duration: Long?,
        val mainLabel: String, val subLabel: String?
    )
}

fun calculateSetVolume(set: SetExecutionResponse): Double {
    val param = set.parameters?.find {
        it.parameterType?.uppercase() in listOf("NUMBER", "WEIGHT")
    } ?: return 0.0
    val weight = param.numericValue ?: return 0.0
    val reps = param.integerValue ?: return 0.0
    return weight * reps
}
package com.fitapp.appfit.feature.workout.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.SetExecutionResponse

class SessionExerciseAdapter : ListAdapter<SessionExerciseResponse, SessionExerciseAdapter.ExerciseViewHolder>(DiffCallback()) {

    companion object {
        private const val TAG = "SessionExerciseAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val tvExerciseVolume: TextView = itemView.findViewById(R.id.tv_exercise_volume)
        private val rvSets: RecyclerView = itemView.findViewById(R.id.rv_sets)

        private val setsAdapter = SessionSetsAdapter()

        init {
            rvSets.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = setsAdapter
                isNestedScrollingEnabled = false
            }
        }

        fun bind(exercise: SessionExerciseResponse) {
            Log.d(TAG, "BIND_EXERCISE | id=${exercise.id} | name=${exercise.exerciseName} | sets=${exercise.sets?.size}")

            tvExerciseName.text = exercise.exerciseName ?: "Ejercicio #${exercise.exerciseId}"

            val totalVolume = exercise.sets?.sumOf { set ->
                val weight = set.parameters?.find {
                    it.parameterType?.uppercase() in listOf("NUMBER", "WEIGHT")
                }?.numericValue ?: 0.0

                val reps = set.parameters?.find {
                    it.parameterType?.uppercase() == "REPETITIONS"
                }?.integerValue ?: 0

                weight * reps
            } ?: 0.0

            tvExerciseVolume.text = if (totalVolume > 0) {
                String.format("%.1f kg", totalVolume)
            } else {
                "0.0 kg"
            }

            exercise.sets?.let {
                Log.d(TAG, "SUBMITTING_SETS | count=${it.size}")
                setsAdapter.submitList(it)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SessionExerciseResponse>() {
        override fun areItemsTheSame(
            oldItem: SessionExerciseResponse,
            newItem: SessionExerciseResponse
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SessionExerciseResponse,
            newItem: SessionExerciseResponse
        ): Boolean = oldItem == newItem
    }
}

class SessionSetsAdapter : ListAdapter<SetExecutionResponse, SessionSetsAdapter.SetViewHolder>(SetDiffCallback()) {

    companion object {
        private const val TAG = "SessionSetsAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvSetNumber: TextView = itemView.findViewById(R.id.tv_set_number)
        private val tvSetDetails: TextView = itemView.findViewById(R.id.tv_set_details)
        private val tvSetVolume: TextView = itemView.findViewById(R.id.tv_set_volume)
        private val tvSetPR: TextView = itemView.findViewById(R.id.tv_set_pr)

        fun bind(set: SetExecutionResponse, setNumber: Int) {
            Log.d(TAG, "BIND_SET | setNumber=$setNumber | params=${set.parameters?.size}")

            tvSetNumber.text = "Set $setNumber"

            val details = buildSetDetails(set)
            tvSetDetails.text = details

            val weight = set.parameters?.find {
                it.parameterType?.uppercase() in listOf("NUMBER", "WEIGHT")
            }?.numericValue ?: 0.0

            val reps = set.parameters?.find {
                it.parameterType?.uppercase() == "REPETITIONS"
            }?.integerValue ?: 0

            val volume = weight * reps

            if (volume > 0) {
                tvSetVolume.visibility = View.VISIBLE
                tvSetVolume.text = String.format("%.1f kg", volume)
            } else {
                tvSetVolume.visibility = View.GONE
            }

            val hasPersonalRecord = set.parameters?.any { it.isPersonalRecord == true } == true
            tvSetPR.visibility = if (hasPersonalRecord) View.VISIBLE else View.GONE
        }

        private fun buildSetDetails(set: SetExecutionResponse): String {
            val params = set.parameters

            if (params.isNullOrEmpty()) {
                Log.w(TAG, "NO_PARAMS_FOUND")
                return "--"
            }

            Log.d(TAG, "BUILD_DETAILS | params=${params.map { "${it.parameterName}=${it.numericValue ?: it.integerValue ?: it.durationValue}" }}")

            val reps = params.find { it.parameterType?.uppercase() == "REPETITIONS" }?.integerValue
            val weight = params.find {
                it.parameterType?.uppercase() in listOf("NUMBER", "WEIGHT")
            }?.numericValue

            val duration = params.find { it.parameterType?.uppercase() == "DURATION" }?.durationValue

            return when {
                reps != null && weight != null -> {
                    "$reps reps × ${String.format("%.1f", weight)}kg"
                }
                duration != null -> {
                    formatDuration(duration)
                }
                reps != null -> {
                    "$reps reps"
                }
                weight != null -> {
                    "${String.format("%.1f", weight)}kg"
                }
                else -> {
                    "--"
                }
            }
        }

        private fun formatDuration(seconds: Long): String {
            val m = seconds / 60
            val s = seconds % 60
            return if (m > 0) "${m}m ${s}s" else "${s}s"
        }
    }

    private class SetDiffCallback : DiffUtil.ItemCallback<SetExecutionResponse>() {
        override fun areItemsTheSame(
            oldItem: SetExecutionResponse,
            newItem: SetExecutionResponse
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SetExecutionResponse,
            newItem: SetExecutionResponse
        ): Boolean = oldItem == newItem
    }
}
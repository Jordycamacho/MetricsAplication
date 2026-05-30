package com.fitapp.appfit.feature.workout.presentation.execution

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.util.WorkoutParameterHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class WorkoutNumericValueBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "WorkoutNumericValueBottomSheet"

        private val pendingCallbacks = mutableMapOf<String, (Double) -> Unit>()

        fun show(
            fragmentManager: FragmentManager,
            param: RoutineSetParameterResponse,
            currentValue: Double,
            onConfirm: (Double) -> Unit
        ) {
            if (fragmentManager.isStateSaved) return

            val callbackKey = "${param.parameterId}_${System.nanoTime()}"
            pendingCallbacks[callbackKey] = onConfirm

            (fragmentManager.findFragmentByTag(TAG) as? WorkoutNumericValueBottomSheet)
                ?.dismissAllowingStateLoss()

            WorkoutNumericValueBottomSheet().apply {
                arguments = bundleOf(
                    ARG_CALLBACK_KEY to callbackKey,
                    ARG_NAME to param.parameterName,
                    ARG_TYPE to (param.parameterType ?: "NUMBER"),
                    ARG_UNIT to param.unit,
                    ARG_VALUE to currentValue
                )
            }.show(fragmentManager, TAG)
        }

        private const val ARG_CALLBACK_KEY = "callback_key"
        private const val ARG_NAME = "param_name"
        private const val ARG_TYPE = "param_type"
        private const val ARG_UNIT = "param_unit"
        private const val ARG_VALUE = "current_value"
    }

    override fun getTheme(): Int = R.style.DarkBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_numeric_value, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paramName = requireArguments().getString(ARG_NAME).orEmpty()
        val paramType = requireArguments().getString(ARG_TYPE).orEmpty()
        val paramUnit = requireArguments().getString(ARG_UNIT)
        val currentValue = requireArguments().getDouble(ARG_VALUE)

        val param = RoutineSetParameterResponse(
            id = 0L,
            setTemplateId = 0L,
            parameterId = 0L,
            parameterName = paramName,
            parameterType = paramType,
            unit = paramUnit,
            repetitions = null,
            numericValue = currentValue,
            durationValue = null,
            integerValue = null
        )

        view.findViewById<TextView>(R.id.bs_numeric_param_name).text =
            paramName.ifEmpty { "Valor" }
        view.findViewById<TextView>(R.id.bs_numeric_type).text = typeLabel(paramType)

        val unitRow = view.findViewById<LinearLayout>(R.id.bs_row_numeric_unit)
        val unitView = view.findViewById<TextView>(R.id.bs_numeric_unit)
        val displayUnit = WorkoutParameterHelper.displayUnit(param)
        if (displayUnit != "—") {
            unitView.text = displayUnit
            unitRow.isVisible = true
        } else {
            unitRow.isVisible = false
        }

        val input = view.findViewById<TextInputEditText>(R.id.bs_numeric_input)
        input.setText(WorkoutParameterHelper.formatNumericValue(currentValue, param))
        input.inputType = if (WorkoutParameterHelper.isIntegerInput(param)) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        } else {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        input.post {
            input.requestFocus()
            context?.getSystemService<InputMethodManager>()
                ?.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }

        view.findViewById<View>(R.id.bs_numeric_btn_confirm).setOnClickListener {
            val parsed = WorkoutParameterHelper.parseNumericInput(
                input.text?.toString().orEmpty(),
                param
            ) ?: return@setOnClickListener
            val callbackKey = requireArguments().getString(ARG_CALLBACK_KEY).orEmpty()
            pendingCallbacks.remove(callbackKey)?.invoke(parsed)
            dismiss()
        }
    }

    private fun typeLabel(type: String): String = when (type.uppercase()) {
        "NUMBER" -> "Número decimal"
        "INTEGER" -> "Número entero"
        "DISTANCE" -> "Distancia"
        "PERCENTAGE" -> "Porcentaje"
        else -> type.ifEmpty { "Valor" }
    }
}

package com.fitapp.appfit.feature.parameter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ParameterDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var parameter: CustomParameterResponse

    companion object {
        fun newInstance(parameter: CustomParameterResponse): ParameterDetailBottomSheet {
            return ParameterDetailBottomSheet().apply {
                this.parameter = parameter
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(
            R.layout.bottom_sheet_parameter_detail,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.bs_param_name).text = parameter.name
        view.findViewById<TextView>(R.id.bs_param_type).text = getTypeLabel(parameter.parameterType)

        val descView = view.findViewById<TextView>(R.id.bs_param_description)
        val descRow = view.findViewById<LinearLayout>(R.id.bs_row_description)
        if (!parameter.description.isNullOrEmpty()) {
            descView.text = parameter.description
            descRow.visibility = View.VISIBLE
        } else {
            descRow.visibility = View.GONE
        }

        val unitView = view.findViewById<TextView>(R.id.bs_param_unit)
        val unitRow = view.findViewById<LinearLayout>(R.id.bs_row_unit)
        if (!parameter.unit.isNullOrEmpty()) {
            unitView.text = parameter.unit
            unitRow.visibility = View.VISIBLE
        } else {
            unitRow.visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.bs_param_visibility).text =
            when {
                parameter.isGlobal && parameter.ownerId == null -> "Sistema"
                parameter.isGlobal -> "Global"
                else -> "Personal"
            }

        view.findViewById<TextView>(R.id.bs_param_status).text =
            if (parameter.isActive) "Activo" else "Inactivo"

        val ownerRow = view.findViewById<LinearLayout>(R.id.bs_row_owner)
        val ownerView = view.findViewById<TextView>(R.id.bs_param_owner)
        if (!parameter.ownerName.isNullOrEmpty()) {
            ownerView.text = parameter.ownerName
            ownerRow.visibility = View.VISIBLE
        } else {
            ownerRow.visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.bs_param_usage).text = "${parameter.usageCount} usos"

        view.findViewById<View>(R.id.bs_btn_close).setOnClickListener {
            dismiss()
        }
    }

    private fun getTypeLabel(type: String): String = when (type.uppercase()) {
        "NUMBER" -> "Número decimal"
        "INTEGER" -> "Número entero"
        "TEXT" -> "Texto"
        "BOOLEAN" -> "Sí / No"
        "DURATION" -> "Tiempo / Duración"
        "DISTANCE" -> "Distancia"
        "PERCENTAGE" -> "Porcentaje"
        else -> type
    }
}
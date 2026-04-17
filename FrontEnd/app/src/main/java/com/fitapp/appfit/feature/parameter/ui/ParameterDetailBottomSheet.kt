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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_parameter_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nombre y tipo
        view.findViewById<TextView>(R.id.bs_param_name).text = parameter.name
        view.findViewById<TextView>(R.id.bs_param_type).text = getTypeLabel(parameter.parameterType)
        view.findViewById<TextView>(R.id.bs_param_usage).text = "${parameter.usageCount} usos"

        // Descripción
        val descView = view.findViewById<TextView>(R.id.bs_param_description)
        val descRow = view.findViewById<LinearLayout>(R.id.bs_row_description)
        if (!parameter.description.isNullOrEmpty()) {
            descView.text = parameter.description
            descRow.visibility = View.VISIBLE
        } else {
            descRow.visibility = View.GONE
        }

        // Unidad
        val unitView = view.findViewById<TextView>(R.id.bs_param_unit)
        val unitRow = view.findViewById<LinearLayout>(R.id.bs_row_unit)
        if (!parameter.unit.isNullOrEmpty()) {
            unitView.text = parameter.unit
            unitRow.visibility = View.VISIBLE
        } else {
            unitRow.visibility = View.GONE
        }

        // Visibilidad
        view.findViewById<TextView>(R.id.bs_param_visibility).text = when {
            parameter.isGlobal && parameter.ownerId == null -> "Sistema (oficial)"
            parameter.isGlobal -> "Global"
            else -> "Personal"
        }

        // Estado
        view.findViewById<TextView>(R.id.bs_param_status).text =
            if (parameter.isActive) "Activo" else "Inactivo"

        // Dueño (solo si tiene)
        val ownerRow = view.findViewById<LinearLayout>(R.id.bs_row_owner)
        val ownerView = view.findViewById<TextView>(R.id.bs_param_owner)
        if (!parameter.ownerName.isNullOrEmpty()) {
            ownerView.text = parameter.ownerName
            ownerRow.visibility = View.VISIBLE
        } else {
            ownerRow.visibility = View.GONE
        }

        // Trackeable
        val trackableRow = view.findViewById<LinearLayout>(R.id.bs_row_trackable)
        val trackableView = view.findViewById<TextView>(R.id.bs_param_trackable)
        trackableView.text = if (parameter.isTrackable) {
            "Sí (se calculan métricas)"
        } else {
            "No (solo informativo)"
        }
        trackableRow.visibility = View.VISIBLE

        // Agregación de métrica
        val aggregationRow = view.findViewById<LinearLayout>(R.id.bs_row_aggregation)
        val aggregationView = view.findViewById<TextView>(R.id.bs_param_aggregation)
        if (parameter.isTrackable && parameter.metricAggregation != null) {
            val aggLabel = parameter.getAggregationLabel() ?: parameter.metricAggregation
            aggregationView.text = aggLabel
            aggregationRow.visibility = View.VISIBLE
        } else {
            aggregationRow.visibility = View.GONE
        }

        // Botón cerrar
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
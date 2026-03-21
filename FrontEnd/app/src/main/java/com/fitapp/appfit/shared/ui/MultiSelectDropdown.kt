package com.fitapp.appfit.shared.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.fitapp.appfit.R

class MultiSelectDropdown(context: Context) : LinearLayout(context) {

    data class Item(val id: Long, val name: String)

    var onSelectionChanged: ((Set<Long>) -> Unit)? = null

    private val selectedIds = mutableSetOf<Long>()
    private var allItems: List<Item> = emptyList()
    private val checkboxes = mutableListOf<Pair<Item, CheckBox>>()
    private var isExpanded = false

    // Colores
    private val colorGold   by lazy { resources.getColor(R.color.gold_primary, null) }
    private val colorSurface by lazy { resources.getColor(R.color.surface_dark, null) }
    private val colorText   by lazy { resources.getColor(R.color.text_primary_dark, null) }
    private val colorHint   by lazy { resources.getColor(R.color.text_secondary_dark, null) }

    // ── Vista raíz ────────────────────────────────────────────────────────────
    private val headerRow: LinearLayout
    private val summaryText: TextView
    private val arrowIcon: TextView
    private val listContainer: LinearLayout

    init {
        orientation = VERTICAL

        // Fondo con borde dorado
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f.dp
            setColor(colorSurface)
            setStroke(1.dp.toInt(), Color.parseColor("#333333"))
        }

        // ── Cabecera (siempre visible, hace toggle) ───────────────────────────
        headerRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(14.dp.toInt(), 14.dp.toInt(), 14.dp.toInt(), 14.dp.toInt())
            isClickable = true
            isFocusable = true
            background = TypedValue().let { tv ->
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                context.getDrawable(tv.resourceId)
            }
        }

        summaryText = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            textSize = 14f
            setTextColor(colorHint)
        }

        arrowIcon = TextView(context).apply {
            text = "▾"
            textSize = 14f
            setTextColor(colorGold)
            setPadding(8.dp.toInt(), 0, 0, 0)
        }

        headerRow.addView(summaryText)
        headerRow.addView(arrowIcon)
        addView(headerRow)

        // ── Lista colapsable ──────────────────────────────────────────────────
        listContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            visibility = GONE
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // Separador
        listContainer.addView(View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#2A2A2A"))
        })

        addView(listContainer)

        // Toggle al pulsar cabecera
        headerRow.setOnClickListener { toggle() }

        refreshSummary()
    }

    // ── API pública ───────────────────────────────────────────────────────────

    fun setItems(items: List<Item>) {
        allItems = items
        checkboxes.clear()
        // Limpiar lista (conservar separador en índice 0)
        while (listContainer.childCount > 1) listContainer.removeViewAt(1)

        if (items.isEmpty()) {
            listContainer.addView(TextView(context).apply {
                text = "No hay opciones disponibles"
                textSize = 13f
                setTextColor(colorHint)
                setPadding(14.dp.toInt(), 12.dp.toInt(), 14.dp.toInt(), 12.dp.toInt())
            })
            return
        }

        items.forEach { item ->
            val cb = CheckBox(context).apply {
                isChecked = selectedIds.contains(item.id)
                buttonTintList = ColorStateList.valueOf(colorGold)
                isClickable = false
                isFocusable = false
            }

            val lbl = TextView(context).apply {
                text = item.name
                textSize = 14f
                setTextColor(colorText)
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 12.dp.toInt()
                }
            }

            val row = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                setPadding(14.dp.toInt(), 12.dp.toInt(), 14.dp.toInt(), 12.dp.toInt())
                isClickable = true
                isFocusable = true
                background = TypedValue().let { tv ->
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                    context.getDrawable(tv.resourceId)
                }
                setOnClickListener {
                    cb.isChecked = !cb.isChecked
                    if (cb.isChecked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                    refreshSummary()
                    onSelectionChanged?.invoke(selectedIds.toSet())
                }
                addView(cb)
                addView(lbl)
            }

            listContainer.addView(row)
            checkboxes.add(item to cb)
        }
    }

    /** Marca los IDs indicados como seleccionados (para pre-rellenar en edición) */
    fun setSelected(ids: Collection<Long>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        checkboxes.forEach { (item, cb) -> cb.isChecked = selectedIds.contains(item.id) }
        refreshSummary()
    }

    fun getSelected(): Set<Long> = selectedIds.toSet()

    fun collapse() {
        isExpanded = false
        listContainer.visibility = GONE
        arrowIcon.text = "▾"
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private fun toggle() {
        isExpanded = !isExpanded
        listContainer.visibility = if (isExpanded) VISIBLE else GONE
        arrowIcon.text = if (isExpanded) "▴" else "▾"
    }

    private fun refreshSummary() {
        val names = allItems.filter { selectedIds.contains(it.id) }.map { it.name }
        if (names.isEmpty()) {
            summaryText.text = "Seleccionar..."
            summaryText.setTextColor(colorHint)
        } else {
            summaryText.text = names.joinToString(", ")
            summaryText.setTextColor(colorText)
        }
    }

    private val Float.dp: Float get() = this * resources.displayMetrics.density
    private val Int.dp: Float  get() = this.toFloat() * resources.displayMetrics.density
}
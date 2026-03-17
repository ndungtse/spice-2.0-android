package org.medtroniclabs.uhis.formgeneration.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.databinding.DialogMultiDatePickerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MultiSelectDatePickerDialog(
    context: Context,
    private val initialSelectedDates: List<Long>,
    private val minDate: Long? = null,
    private val maxDate: Long? = null,
    private val onDateSelected: (List<Long>) -> Unit,
) : Dialog(context) {
    private lateinit var binding: DialogMultiDatePickerBinding

    private val selectedDates = initialSelectedDates.toMutableSet()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogMultiDatePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        updateUI()
    }

    private fun initView() {
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.okButton.setOnClickListener {
            onDateSelected(selectedDates.toList())
            dismiss()
        }
        binding.clearAllBtn.setOnClickListener {
            selectedDates.clear()
            updateUI(true)
        }
        binding.prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateUI()
        }
        binding.nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateUI()
        }
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        binding.calendarRecyclerView.adapter = MultiSelectDatePickerAdapter(context, calendar, selectedDates, minDate, maxDate) {
            selectedDates.clear()
            selectedDates.addAll(it)
        }
        updateButtonsState()
    }

    private fun updateUI(isClear: Boolean = false) {
        binding.monthYearTextView.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        binding.calendarRecyclerView.adapter = MultiSelectDatePickerAdapter(context, calendar, selectedDates, minDate, maxDate) {
            selectedDates.clear()
            selectedDates.addAll(it)
            updateButtonsState()
        }
        if (isClear) {
            updateButtonsState()
        }
    }

    private fun updateButtonsState() {
        val hasSelectedDates = selectedDates.isNotEmpty()
        binding.okButton.isEnabled = hasSelectedDates
        binding.clearAllBtn.isEnabled = hasSelectedDates

        val disabledColor = ContextCompat.getColor(context, android.R.color.darker_gray)
        val enabledColor = ContextCompat.getColor(context, R.color.cobalt_blue)

        binding.okButton.setTextColor(if (hasSelectedDates) enabledColor else disabledColor)
        binding.clearAllBtn.setTextColor(if (hasSelectedDates) enabledColor else disabledColor)
    }
}

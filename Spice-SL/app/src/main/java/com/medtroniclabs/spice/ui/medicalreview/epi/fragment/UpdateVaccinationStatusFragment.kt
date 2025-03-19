package com.medtroniclabs.spice.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.appextensions.getLongDate
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.DialogUpdateVaccinationStatusBinding
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.ui.BaseDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.view.VaccinationStatusNudge
import com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel.ImmunisationViewModel
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class UpdateVaccinationStatusFragment(
    private val vaccinationItem: VaccinationDetail
) : BaseDialogFragment() {

    private lateinit var binding: DialogUpdateVaccinationStatusBinding
    private val viewModel: ImmunisationViewModel by activityViewModels()
    private val displayFormatter = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUpdateVaccinationStatusBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rect_white)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (600 * resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
    }

    private fun initView() {
        binding.tvVaccinationName.text = vaccinationItem.vaccineName
        binding.flEpiStatusNudge.addView(VaccinationStatusNudge(context = requireContext(), item = vaccinationItem))

        vaccinationItem.updatedScheduleDate?.let {
            binding.tvScheduledDate.text = it.format(displayFormatter)
        }

        if (vaccinationItem.vaccinatedDate != null) {
            shouldEnableUpdate(false)
            binding.tvVaccinationDate.text = vaccinationItem.vaccinatedDate!!.getLocalDate().format(displayFormatter)
        } else {
            shouldEnableUpdate(true)
            binding.tvVaccinationDate.text = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
            //binding.tvVaccinationDate.text = vaccinationItem.updatedScheduleDate!!.format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
        }
    }

    private fun shouldEnableUpdate(flag: Boolean) {
        binding.tvVaccinationDate.isEnabled = flag
        binding.btnVaccinatoinDone.isEnabled = flag
        if (flag)
            binding.ivDatePicker.visible()
        else
            binding.ivDatePicker.gone()
    }

    private fun clickListener() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.tvVaccinationDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnVaccinatoinDone.setOnClickListener {
            addVaccinationDetail()
            dismiss()
        }
    }

    private fun addVaccinationDetail() {
        val inputFormatter = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)
        val outputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        val inputDate = binding.tvVaccinationDate.text.toString()
        val localDate = LocalDate.parse(inputDate, inputFormatter)
        val formattedDate = localDate.atStartOfDay(ZoneOffset.UTC).format(outputFormatter)
        vaccinationItem.status = "Vaccinated"
        vaccinationItem.vaccinatedDate = formattedDate
        vaccinationItem.isEdited = true
        viewModel.addVaccinationDetail(vaccinationItem)
    }

    private fun showDatePicker() {
        val selectedDate = DateUtils.convertedMMMToddMM(binding.tvVaccinationDate.text.toString())
        val minDate = vaccinationItem.updatedScheduleDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()
            ?.toEpochMilli() ?: vaccinationItem.scheduledDate.getLongDate(
            DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        )
        ViewUtils.showDatePicker(
            context = requireContext(),
            disableFutureDate = true,
            date = selectedDate,
            minDate = minDate
        ) { _, year, month, dayOfMonth ->
            DateUtils.convertDateTimeToDate(
                "$dayOfMonth-$month-$year",
                DateUtils.DATE_FORMAT_ddMMyyyy, DATE_ddMMyyyy
            ).let { stringDate ->
                binding.tvVaccinationDate.text = stringDate
            }
        }
    }

}
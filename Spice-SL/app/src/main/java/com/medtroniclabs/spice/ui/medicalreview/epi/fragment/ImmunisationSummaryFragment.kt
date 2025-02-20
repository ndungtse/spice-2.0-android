package com.medtroniclabs.spice.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.ImmunisationSummaryFragmentBinding
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel.ImmunisationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ImmunisationSummaryFragment :  BaseFragment() {

    private lateinit var binding: ImmunisationSummaryFragmentBinding
    private val viewModel: ImmunisationViewModel by activityViewModels()

    companion object {
        const val TAG = "ImmunisationSummaryFragment"
        fun newInstance() = ImmunisationSummaryFragment()

        fun newInstance(
            patientId: String?,
            dateOfBirth: String?,
            encounterId: String?,
            patientReference: String?
        ): ImmunisationSummaryFragment {
            val fragment = ImmunisationSummaryFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            bundle.putString(DefinedParams.DOB, dateOfBirth)
            bundle.putString(DefinedParams.EncounterId, encounterId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ImmunisationSummaryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        arguments?.getString(DefinedParams.EncounterId)?.let { encounterId ->
            viewModel.getImmunisationSummaryDetails(encounterId)
        }

        binding.tvNextVaccinationDate.setOnClickListener {
            showDatePicker()
        }

        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails()?.firstName,
            SecuredPreference.getUserDetails()?.lastName
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DATE_ddMMyyyy
        )
    }

    private fun showDatePicker() {
        val selectedDate = DateUtils.convertedMMMToddMM(binding.tvNextVaccinationDate.text.toString())
        val minDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ViewUtils.showDatePicker(
            context = requireContext(),
            disableFutureDate = false,
            date = selectedDate,
            minDate = minDate
        ) { _, year, month, dayOfMonth ->
            DateUtils.convertDateTimeToDate(
                "$dayOfMonth-$month-$year",
                DateUtils.DATE_FORMAT_ddMMyyyy, DATE_ddMMyyyy
            ).let { stringDate ->
                binding.tvNextVaccinationDate.text = stringDate

                val inputFormatter = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)
                val outputFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
                val localDate = LocalDate.parse(stringDate, inputFormatter)
                val formattedDate = localDate.atStartOfDay(ZoneOffset.UTC).format(outputFormatter)
                viewModel.nextVaccinationDetails?.nextVisitDate = formattedDate
            }
        }
    }

    private fun attachObserver() {
        viewModel.immunisationSummaryLiveData.observe(viewLifecycleOwner) {
            when(it.state) {
                ResourceState.LOADING -> showProgress()
                ResourceState.SUCCESS -> {
                    hideProgress()
                    it.data?.let { summaryDetails ->
                        updateSummaryDetails(summaryDetails)
                    }
                }
                ResourceState.ERROR -> {
                    showProgress()
                }
            }
        }

        viewModel.nextVaccinationDetails?.let {
            binding.tvNextDuration.text = it.nextVaccinationDuration
            binding.tvNextDose.text = it.nextVaccinationDose.joinToString(separator = ", ")
            binding.tvNextVaccinationDate.text = DateUtils.convertDateTimeToDate(
                it.nextVisitDate,
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DATE_ddMMyyyy
            )
        }
    }

    private fun updateSummaryDetails(response: ResponseImmunisationSummaryDetails) {
        val vaccinationTaken = response.vaccinated.joinToString(separator = ", ")
        binding.tvVaccinationTaken.text = vaccinationTaken

        if (response.missedVaccine.isEmpty()) {
            binding.lblVaccinationMissed.gone()
            binding.tvVaccinationMissedSeparator.gone()
            binding.tvVaccinationMissed.gone()

            binding.lblMissedReason.gone()
            binding.tvMissedReasonSeparator.gone()
            binding.tvMissedReason.gone()
        } else {
            val missedVaccine = response.missedVaccine.joinToString(separator = ", ")
            binding.tvVaccinationMissed.text = missedVaccine

            val missedReason = response.missedReason ?: "--"
            binding.tvMissedReason.text = missedReason
        }
    }

}
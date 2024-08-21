package com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.getDecimalFormatted
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.FragmentNeonateSummaryBinding
import com.medtroniclabs.spice.data.model.NeonateDTO
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.viewmodel.LabourDeliverySummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.viewmodel.LabourDeliveryViewModel

class NeonateSummaryFragment : BaseFragment() {

    private lateinit var binding: FragmentNeonateSummaryBinding
    private val viewModel: LabourDeliverySummaryViewModel by activityViewModels()
    private val viewModelLabour:LabourDeliveryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNeonateSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { neonateState ->
                        setDetails(neonateState.neonateDTO)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun setDetails(neonateDTO: NeonateDTO?) {
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails().firstName,
            SecuredPreference.getUserDetails().lastName
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        binding.tvNeonateOutcome.text =
            neonateDTO?.neonateOutcome ?: getString(R.string.hyphen_symbol)
        binding.tvGender.text =
            neonateDTO?.gender?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)
        if (!neonateDTO?.birthWeight.isNullOrEmpty()) {
            var weight = getDecimalFormatted(neonateDTO?.birthWeight)
            binding.tvBirthWeight.text = if (weight.toDouble()== 0.0){
                (getString(R.string.na))
            }else if (weight.toDouble() < DefinedParams.LowBirthWeight){
                weight.plus(" ").plus(getString(R.string.kg))
            } else{
                weight.plus(" ").plus(getString(R.string.kgs))
            }
        }else{
            binding.tvBirthWeight.text= (getString(R.string.na))
        }

        binding.tvStateOfBaby.text = neonateDTO?.stateOfBaby ?: getString(R.string.hyphen_symbol)
        binding.tvGestationalAge.text =
            neonateDTO?.gestationalAge?.let { DateUtils.formatGestationalAge(it.toLong(),requireContext()) }
                ?: getString(R.string.hyphen_symbol)
        viewModelLabour.gestationalAge =  neonateDTO?.gestationalAge
        binding.tvAPGARScore.text =
            if (neonateDTO?.apgarScoreFiveMinuteDTO?.fiveMinuteTotalScore == null) {
                getString(R.string.hyphen_symbol)
            } else {
                neonateDTO.apgarScoreFiveMinuteDTO.fiveMinuteTotalScore.toString().plus(
                    getString(
                        R.string.five_minutes
                    )
                )
            }
        binding.tvSignsSymptomsObserved.text = neonateDTO?.signs?.joinToString(separator = "\n")?:getString(R.string.hyphen_symbol)
        binding.tvPatientStatus.text = getString(R.string.title_neonate)
    }

    companion object {
        const val TAG = "NeonateSummaryFragment"

        fun newInstance(): NeonateSummaryFragment {
            return NeonateSummaryFragment()
        }
    }
}
package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.labourdelivery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.CommonUtils.getDecimalFormatted
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.model.NeonateDTO
import org.medtroniclabs.uhis.databinding.FragmentNeonateSummaryBinding
import org.medtroniclabs.uhis.formgeneration.extension.capitalizeFirstChar
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.labourdelivery.viewmodel.LabourDeliverySummaryViewModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.labourdelivery.viewmodel.LabourDeliveryViewModel

class NeonateSummaryFragment : BaseFragment() {
    private lateinit var binding: FragmentNeonateSummaryBinding
    private val viewModel: LabourDeliverySummaryViewModel by activityViewModels()
    private val viewModelLabour: LabourDeliveryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNeonateSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
            SecuredPreference.getUserDetails()?.firstName,
            SecuredPreference.getUserDetails()?.lastName,
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy,
        )
        binding.tvNeonateOutcome.text =
            neonateDTO?.neonateOutcome ?: getString(R.string.hyphen_symbol)
        binding.tvGender.text =
            neonateDTO?.gender?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)
        if (!neonateDTO?.birthWeight.isNullOrEmpty()) {
            var weight = getDecimalFormatted(neonateDTO?.birthWeight)
            binding.tvBirthWeight.text = if (weight.toDouble() == 0.0) {
                (getString(R.string.na))
            } else if (weight.toDouble() < DefinedParams.LowBirthWeight) {
                weight.plus(" ").plus(getString(R.string.kg)).plus(" ").plus(
                    "(${CommonUtils.birthWeight(
                        weight.toDouble(),
                        requireContext(),
                    )})",
                )
            } else {
                weight.plus(" ").plus(getString(R.string.kg)).plus(" ").plus(
                    "(${CommonUtils.birthWeight(
                        weight.toDouble(),
                        requireContext(),
                    )})",
                )
            }
        } else {
            binding.tvBirthWeight.text = (getString(R.string.na))
        }

        binding.tvStateOfBaby.text = neonateDTO?.stateOfBaby ?: getString(R.string.hyphen_symbol)
        binding.tvGestationalAge.text =
            neonateDTO?.gestationalAge?.let { DateUtils.formatGestationalAge(it.toLong(), requireContext()) }
                ?: getString(R.string.hyphen_symbol)
        viewModelLabour.gestationalAge = neonateDTO?.gestationalAge
        binding.tvAPGARScore.text =
            if (neonateDTO?.apgarScoreFiveMinuteDTO?.fiveMinuteTotalScore == null) {
                getString(R.string.hyphen_symbol)
            } else {
                neonateDTO.apgarScoreFiveMinuteDTO.fiveMinuteTotalScore.toString().plus(
                    getString(
                        R.string.five_minutes,
                    ),
                )
            }
        binding.tvSignsSymptomsObserved.text = neonateDTO?.signs?.joinToString(separator = "\n") ?: getString(R.string.hyphen_symbol)
        binding.tvPatientStatus.text = getString(R.string.title_neonate)
    }

    companion object {
        const val TAG = "NeonateSummaryFragment"

        fun newInstance(): NeonateSummaryFragment = NeonateSummaryFragment()
    }
}

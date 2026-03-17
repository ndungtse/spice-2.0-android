package org.medtroniclabs.uhis.ui.medicalreview.undertwomonths.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.BirthHistoryResponse
import org.medtroniclabs.uhis.databinding.FragmentBirthHistoryBinding
import org.medtroniclabs.uhis.formgeneration.extension.capitalizeFirstChar
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.toYesNoOrDefault
import org.medtroniclabs.uhis.ui.medicalreview.undertwomonths.viewmodel.BirthHistoryViewModel

class BirthHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentBirthHistoryBinding
    val viewModel: BirthHistoryViewModel by activityViewModels()

    companion object {
        const val TAG = "BirthHistoryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            FragmentBirthHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
        getBirthHistory()
    }

    private fun getBirthHistory() {
        val patientId = arguments?.getString(DefinedParams.PatientId, "")
        val memberId = arguments?.getString(DefinedParams.MEMBER_ID, "")
        viewModel.getBirthHistoryDetails(patientId, memberId)
    }

    fun attachObservers() {
        viewModel.birthHistoryLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        setBirthHistoryDetails(it)
                    }
                }
            }
        }
    }

    private fun setBirthHistoryDetails(birthHistoryDetails: BirthHistoryResponse) {
        binding.apply {
            tvBirthWeight.text = birthHistoryDetails.birthWeight?.let { weight ->
                val decimalBirthWeight = CommonUtils.getDecimalFormatted(weight)
                birthHistoryDetails.birthWeightCategory?.let { category ->
                    when {
                        decimalBirthWeight.toDouble() == 0.0 -> getString(R.string.na)
                        else -> {
                            decimalBirthWeight.plus(" ").plus(getString(R.string.kg)).plus(" ($category)")
                        }
                    }
                } ?: getString(R.string.separator_double_hyphen)
            } ?: getString(R.string.na)
//            tvBirthWeight.text = birthHistoryDetails.birthWeight?.let { it ->
//                val decimalBirthWeight = CommonUtils.getDecimalFormatted(it)
//                birthHistoryDetails.birthWeightCategory?.let {it2->
//                    if (decimalBirthWeight.toDouble() < viewModel.lowBirthWeight){
//                        decimalBirthWeight.plus(
//                            " ").plus(getString(R.string.kg))
//                            .plus(" ($it2)")
//                    } else {
//                        decimalBirthWeight.plus(
//                            " ").plus(getString(R.string.kgs)).plus(" ($it2)")
//                    }
//                } ?: getString(R.string.separator_double_hyphen)
//            } ?:  getString(R.string.separator_double_hyphen)

            tvGestationalAge.text = birthHistoryDetails.gestationalAge?.let { ageWeek ->
                val weeksText = getString(if (ageWeek >= 1) R.string.weeks_baby else R.string.week_baby)
                val formattedPrematureText = birthHistoryDetails.gestationalAgeCategory
                    ?.takeIf { it.isNotBlank() }
                    ?.capitalizeFirstChar()
                    ?.let { " ($it)" }
                    .orEmpty()
                "$ageWeek$weeksText$formattedPrematureText"
            } ?: getString(R.string.separator_double_hyphen)
            tvBreathingProblem.text = birthHistoryDetails.haveBreathingProblem.toYesNoOrDefault(
                getString(R.string.separator_double_hyphen),
                getString(R.string.yes),
                getString(R.string.no),
            )
        }
    }
}

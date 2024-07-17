package com.medtroniclabs.spice.ui.medicalreview.undertwomonths.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.databinding.FragmentBirthHistoryBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.toYesNoOrDefault
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel.BirthHistoryViewModel

class BirthHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentBirthHistoryBinding
    val viewModel: BirthHistoryViewModel by activityViewModels()

    companion object {
        const val TAG = "BirthHistoryFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentBirthHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
        getBirthHistory()
    }
    private fun getBirthHistory(){
        val patientId = arguments?.getString(DefinedParams.PatientId, "")
        val memberId = arguments?.getString(DefinedParams.MemberID, "")
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
            tvBirthWeight.text = birthHistoryDetails.birthWeight?.let {
                val decimalBirthWeight = CommonUtils.getDecimalFormatted(it)
                birthHistoryDetails.birthWeightCategory?.let {
                    if (decimalBirthWeight.toDouble() < viewModel.lowBirthWeight && birthHistoryDetails.birthWeightCategory != null) {
                        decimalBirthWeight.plus(getString(R.string.kg))
                            .plus(birthHistoryDetails.birthWeightCategory)
                    } else {
                        decimalBirthWeight.plus(getString(R.string._kg))
                    }
                } ?: getString(R.string.separator_double_hyphen)
            } ?:  getString(R.string.separator_double_hyphen)

            tvGestationalAge.text = birthHistoryDetails.gestationalAge?.let { ageWeek ->
                val weeksText =
                    if (ageWeek >= 1) getString(R.string.weeks_baby) else getString(R.string.week_baby)
                val prematureText =
                    if (ageWeek < 37 && birthHistoryDetails.gestationalAgeCategory != null) birthHistoryDetails.gestationalAgeCategory else ""
                ageWeek.toString().plus(weeksText).plus(prematureText)
            } ?: getString(R.string.separator_double_hyphen)
            tvBreathingProblem.text = birthHistoryDetails.haveBreathingProblem.toYesNoOrDefault(
                getString(R.string.separator_double_hyphen),
                getString(R.string.yes),
                getString(R.string.no)
            )
        }
    }
}
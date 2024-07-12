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
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
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
                if (decimalBirthWeight.toDouble() < viewModel.lowBirthWeight) {
                    decimalBirthWeight.plus(getString(R.string.kg)).plus(getString(R.string.low_birth_weight))
                } else {
                    decimalBirthWeight.plus(getString(R.string._kg))
                }
            } ?: "--"

            tvGestationalAge.text = birthHistoryDetails.gestationalAge?.let { ageWeek ->
                val weeksText =
                    if (ageWeek >= 1) getString(R.string.weeks_baby) else getString(R.string.week_baby)
                val prematureText = if (ageWeek < 37) getString(R.string.premature_baby) else ""
                ageWeek.toString().plus(weeksText).plus(prematureText)
            } ?: "--"

            tvBreathingProblem.text = (birthHistoryDetails.haveBreathingProblem?.toString()?.capitalizeFirstChar() ?: "--").toString()
        }
    }
}
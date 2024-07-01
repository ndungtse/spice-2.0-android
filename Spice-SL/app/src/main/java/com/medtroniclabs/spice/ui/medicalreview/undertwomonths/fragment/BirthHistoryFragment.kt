package com.medtroniclabs.spice.ui.medicalreview.undertwomonths.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.databinding.FragmentBirthHistoryBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel.BirthHistoryViewModel
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
            tvBirthWeight.text = birthHistoryDetails.birthWeight?: "--"
            tvGestationalAge.text = birthHistoryDetails.lastMenstrualPeriod?.let { lmp ->
               val week=DateUtils.calculateGestationalAge(parseToLocalDate(lmp))
                if (week<37){
                    week.toString().plus(getString(R.string.weeks_baby)).plus(getString(R.string.premature_baby))
                }else{
                    week.toString().plus(getString(R.string.weeks_baby))
                }
            } ?: "--"
            tvBreathingProblem.text = birthHistoryDetails.breathingProblem?: "--"
        }
    }

    private fun parseToLocalDate(dateString: String): LocalDate {
        val zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return zonedDateTime.toLocalDate()
    }
}
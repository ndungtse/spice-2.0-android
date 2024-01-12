package com.medtroniclabs.spice.ui.household.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentHouseholdDetailsBinding
import com.medtroniclabs.spice.databinding.SummaryListItemBinding

class HouseholdDetailsFragment : Fragment() {

    private lateinit var binding: FragmentHouseholdDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHouseholdDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        renderHouseholdDetailsSummary()
    }

    private fun renderHouseholdDetailsSummary() {
        var householdSummaryMap = HashMap<String, Any>()
        householdSummaryMap[DefinedParams.HOUSE_NO] = 4647
        householdSummaryMap[DefinedParams.VILLAGE ] = "Gbinti"
        householdSummaryMap[DefinedParams.LANDMARK] = "Near church"
        householdSummaryMap[DefinedParams.HH_MOBILE_NUMBER] = 9032894843
        for ((key,value) in householdSummaryMap){
            val summaryViewBinding = SummaryListItemBinding.inflate(LayoutInflater.from(context))
            when(key){
                DefinedParams.HOUSE_NO ->{
                    summaryViewBinding.tvLabel.text = getString(R.string.house_no)
                    summaryViewBinding.tvValue.text = householdSummaryMap[key].toString()
                }
                DefinedParams.VILLAGE ->{
                    summaryViewBinding.tvLabel.text = getString(R.string.village)
                    summaryViewBinding.tvValue.text = householdSummaryMap[key].toString()
                }
                DefinedParams.LANDMARK ->{
                    summaryViewBinding.tvLabel.text = getString(R.string.landmark)
                    summaryViewBinding.tvValue.text = householdSummaryMap[key].toString()
                }
                DefinedParams.HH_MOBILE_NUMBER ->{
                    summaryViewBinding.tvLabel.text = getString(R.string.hh_mobile_number)
                    summaryViewBinding.tvValue.text = householdSummaryMap[key].toString()
                }
            }
            binding.llDetails.addView(summaryViewBinding.root)
        }
    }
}
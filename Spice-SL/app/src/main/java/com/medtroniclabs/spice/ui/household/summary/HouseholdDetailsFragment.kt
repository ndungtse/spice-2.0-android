package com.medtroniclabs.spice.ui.household.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.model.HouseholdCardDetail
import com.medtroniclabs.spice.databinding.FragmentHouseholdDetailsBinding
import com.medtroniclabs.spice.databinding.SummaryListItemBinding
import com.medtroniclabs.spice.ui.household.viewmodel.HouseHoldSummaryViewModel

class HouseholdDetailsFragment : Fragment() {
    private lateinit var binding: FragmentHouseholdDetailsBinding
    private val householdSummaryViewModel: HouseHoldSummaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHouseholdDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        attachObserver()
    }

    private fun attachObserver() {
        householdSummaryViewModel.householdCardDetailLiveData.observe(viewLifecycleOwner) { houseHoldDetail ->
            renderHouseholdDetailsSummary(houseHoldDetail)
        }
    }

    private fun renderHouseholdDetailsSummary(houseHoldDetail: HouseholdCardDetail) {
        binding.llDetails.removeAllViews()

        addHouseholdNoView(houseHoldDetail.householdNo?.toString() ?: getString(R.string.separator_double_hyphen))
        addVillageNameView(houseHoldDetail.villageName)
    }

    private fun addHouseholdNoView(householdNo: String) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.household_no)
        view.tvValue.text = householdNo
        binding.llDetails.addView(view.root)
    }

    private fun addVillageNameView(villageName: String) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.household_location)
        view.tvValue.text = villageName
        binding.llDetails.addView(view.root)
    }
}

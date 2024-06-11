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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseholdDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        addHouseholdNoView(houseHoldDetail.householdNo)
        addVillageNameView(houseHoldDetail.villageName)
        addLandmarkView(houseHoldDetail.landmark)
        addHouseholdHeadNumberView(houseHoldDetail.householdHeadPhoneNumber)
        addMemberRegisteredView(houseHoldDetail.memberRegistered, houseHoldDetail.memberAdded)
    }

    private fun addHouseholdNoView(householdNo: Long) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.household_no)
        view.tvValue.text = householdNo.toString()
        binding.llDetails.addView(view.root)
    }

    private fun addVillageNameView(villageName: String) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.village)
        view.tvValue.text = villageName
        binding.llDetails.addView(view.root)
    }

    private fun addLandmarkView(landmark: String?) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.landmark)
        view.tvValue.text = landmark ?: getString(R.string.hyphen_symbol)
        binding.llDetails.addView(view.root)
    }

    private fun addHouseholdHeadNumberView(householdHeadPhoneNo: String?) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.hh_mobile_number)
        view.tvValue.text = householdHeadPhoneNo ?: getString(R.string.hyphen_symbol)
        binding.llDetails.addView(view.root)
    }

    private fun addMemberRegisteredView(memberRegistered: Int, memberAdded: Int) {
        binding.tvLabel.text = getString(R.string.members_registered)
        binding.tvValue.text =
            requireContext().getString(
                R.string.people_registered,
                memberAdded,
                memberRegistered
            )
        binding.ivMemberRegCount.visibility =
            if (memberRegistered == memberAdded) View.INVISIBLE else View.VISIBLE
    }

}
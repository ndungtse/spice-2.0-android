package com.medtroniclabs.spice.ui.household.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.FragmentHouseholdDetailsBinding
import com.medtroniclabs.spice.databinding.SummaryListItemBinding
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
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
        householdSummaryViewModel.houseHoldDetailLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let { houseHoldDetail ->
                        householdSummaryViewModel.getVillageByID (houseHoldDetail.villageId)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                }
            }
        }

        householdSummaryViewModel.villageDetailLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let { villageDetail ->
                        renderHouseholdDetailsSummary(villageDetail.name)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                }
            }
        }
    }

    private fun renderHouseholdDetailsSummary(villageName: String) {
        binding.llDetails.removeAllViews()
        householdSummaryViewModel.houseHoldDetailLiveData.value?.data?.let { houseHoldDetail ->
            val houseHoldNumberViewBinding =
                SummaryListItemBinding.inflate(LayoutInflater.from(context))
            houseHoldNumberViewBinding.tvLabel.text = getString(R.string.house_no)
            houseHoldNumberViewBinding.tvValue.text = houseHoldDetail.householdNo.toString()
            binding.llDetails.addView(houseHoldNumberViewBinding.root)
            val villageViewBinding =
                SummaryListItemBinding.inflate(LayoutInflater.from(context))
            villageViewBinding.tvLabel.text = getString(R.string.village)
            villageViewBinding.tvValue.text = villageName
            binding.llDetails.addView(villageViewBinding.root)
            val landmarkViewBinding = SummaryListItemBinding.inflate(LayoutInflater.from(context))
            landmarkViewBinding.tvLabel.text = getString(R.string.landmark)
            landmarkViewBinding.tvValue.text =
                if (!houseHoldDetail.landmark.isNullOrEmpty()) houseHoldDetail.landmark else
                    getString(R.string.hyphen_symbol)
            binding.llDetails.addView(landmarkViewBinding.root)
            val houseHoldHeadMobileNumber = SummaryListItemBinding.inflate(LayoutInflater.from(context))
            houseHoldHeadMobileNumber.tvLabel.text = getString(R.string.hh_mobile_number)
            houseHoldHeadMobileNumber.tvValue.text =
                houseHoldDetail.headPhoneNumber ?: getString(R.string.hyphen_symbol)
            binding.llDetails.addView(houseHoldHeadMobileNumber.root)
        }
    }
}
package org.medtroniclabs.uhis.ui.household.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.databinding.FragmentHouseholdDetailsBinding
import org.medtroniclabs.uhis.databinding.SummaryListItemBinding
import org.medtroniclabs.uhis.db.response.HouseHoldEntityWithLastActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseHoldSummaryViewModel

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
        householdSummaryViewModel.householdCardDetailLiveData.observe(viewLifecycleOwner) { houseHoldDetailList ->
            val householdDetail = houseHoldDetailList.firstOrNull() ?: return@observe
            renderHouseholdDetailsSummary(householdDetail)
        }
    }

    private fun renderHouseholdDetailsSummary(houseHoldDetail: HouseHoldEntityWithLastActivity) {
        binding.llDetails.removeAllViews()

        addHouseholdNoView(houseHoldDetail.householdNo ?: getString(R.string.separator_double_hyphen))
        addVillageNameView(houseHoldDetail.subVillageName)
        addSummaryView(getString(R.string.ss_name), houseHoldDetail.shasthyaShebikaName)
        addSummaryView(getString(R.string.last_visit_date), DateUtils.formatDateToDisplayFormat(houseHoldDetail.lastActivityAt) ?: "")
    }

    private fun addHouseholdNoView(householdNo: String) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.household_no)
        view.tvValue.text = householdNo
        binding.llDetails.addView(view.root)
    }

    private fun addVillageNameView(villageName: String) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = getString(R.string.village)
        view.tvValue.text = villageName
        binding.llDetails.addView(view.root)
    }

    private fun addSummaryView(
        name: String,
        value: String,
    ) {
        val view = SummaryListItemBinding.inflate(LayoutInflater.from(context))
        view.tvLabel.text = name
        view.tvValue.text = value
        binding.llDetails.addView(view.root)
    }
}

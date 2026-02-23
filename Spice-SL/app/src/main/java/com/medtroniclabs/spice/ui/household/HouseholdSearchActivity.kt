package com.medtroniclabs.spice.ui.household

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.HOUSEHOLDLISTSEARCHTRIGGERED
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.HOUSEHOLDS
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setTextChangeListener
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.isHouseHold
import com.medtroniclabs.spice.databinding.ActivityHouseholdSearchBinding
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.ID
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.isFromHouseHoldRegistration
import com.medtroniclabs.spice.ui.household.adapter.HouseholdListAdapter
import com.medtroniclabs.spice.ui.household.summary.HouseholdSummaryActivity
import com.medtroniclabs.spice.ui.household.viewmodel.HouseholdListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseholdSearchActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityHouseholdSearchBinding
    private val householdListViewModel: HouseholdListViewModel by viewModels()
    private lateinit var householdListAdapter: HouseholdListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.households),
        )
        showLoading()
        initViews()
        setListeners()
        attachObserver()
    }

    override fun onResume() {
        super.onResume()
        householdListViewModel.setUserJourney(HOUSEHOLDS)
    }

    private fun initViews() {
        binding.llFilter.btnFilter.text = getString(R.string.filter)
        binding.llExactSearch.etSearchTerm.hint = getString(R.string.household_name_or_no)
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        val spanCount = if (tabletSize) {
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            DefinedParams.span_count_3
        } else {
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            DefinedParams.span_count_1
        }

        householdListAdapter = HouseholdListAdapter {
            val intent = Intent(this@HouseholdSearchActivity, HouseholdSummaryActivity::class.java)
            intent.putExtra(ID, it)
            intent.putExtra(isFromHouseHoldRegistration, false)
            startActivity(intent)
        }

        binding.rvHouseholdList.apply {
            layoutManager =
                GridLayoutManager(
                    this@HouseholdSearchActivity,
                    DefinedParams.span_count_1,
                )
            adapter = householdListAdapter
        }
    }

    private fun setListeners() {
        binding.llExactSearch.btnSearch.safeClickListener(this)
        binding.btnAddHousehold.safeClickListener(this)
        binding.llFilter.btnFilter.safeClickListener(this)
        binding.llExactSearch.etSearchTerm.setTextChangeListener {
            val input = it?.trim().toString()
            binding.llExactSearch.btnSearch.isEnabled =
                input.isNotEmpty() &&
                ((input[0].isLetter() && input.length >= 3) || input[0].isDigit())

            if (input.isEmpty()) {
                householdListViewModel.setFilterLiveData(search = "")
            }
        }
    }

    private fun attachObserver() {
        householdListViewModel.getFilterLiveData().observe(this) {
            var count = 0
            if (it.filterByVillage.isNotEmpty()) {
                count++
            }
            if (it.filterBySs.isNotEmpty()) {
                count++
            }
            if (it.filterByStatus.isNotEmpty()) {
                count++
            }

            if (count > 0) {
                binding.llFilter.btnFilter.text = this.getString(R.string.filter_count, count)
            } else {
                binding.llFilter.btnFilter.text = getString(R.string.filter)
            }
        }

        householdListViewModel.filteredHouseholdsLiveData.observe(this) {
            hideLoading()
            it?.let {
                setHouseholdListAdapter(it)
            } ?: kotlin.run {
                setHouseholdListAdapter(ArrayList())
            }
        }
    }

    private fun setHouseholdListAdapter(householdList: List<HouseHoldEntityWithMemberCount>) {
        binding.tvHouseHoldCount.text = setLabelValue(householdList.size)
        if (householdList.isNotEmpty()) {
            binding.llFilter.btnFilter.visible()
            binding.tvNoHouseHoldFound.gone()
            binding.rvHouseholdList.visible()
            householdListAdapter.setHouseHoldList(householdList)
        } else {
            binding.tvNoHouseHoldFound.visible()
            binding.rvHouseholdList.gone()
        }
    }

    private fun setLabelValue(size: Int): CharSequence =
        if (size > 1) {
            "$size ${getString(R.string.households)}"
        } else {
            "$size ${getString(R.string.household)}"
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAddHousehold -> {
                withLocationCheck({
                    householdListViewModel.setUserJourney(AnalyticsDefinedParams.ADDHOUSEHOLDBUTTONTRIGGERED)
                    launchHouseholdActivity()
                })
            }

            R.id.btnSearch -> {
                withLocationCheck({
                    householdListViewModel.setUserJourney(HOUSEHOLDLISTSEARCHTRIGGERED)
                    val searchTerm = binding.llExactSearch.etSearchTerm.text
                        .toString()
                    householdListViewModel.setFilterLiveData(search = searchTerm)
//                if (!searchTerm.isNullOrBlank()) {
//                    householdListViewModel.searchByHouseholdNameOrNo(searchTerm.toString())
//                }
                })
            }

            R.id.btnFilter -> {
                withLocationCheck({
                    FilterBottomSheetDialogFragment
                        .newInstance()
                        .show(supportFragmentManager, FilterBottomSheetDialogFragment.TAG)
                })
            }
        }
    }

    private fun launchHouseholdActivity() {
        val intent = Intent(this, ConsentFormActivity::class.java)
        intent.putExtra(isHouseHold, true)
        startActivity(intent)
    }
}

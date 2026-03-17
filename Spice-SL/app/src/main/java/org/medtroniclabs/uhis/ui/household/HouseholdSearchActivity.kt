package org.medtroniclabs.uhis.ui.household

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams.HOUSEHOLDLISTSEARCHTRIGGERED
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams.HOUSEHOLDS
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.hideKeyboard
import org.medtroniclabs.uhis.appextensions.setTextChangeListener
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.DefinedParams.isHouseHold
import org.medtroniclabs.uhis.databinding.ActivityHouseholdSearchBinding
import org.medtroniclabs.uhis.db.dao.HouseholdSortOrder
import org.medtroniclabs.uhis.db.response.HouseHoldEntityWithLastActivity
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.household.HouseholdDefinedParams.ID
import org.medtroniclabs.uhis.ui.household.HouseholdDefinedParams.isFromHouseHoldRegistration
import org.medtroniclabs.uhis.ui.household.adapter.HouseholdListAdapter
import org.medtroniclabs.uhis.ui.household.summary.HouseholdSummaryActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseholdListViewModel
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
        binding.llFilter.btnSort.visible()
        binding.llFilter.btnSort.setText(R.string.sort)
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
        binding.llFilter.btnSort.safeClickListener(this)
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
            if (it.filterBySs.isNotEmpty()) {
                count++
            }
            if (it.filterBySubVillages.isNotEmpty()) {
                count++
            }

            if (count > 0) {
                binding.llFilter.btnFilter.text = this.getString(R.string.filter_count, count)
            } else {
                binding.llFilter.btnFilter.text = getString(R.string.filter)
            }
            val sortCount = if (it.sortOrder == HouseholdSortOrder.DEFAULT) 0 else 1
            binding.llFilter.btnSort.text = if (sortCount > 0) {
                getString(R.string.sort_count, sortCount)
            } else {
                getString(R.string.sort)
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

    private fun setHouseholdListAdapter(householdList: List<HouseHoldEntityWithLastActivity>) {
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
                hideKeyboard(view)
                withLocationCheck({
                    FilterBottomSheetDialogFragment
                        .newInstance()
                        .show(supportFragmentManager, FilterBottomSheetDialogFragment.TAG)
                })
            }
            R.id.btnSort -> {
                hideKeyboard(view)
                handleSortClick()
            }
        }
    }

    private fun handleSortClick() {
        val existingFragment =
            supportFragmentManager.findFragmentByTag(SortDialogFragment.TAG) as? SortDialogFragment
        if (existingFragment == null) {
            SortDialogFragment
                .newInstance()
                .show(supportFragmentManager, SortDialogFragment.TAG)
        } else {
            existingFragment.show(supportFragmentManager, SortDialogFragment.TAG)
        }
    }

    private fun launchHouseholdActivity() {
        val intent = Intent(this, ConsentFormActivity::class.java)
        intent.putExtra(isHouseHold, true)
        startActivity(intent)
    }
}
